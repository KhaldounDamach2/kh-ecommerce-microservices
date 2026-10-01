═══════════════════════════════════════════════════════════════
WORKING AGREEMENT — READ THIS FIRST
═══════════════════════════════════════════════════════════════

1. DO NOT act autonomously. Wait for explicit tasks.
2. Generate ONLY the files I list. Do NOT create extra files.
3. Do NOT modify any file I didn't explicitly mention.
4. After generating, ACTUALLY CREATE the files on disk.
5. Do NOT run commands.
6. If unsure, ASK before expanding scope.
7. When in doubt, do less, not more.

Reply with "Ready" when I paste this.
═══════════════════════════════════════════════════════════════

PROJECT: Orders + Cart frontend for E-Commerce platform

BACKEND (via API Gateway at http://localhost:8080/api):

- POST /api/orders → place order (CUSTOMER)
- GET /api/orders/mine → customer's orders (paginated)
- GET /api/orders/seller → seller's incoming orders (paginated)
- GET /api/orders/{id} → single order
- PATCH /api/orders/{id}/status → update status (SELLER)

REQUEST / RESPONSE SHAPES:

CreateOrderRequest:
{
items: [
{ productId: number, quantity: number }
]
}

OrderResponse:
{
id: number,
customerId: number,
totalPrice: number,
status: string, // PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
createdAt: string,
updatedAt: string,
items: [
{
id: number,
productId: number,
productName: string,
productImage: string,
sellerId: number,
unitPrice: number,
quantity: number,
subtotal: number
}
]
}

UpdateOrderStatusRequest:
{
status: "CONFIRMED" | "SHIPPED" | "DELIVERED"
}

PAGINATION: page (default 0), size (default 20), sort (default "createdAt,desc")

STATUS TRANSITIONS (seller can move order forward):

- PENDING → CONFIRMED
- CONFIRMED → SHIPPED
- SHIPPED → DELIVERED
- Any other transition → 400 error

EXISTING FRONTEND SETUP:

- React 18 + Vite + TailwindCSS 3 + React Router v6
- src/api/axios.js → auth-service (http://localhost:8081)
- src/api/productsAxios.js → gateway for products (http://localhost:8080/api)
- src/api/products.js → product API functions
- src/context/AuthContext.jsx → { user, isAuthenticated, hasRole, loading }
- Routes in src/App.jsx
- Navbar in src/components/Navbar.jsx

IMPORTANT — AXIOS INSTANCE:
For orders, use the SAME gateway baseURL as productsAxios: http://localhost:8080/api
You can reuse productsAxios OR create ordersAxios.js.
Preference: create src/api/ordersAxios.js as a SEPARATE instance
(copy the pattern from productsAxios.js), and wire AuthContext to call
setAccessToken on it too (same as it does for axios and productsAxios).

CART DESIGN (client-side only — no cart microservice):

- CartContext with useReducer
- Persists to localStorage key: "ecommerce_cart"
- Cart shape: { items: [{ productId, name, imageUrl, unitPrice, sellerId, quantity }] }
- Actions: ADD_ITEM, REMOVE_ITEM, UPDATE_QUANTITY, CLEAR_CART
- Selectors: totalItems (sum of quantities), totalPrice (sum of subtotals)
- Cart persists across login/logout

STYLING CONVENTIONS:

- Card: bg-white p-6 rounded-lg shadow
- Primary button: bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50
- Secondary button: bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300
- Danger button: bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700
- Input: w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500
- Status badge colors:
  - PENDING: bg-yellow-100 text-yellow-800
  - CONFIRMED: bg-blue-100 text-blue-800
  - SHIPPED: bg-purple-100 text-purple-800
  - DELIVERED: bg-green-100 text-green-800
  - CANCELLED: bg-red-100 text-red-800

ROLES:

- CUSTOMER: browse, add to cart, checkout, view own orders
- SELLER: see incoming orders, update statuses
- ADMIN: (future)

JavaScript only (no TypeScript). Functional components + hooks.
