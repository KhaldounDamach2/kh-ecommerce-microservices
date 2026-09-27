import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const dashboardByRole = {
  CUSTOMER: '/dashboard/customer',
  SELLER: '/dashboard/seller',
  ADMIN: '/dashboard/admin',
};

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <nav className="bg-white shadow px-4 py-3 flex justify-between items-center">
      <Link to="/" className="font-semibold">
        E-Commerce
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
            <span className="rounded bg-gray-100 px-2 py-1 text-sm">
              {user.role}
            </span>
            {dashboardByRole[user.role] && (
              <Link to={dashboardByRole[user.role]}>Dashboard</Link>
            )}
            <button type="button" onClick={handleLogout}>
              Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
}