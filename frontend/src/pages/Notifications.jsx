import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  announceNotificationsChanged,
  getMyNotifications,
  markAllAsRead,
  markAsRead,
} from "../api/notifications";

const typeStyle = {
  STOCK_LOW: "bg-yellow-100 text-yellow-800",
  STOCK_DEPLETED: "bg-red-100 text-red-800",
  // Same colors as the order status badges
  ORDER_CONFIRMED: "bg-blue-100 text-blue-800",
  ORDER_SHIPPED: "bg-purple-100 text-purple-800",
  ORDER_DELIVERED: "bg-green-100 text-green-800",
  ORDER_CANCELLED: "bg-red-100 text-red-800",
};

const typeLabel = {
  STOCK_LOW: "Low stock",
  STOCK_DEPLETED: "Sold out",
  ORDER_CONFIRMED: "Order confirmed",
  ORDER_SHIPPED: "Order shipped",
  ORDER_DELIVERED: "Order delivered",
  ORDER_CANCELLED: "Order cancelled",
};

const fmtDate = (iso) =>
  new Date(iso).toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;

    getMyNotifications({ page: 0, size: 50 })
      .then((response) => {
        if (!cancelled) {
          setNotifications(Array.isArray(response?.content) ? response.content : []);
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(
            requestError?.response?.data?.message ||
              "Unable to load notifications. Please try again.",
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Mark as read, then an order notification opens its order.
  const handleClick = async (notification) => {
    if (!notification.read) {
      await markAsRead(notification.id);
      setNotifications((current) =>
        current.map((n) => (n.id === notification.id ? { ...n, read: true } : n)),
      );
      announceNotificationsChanged();
    }
    if (notification.orderId) {
      navigate(`/orders/${notification.orderId}`);
    }
  };

  const handleMarkAllAsRead = async () => {
    await markAllAsRead();
    setNotifications((current) => current.map((n) => ({ ...n, read: true })));
    announceNotificationsChanged();
  };

  const unread = notifications.filter((n) => !n.read).length;

  return (
    <main className="container mx-auto px-4 py-8 max-w-3xl">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold">Notifications</h1>
        {unread > 0 && (
          <button
            type="button"
            onClick={handleMarkAllAsRead}
            className="border border-gray-300 px-3 py-1 rounded hover:bg-gray-100"
          >
            Mark all as read ({unread})
          </button>
        )}
      </div>

      {loading ? (
        <p className="text-center text-gray-600 py-12" role="status">
          Loading notifications...
        </p>
      ) : error ? (
        <p className="text-center text-red-600 py-12" role="alert">
          {error}
        </p>
      ) : notifications.length === 0 ? (
        <p className="text-center text-gray-600 py-12">No notifications yet.</p>
      ) : (
        <ul className="space-y-3">
          {notifications.map((n) => (
            <li
              key={n.id}
              onClick={() => handleClick(n)}
              className={`p-4 rounded-lg border cursor-pointer ${
                n.read ? "bg-white border-gray-200" : "bg-blue-50 border-blue-200"
              }`}
            >
              <div className="flex items-center justify-between mb-1">
                <span
                  className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${
                    typeStyle[n.type] || "bg-gray-100 text-gray-800"
                  }`}
                >
                  {typeLabel[n.type] || n.type}
                </span>
                <span className="text-sm text-gray-500">{fmtDate(n.createdAt)}</span>
              </div>
              <p className={n.read ? "text-gray-700" : "font-semibold"}>{n.title}</p>
              <p className="text-gray-600">{n.message}</p>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
