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

PROJECT: Products frontend for E-Commerce platform

BACKEND (all via API Gateway at http://localhost:8080):
Product endpoints (all under /api/products):

- GET /api/products → paginated list (public)
- GET /api/products/{id} → single product (public)
- GET /api/products/search?q=... → search (public)
- GET /api/products/mine → seller's own products (SELLER only)
- POST /api/products → create (SELLER only)
- PUT /api/products/{id} → update (SELLER owns it)
- DELETE /api/products/{id} → soft delete (SELLER owns it)

PAGINATION QUERY PARAMS:

- page (default 0)
- size (default 20)
- sort (default "createdAt,desc") — format: "field,direction"

PRODUCT RESPONSE SHAPES:

ProductSummaryResponse (used in list endpoints):
{
id: number,
name: string,
price: number,
stock: number,
category: string,
imageUrl: string,
sellerId: number
}

ProductResponse (used in single/create/update):
{
id: number,
name: string,
description: string,
price: number,
stock: number,
category: string,
imageUrl: string,
sellerId: number,
active: boolean,
createdAt: string,
updatedAt: string
}

Page response shape (Spring Data):
{
content: [ ...items ],
pageable: { pageNumber, pageSize, ... },
totalElements: number,
totalPages: number,
first: boolean,
last: boolean,
size: number,
number: number
}

EXISTING FRONTEND SETUP:

- React 18 + Vite + TailwindCSS 3 + React Router v6
- Axios instance at src/api/axios.js (baseURL: http://localhost:8081,
  BUT for products we need http://localhost:8080/api — see note below)
- AuthContext provides: { user, isAuthenticated, hasRole }
- Pages live in src/pages/
- Components in src/components/

IMPORTANT — AXIOS BASE URL:
The existing axios.js targets http://localhost:8081 (auth-service direct).
For products, requests must go through the API Gateway: http://localhost:8080/api
So when creating the products API layer:

- Either create a SEPARATE axios instance for products
- Or use full URLs in products API functions
- Do NOT modify the existing axios.js (would break auth)

RECOMMENDED: create src/api/products.js that imports a small dedicated
axios instance OR uses full URLs. Your choice, but be consistent.

STYLING CONVENTIONS:

- Card: bg-white p-6 rounded-lg shadow hover:shadow-md transition
- Primary button: bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50
- Secondary button: bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300
- Input: w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500
- Error: text-red-600 text-sm
- Loading: centered spinner or "Loading..." text
- Grid layout for products: grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6

ROLES:

- CUSTOMER: can browse products, place orders (future)
- SELLER: can browse + CRUD own products
- ADMIN: can manage everything (future)

Use useAuth() for role checks. Use useNavigate for redirects.
JavaScript only (no TypeScript).
