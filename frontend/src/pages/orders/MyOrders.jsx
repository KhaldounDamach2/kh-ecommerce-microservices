import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { getMyOrders } from "../../api/orders";
import { useAuth } from "../../context/AuthContext";

const statusColor = {
  PENDING: "bg-yellow-100 text-yellow-800",
  CONFIRMED: "bg-blue-100 text-blue-800",
  SHIPPED: "bg-purple-100 text-purple-800",
  DELIVERED: "bg-green-100 text-green-800",
  CANCELLED: "bg-red-100 text-red-800",
};

const fmt = (n) => `$${Number(n ?? 0).toFixed(2)}`;
const fmtDate = (iso) =>
  new Date(iso).toLocaleString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  });

function StatusBadge({ status }) {
  return (
    <span
      className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${statusColor[status] || "bg-gray-100 text-gray-800"}`}
    >
      {status || "UNKNOWN"}
    </span>
  );
}

export default function MyOrders() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    if (authLoading || !isAuthenticated || user?.role !== "CUSTOMER") return;

    let cancelled = false;

    async function loadOrders() {
      setLoading(true);
      setError("");

      try {
        const response = await getMyOrders({ page, size: 10 });
        if (cancelled) return;
        const content = Array.isArray(response?.content)
          ? response.content
          : [];
        setOrders(content);
        setTotalPages(Number(response?.totalPages ?? 0));
        setTotalElements(Number(response?.totalElements ?? content.length));
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError?.response?.data?.message ||
              requestError?.message ||
              "Unable to load your orders. Please try again.",
          );
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadOrders();
    return () => {
      cancelled = true;
    };
  }, [authLoading, isAuthenticated, user?.role, page, retryCount]);

  if (authLoading) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl">
        <p className="text-center text-gray-600" role="status">
          Loading orders...
        </p>
      </main>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "CUSTOMER") return <Navigate to="/" replace />;

  const retry = () => setRetryCount((count) => count + 1);

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      <h1 className="text-3xl font-bold mb-6">My Orders</h1>

      {loading ? (
        <p className="text-center text-gray-600 py-12" role="status">
          Loading orders...
        </p>
      ) : error ? (
        <div className="text-center py-8">
          <p className="text-red-600 mb-4" role="alert">
            {error}
          </p>
          <button
            type="button"
            onClick={retry}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 mb-4">
            You haven't placed any orders yet
          </p>
          <Link
            to="/products"
            className="inline-block bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Browse Products
          </Link>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {orders.map((order) => {
              const items = Array.isArray(order?.items) ? order.items : [];
              const remainingItems = Math.max(items.length - 2, 0);

              return (
                <article
                  key={order?.id}
                  className="bg-white p-6 rounded-lg shadow"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                    <div>
                      <div className="flex flex-wrap items-center gap-3">
                        <h2 className="font-semibold">
                          Order #{order?.id ?? "-"}
                        </h2>
                        <StatusBadge status={order?.status} />
                      </div>
                      <p className="text-sm text-gray-600 mt-1">
                        {order?.createdAt
                          ? fmtDate(order.createdAt)
                          : "Date unavailable"}
                      </p>
                    </div>
                    <p className="font-bold">{fmt(order?.totalPrice)}</p>
                  </div>

                  <div className="mt-4 space-y-2 text-sm text-gray-700">
                    {items.slice(0, 2).map((item, index) => (
                      <div
                        key={
                          item?.id ?? `${item?.productId ?? "item"}-${index}`
                        }
                        className="flex justify-between gap-4"
                      >
                        <span>
                          {item?.productName ?? "Product"} ×{" "}
                          {item?.quantity ?? 0}
                        </span>
                        <span className="shrink-0">{fmt(item?.subtotal)}</span>
                      </div>
                    ))}
                    {remainingItems > 0 && (
                      <p className="text-gray-500">
                        +{remainingItems} more items
                      </p>
                    )}
                  </div>

                  <div className="mt-5 border-t pt-4">
                    <Link
                      to={`/orders/${order?.id}`}
                      className="text-blue-600 hover:text-blue-800 font-medium"
                    >
                      View Details
                    </Link>
                  </div>
                </article>
              );
            })}
          </div>

          <div className="flex flex-wrap items-center justify-center gap-4 mt-6">
            <button
              type="button"
              disabled={page === 0}
              onClick={() =>
                setPage((currentPage) => Math.max(currentPage - 1, 0))
              }
              className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              Previous
            </button>
            <span aria-live="polite">
              Page {page + 1} of {Math.max(totalPages, 1)}
              <span className="sr-only">, {totalElements} total orders</span>
            </span>
            <button
              type="button"
              disabled={totalPages === 0 || page >= totalPages - 1}
              onClick={() => setPage((currentPage) => currentPage + 1)}
              className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </>
      )}
    </main>
  );
}
