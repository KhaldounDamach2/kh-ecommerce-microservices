═══════════════════════════════════════════════════════════════
WORKING AGREEMENT — READ THIS FIRST
═══════════════════════════════════════════════════════════════
You are assisting with a step-by-step learning project. Follow these rules strictly:

1. DO NOT act autonomously. Wait for explicit tasks.
2. When I give a task, generate ONLY the files I list. Do NOT create extra files.
3. Do NOT modify any file I didn't explicitly mention.
4. After generating code, ACTUALLY CREATE the files on disk using your tools.
   Do not just show code in chat.
5. Do NOT run build/install commands without my approval.
   The dev server is already running.
6. Do NOT suggest architecture changes or "improvements" unless I ask.
7. If a task seems incomplete, ASK before expanding scope.
8. Keep responses concise — code + short explanations only.

If you're unsure whether to create/modify a file, ASK first.
When in doubt, do less, not more.

Reply with "Understood" when I paste this context, then wait for my task.
═══════════════════════════════════════════════════════════════

PROJECT: E-Commerce Microservices Platform — React Frontend
═══════════════════════════════════════════════════════════════

BACKEND:

- Auth Service: http://localhost:8081 (JWT auth, roles: CUSTOMER, SELLER, ADMIN)
- API Gateway: http://localhost:8080 (routes /api/\*\* to services)
- All auth endpoints currently hit auth-service directly for dev

TECH STACK:

- React 18 + Vite 8 (JavaScript, not TypeScript)
- React Router v6 (client-side routing)
- Axios (HTTP client with interceptors)
- TailwindCSS 3 (utility-first styling)
- Context API for global auth state (no Redux/Zustand)

BACKEND API ENDPOINTS (auth-service):

- POST /auth/register {email, password, confirmPassword, role}
  → 201 UserResponse {id, email, role, enabled}
- POST /auth/confirm {token}
  → 200 {message: "Email confirmed successfully"}
- POST /auth/login {email, password}
  → 200 AuthResponse {
  accessToken, refreshToken, tokenType,
  expiresIn, user: {id, email, role, enabled}
  }
- POST /auth/refresh {refreshToken}
  → 200 AuthResponse (new accessToken)
- POST /auth/logout {refreshToken}
  → 200 {message: "Logged out"}

REGISTRATION FLOW (in UI):

1. User fills register form (email, password, confirmPassword, role)
2. POST /auth/register → success message shown
3. User receives confirmation email (Mailtrap)
4. User clicks link: http://localhost:5173/confirm?token=xxx
5. Frontend auto-calls POST /auth/confirm {token}
6. Success → redirect to /login
7. User logs in with email + password
8. JWT tokens stored (access in memory, refresh in localStorage)
9. Redirect to role-specific dashboard

ROLES & DASHBOARDS:

- CUSTOMER → /dashboard/customer
- SELLER → /dashboard/seller
- ADMIN → /dashboard/admin

PAGES:

- Home / (public landing)
- Register /register (public)
- Login /login (public)
- ConfirmEmail /confirm?token=xxx (public, auto-confirms)
- Customer /dashboard/customer (protected: CUSTOMER)
- Seller /dashboard/seller (protected: SELLER)
- Admin /dashboard/admin (protected: ADMIN)
- NotFound \* (404)

AUTH STRATEGY:

- Access token: stored in memory (React state / Context)
- Refresh token: stored in localStorage
- Axios request interceptor: attach "Authorization: Bearer <accessToken>"
- Axios response interceptor: on 401 → try POST /auth/refresh → retry original
- On refresh failure → clear tokens → redirect to /login
- AuthContext exposes: { user, accessToken, isAuthenticated, login, logout, register }

FOLDER STRUCTURE:
frontend/src/
├── api/
│ ├── axios.js (Axios instance + interceptors)
│ └── auth.js (API functions: register, login, confirm, refresh, logout)
├── components/
│ ├── Navbar.jsx
│ ├── Layout.jsx
│ └── ProtectedRoute.jsx
├── context/
│ └── AuthContext.jsx
├── pages/
│ ├── Home.jsx
│ ├── Register.jsx
│ ├── Login.jsx
│ ├── ConfirmEmail.jsx
│ ├── CustomerDashboard.jsx
│ ├── SellerDashboard.jsx
│ ├── AdminDashboard.jsx
│ └── NotFound.jsx
├── App.jsx (Routes)
├── main.jsx (AuthProvider wrapper)
└── index.css (Tailwind directives)

CONVENTIONS:

- Functional components + hooks (no class components)
- Named exports preferred (except pages = default export)
- Tailwind classes for ALL styling (no CSS files except index.css)
- Form validation client-side + show server errors
- Loading states for async operations
- Error messages displayed near the form
- React Router v6 useNavigate for redirects after auth actions

BASE URL:

- Vite dev proxy not needed — hit backend directly at http://localhost:8081
- (Later we'll switch to http://localhost:8080/api via gateway)

═══════════════════════════════════════════════════════════════
TASK TEMPLATE I WILL USE
═══════════════════════════════════════════════════════════════
TASK: <description>
SCOPE: ONLY generate these files:

- <file1>
- <file2>
  DO NOT create, modify, or run anything else.
  After generating, CREATE the files on disk.

Wait for me to send the TASK. Acknowledge this format with "Ready."
