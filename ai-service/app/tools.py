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

# Everything we offer the AI. A3: one tool; A4 adds the order tools here.
TOOLS = [SEARCH_PRODUCTS_TOOL]

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
# Dispatcher: the AI sends a tool NAME and its ARGUMENTS as a JSON string.
# run_tool() finds the matching Python function, runs it, and returns the
# result as a JSON string (the "tool" message goes back to the AI as text).
# ---------------------------------------------------------------------------

# Allow-list: the AI can only reach functions listed here, nothing else in the code.
TOOL_FUNCTIONS = {
    "search_products": search_products,
}


def run_tool(name: str, arguments_json: str) -> str:
    function = TOOL_FUNCTIONS.get(name)
    if function is None:
        log.warning("AI asked for an unknown tool: %s", name)
        return json.dumps({"error": f"Unknown tool: {name}"})

    try:
        arguments = json.loads(arguments_json or "{}")
        log.info("Tool call: %s(%s)", name, arguments)
        result = function(**arguments)
    except Exception:
        # Bad arguments, product-service down, timeout... The AI gets a short,
        # safe message (no stack trace, no internal URLs) and can tell the user.
        log.exception("Tool %s failed", name)
        return json.dumps({"error": "The tool failed. Tell the customer to try again later."})

    log.info("Tool result: %d item(s)", len(result))
    return json.dumps(result)
