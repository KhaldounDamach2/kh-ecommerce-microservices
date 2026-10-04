import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

const dashboardByRole = {
  CUSTOMER: "/dashboard/customer",
  SELLER: "/dashboard/seller",
  ADMIN: "/dashboard/admin",
};

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { totalItems, clearCart } = useCart();
  const navigate = useNavigate();

  async function handleLogout() {
    clearCart();
    await logout();
    navigate("/login");
  }

  return (
    <nav className="bg-white shadow px-4 py-3 flex justify-between items-center">
      <Link to="/" className="font-semibold">
        E-Commerce
      </Link>
      <Link to="/products" className="text-gray-700 hover:text-blue-600">
        Products
      </Link>
      <Link to="/cart" className="text-gray-700 hover:text-blue-600">
        Cart {totalItems > 0 ? `(${totalItems})` : ""}
      </Link>

      <div className="flex items-center gap-4">
        {!isAuthenticated ? (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        ) : (
          <>
            <span>{user.email}</span>
            <span
              className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                user.role === "CUSTOMER"
                  ? "bg-blue-100 text-blue-800"
                  : user.role === "SELLER"
                    ? "bg-green-100 text-green-800"
                    : "bg-red-100 text-red-800"
              }`}
            >
              {user.role}
            </span>
            {user.role === "SELLER" && (
              <>
                <Link
                  to="/seller/products"
                  className="text-gray-700 hover:text-blue-600"
                >
                  My Products
                </Link>
                <Link
                  to="/seller/orders"
                  className="text-gray-700 hover:text-blue-600"
                >
                  Incoming Orders
                </Link>
              </>
            )}
            {user.role === "CUSTOMER" && (
              <Link
                to="/orders/mine"
                className="text-gray-700 hover:text-blue-600"
              >
                My Orders
              </Link>
            )}
            {dashboardByRole[user.role] && (
              <Link
                to={dashboardByRole[user.role]}
                className="hover:text-blue-600"
              >
                Dashboard
              </Link>
            )}
            <button
              type="button"
              onClick={handleLogout}
              className="border border-gray-300 px-3 py-1 rounded hover:bg-gray-100"
            >
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
