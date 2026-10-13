import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getUnreadCount, NOTIFICATIONS_CHANGED } from "../api/notifications";

const POLL_INTERVAL_MS = 30_000;

export default function NotificationBell() {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const refresh = () => {
      getUnreadCount()
        .then(setCount)
        .catch(() => {});
    };

    refresh();
    const timer = setInterval(refresh, POLL_INTERVAL_MS);
    window.addEventListener(NOTIFICATIONS_CHANGED, refresh);

    return () => {
      clearInterval(timer);
      window.removeEventListener(NOTIFICATIONS_CHANGED, refresh);
    };
  }, []);

  return (
    <Link
      to="/notifications"
      className="relative text-xl"
      aria-label={`Notifications (${count} unread)`}
    >
      🔔
      {count > 0 && (
        <span className="absolute -top-2 -right-3 bg-red-600 text-white text-xs font-bold rounded-full px-1.5">
          {count}
        </span>
      )}
    </Link>
  );
}
