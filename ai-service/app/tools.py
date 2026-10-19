# The "forms" the AI may fill in, and the code that really runs them.
#
# Two parts per tool:
#   1. The DESCRIPTION (JSON schema) that we send to the AI, so it knows the tool exists,
#      what it is good for and which parameters it takes. The AI only ever sees this.
#   2. The PYTHON FUNCTION that runs when the AI asks for the tool. The AI never sees this code.
import json
import logging

import httpx

from app import config
from app.auth import CurrentUser

log = logging.getLogger(__name__)

SEARCH_PRODUCTS_TOOL = {
    "type": "function",
    "function": {
        "name": "search_products",
        "description": (
            "Search the shop's products by a keyword that appears in the product NAME "
            "(for example 'laptop', 'sony' or 'iphone'). Use one short keyword, not a sentence. "
            "Returns up to 10 products with id, name, price in USD, stock and category. "
            "An empty list means no product name contains that keyword."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "keyword": {
                    "type": "string",
                    "description": "One word to look for in product names, e.g. 'laptop'.",
                },
                "max_price": {
                    "type": "number",
                    "description": "Optional. Only return products that cost at most this much (USD).",
                },
            },
            "required": ["keyword"],
        },
    },
}

GET_MY_ORDERS_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_orders",
        "description": (
            "List the customer's own most recent orders, newest first. Each order has its "
            "order number (shown to the customer as 'Order #number'), its position (1 = the "
            "customer's first order ever), date, status, total in USD and items. "
            "Also returns total_orders, how many orders the customer has in all."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "limit": {
                    "type": "integer",
                    "description": "How many recent orders to return, 1 to 10. Default 5.",
                },
            },
            "required": [],
        },
    },
}

GET_MY_ORDER_BY_POSITION_TOOL = {
    "type": "function",
    "function": {
        "name": "get_my_order_by_position",
        "description": (
            "Get ONE of the customer's own orders by its position. "
            "'my 7th order' or 'my first order' = position 7 or 1 counted_from 'oldest'. "
            "'my last order' = position 1 counted_from 'newest'; "
            "'the order before my last one' = position 2 counted_from 'newest'. "
            "Use it only for words like 2nd, seventh, first, last. For a bare number such as "
            "'order 2' (no '#', no 'nd'/'th'), do not guess: ask the customer whether they "
            "mean their 2nd order or the order shown as 'Order #2'."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "position": {
                    "type": "integer",
                    "description": "1 or higher.",
                },
                "counted_from": {
                    "type": "string",
                    "enum": ["oldest", "newest"],
                    "description": "Count from the first order ever ('oldest') or from the latest ('newest').",
                },
            },
            "required": ["position", "counted_from"],
        },
    },
}

GET_ORDER_BY_NUMBER_TOOL = {
    "type": "function",
    "function": {
        "name": "get_order_by_number",
        "description": (
            "Get ONE of the customer's own orders by the number shown on the My Orders page "
            "as 'Order #41'. Use it only when the customer writes the number with '#' "
            "(or has confirmed they mean the number shown on the page). For a bare number "
            "such as 'order 2', ask first (see get_my_order_by_position)."
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "order_number": {
                    "type": "integer",
                    "description": "The number shown as 'Order #number'.",
                },
            },
            "required": ["order_number"],
        },
    },
}

# Which tools each role gets. A tool that is not in the list is invisible to the AI
# AND refused by run_tool() (checked again there, not only "hidden").
TOOLS_FOR_EVERYONE = [SEARCH_PRODUCTS_TOOL]
CUSTOMER_TOOLS = [GET_MY_ORDERS_TOOL, GET_MY_ORDER_BY_POSITION_TOOL, GET_ORDER_BY_NUMBER_TOOL]


def tools_for(user: CurrentUser) -> list[dict]:
    if user.role == "CUSTOMER":
        return TOOLS_FOR_EVERYONE + CUSTOMER_TOOLS
    return TOOLS_FOR_EVERYONE

# def search_products(keyword: str, max_price: float | None = None) -> list[dict]:
def search_products(keyword: str, max_price: float | None = None) -> list[dict]:


#1. GET {PRODUCT_SERVICE_URL}/products/search?q=keyword&size=10  (5 s timeout)
    response = httpx.get(
        f"{config.PRODUCT_SERVICE_URL}/products/search",
        params={"q": keyword, "size": 10},
        timeout=5.0,
    )
#   2. take the "content" list from the JSON page
    response.raise_for_status()
    products = response.json()["content"]


#3. skip products above max_price (when max_price was given)
    results = []
    for p in products:
        if max_price is not None and p["price"] > max_price:
            continue

#4. return only the fields the AI needs: id, name, price, stock, category
        results.append(
            {
                "id": p["id"],
                "name": p["name"],
                "price": p["price"],
                "stock": p["stock"],
                "category": p["category"],
            }
        )
    return results


# ---------------------------------------------------------------------------
# Order tools. Each receives `user` from run_tool(), NEVER from the AI.
# ---------------------------------------------------------------------------


def _user_headers(user: CurrentUser) -> dict:
    # The same trusted headers the gateway sends; order-service checks ownership with them.
    return {"X-User-Id": str(user.id), "X-User-Role": user.role}


def _fetch_my_orders_page(user: CurrentUser, page: int, size: int) -> dict:
    # order-service returns the customer's orders newest first, as a Spring Page.
    response = httpx.get(
        f"{config.ORDER_SERVICE_URL}/orders/mine",
        params={"page": page, "size": size},
        headers=_user_headers(user),
        timeout=5.0,
    )
    response.raise_for_status()
    return response.json()


def _order_summary(order: dict, position: int | None = None) -> dict:
    # Only what the AI needs (no customerId, no image URLs, no seller ids).
    summary = {
        "order_number": order["id"],
        "date": order["createdAt"][:10],
        "status": order["status"],
        "total": order["totalPrice"],
        "items": [
            {"product": i["productName"], "quantity": i["quantity"], "unit_price": i["unitPrice"]}
            for i in order["items"]
        ],
    }
    if position is not None:
        summary["position"] = position
    return summary


def get_my_orders(user: CurrentUser, limit: int = 5) -> dict:
    limit = max(1, min(limit, 10))  # clamp: the AI may ask for 0 or 1000
    page = _fetch_my_orders_page(user, page=0, size=limit)
    total = page["totalElements"]
    orders = [
        _order_summary(order, position=total - index)  # newest first: index 0 = position total
        for index, order in enumerate(page["content"])
    ]
    return {"total_orders": total, "orders": orders}


# TODO (you type these two, see the explanation in the chat):
#
def get_my_order_by_position(user: CurrentUser, position: int, counted_from: str) -> dict:

#1. ask order-service how many orders the user has (page 0, size 1 -> "totalElements")
    total = _fetch_my_orders_page(user, page=0, size=1)["totalElements"]

#2. position outside 1..total -> return {"error": "..."} that tells the AI the real count
    if position < 1 or position > total:
        return {"error": f"The customer has {total} orders, so there is no order at position {position}."}
    
#3. turn the position into a page index (size 1, the list is newest first)
    if counted_from == "newest":
        index = position - 1
    else:
        index = total - position

#4. fetch that one order and return _order_summary(order, position=...)
    page = _fetch_my_orders_page(user, page=index, size=1)
    order = page["content"][0]
    return _order_summary(order, position=total - index)

#
def get_order_by_number(user: CurrentUser, order_number: int) -> dict:
    order_number = int(order_number)  # never trust AI arguments: "41/../admin" -> error

#1. GET {ORDER_SERVICE_URL}/orders/{order_number} with _user_headers(user)
    response = httpx.get(
        f"{config.ORDER_SERVICE_URL}/orders/{order_number}",
        headers=_user_headers(user),
        timeout=5.0,
    )

#2. 403 (not yours) and 404 (does not exist) -> the SAME error answer
    if response.status_code in (403, 404):
        return {"error": f"Order #{order_number} was not found among the customer's orders."}
    
#3. otherwise raise_for_status() and return _order_summary(...)
    response.raise_for_status()
    return _order_summary(response.json())


# ---------------------------------------------------------------------------
# Dispatcher: the AI sends a tool NAME and its ARGUMENTS as a JSON string.
# run_tool() finds the matching Python function, runs it, and returns the
# result as a JSON string (the "tool" message goes back to the AI as text).
# ---------------------------------------------------------------------------

# Allow-list: the AI can only reach functions listed here, nothing else in the code.
TOOL_FUNCTIONS = {
    "search_products": search_products,
    "get_my_orders": get_my_orders,
    "get_my_order_by_position": get_my_order_by_position,
    "get_order_by_number": get_order_by_number,
}

# These tools get the current user injected by OUR code as the first argument.
TOOLS_NEEDING_USER = {"get_my_orders", "get_my_order_by_position", "get_order_by_number"}


def run_tool(name: str, arguments_json: str, user: CurrentUser) -> str:
    allowed = {tool["function"]["name"] for tool in tools_for(user)}
    function = TOOL_FUNCTIONS.get(name)
    if function is None or name not in allowed:
        log.warning("User %s: AI asked for a tool it may not use: %s", user.id, name)
        return json.dumps({"error": f"Unknown tool: {name}"})

    try:
        arguments = json.loads(arguments_json or "{}")
        log.info("Tool call by user %s: %s(%s)", user.id, name, arguments)
        if name in TOOLS_NEEDING_USER:
            # If the AI smuggles in a "user" argument, Python raises TypeError
            # ("multiple values for argument 'user'") and we land in except below.
            result = function(user, **arguments)
        else:
            result = function(**arguments)
    except Exception:
        # Bad arguments, a service down, timeout... The AI gets a short,
        # safe message (no stack trace, no internal URLs) and can tell the user.
        log.exception("Tool %s failed", name)
        return json.dumps({"error": "The tool failed. Tell the customer to try again later."})

    log.info("Tool result: %s", "error" if isinstance(result, dict) and "error" in result else "ok")
    return json.dumps(result)
