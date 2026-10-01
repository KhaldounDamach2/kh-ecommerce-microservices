import { useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { getSellerOrders, updateOrderStatus } from "../../api/orders";
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
      className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${statusColor[status] || "bg-gray-100 text-gray-800"}`}
    >
      {status || "UNKNOWN"}
    </span>
  );
}

export default function IncomingOrders() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [updatingOrderId, setUpdatingOrderId] = useState(null);
  const [updateError, setUpdateError] = useState("");
  const [updateErrorOrderId, setUpdateErrorOrderId] = useState(null);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    if (authLoading || !isAuthenticated || user?.role !== "SELLER") return;

    let cancelled = false;

    async function loadOrders() {
      setLoading(true);
      setError("");

      try {
        const response = await getSellerOrders({ page, size: 10 });
        if (cancelled) return;
        setOrders(Array.isArray(response?.content) ? response.content : []);
        setTotalPages(Number(response?.totalPages ?? 0));
      } catch (requestError) {
        if (!cancelled) {
          setError(
            requestError?.response?.data?.message ||
              requestError?.message ||
              "Unable to load incoming orders. Please try again.",
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
      <main className="container mx-auto px-4 py-8 max-w-5xl">
        <p className="text-center text-gray-600" role="status">
          Loading orders...
        </p>
      </main>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== "SELLER") return <Navigate to="/" replace />;

  const reloadOrders = async () => {
    const response = await getSellerOrders({ page, size: 10 });
    setOrders(Array.isArray(response?.content) ? response.content : []);
    setTotalPages(Number(response?.totalPages ?? 0));
  };

  const handleStatusUpdate = async (order) => {
    const nextStatus = {
      PENDING: "CONFIRMED",
      CONFIRMED: "SHIPPED",
      SHIPPED: "DELIVERED",
    }[order?.status];
    if (!nextStatus) return;

    setUpdateError("");
    setUpdateErrorOrderId(null);
    setUpdatingOrderId(order.id);

    try {
      await updateOrderStatus(order.id, nextStatus);
      await reloadOrders();
    } catch (requestError) {
      setUpdateError(
        requestError?.response?.data?.message ||
          requestError?.message ||
          "Unable to update this order. Please try again.",
      );
      setUpdateErrorOrderId(order.id);
    } finally {
      setUpdatingOrderId(null);
    }
  };

  return (
    <main className="container mx-auto px-4 py-8 max-w-5xl">
      <h1 className="text-3xl font-bold mb-6">Incoming Orders</h1>
      <p className="text-gray-600 mb-6">Orders containing your products</p>

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
            onClick={() => setRetryCount((count) => count + 1)}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 mb-2">You have no incoming orders yet</p>
          <p className="text-gray-500 mb-4">
            Orders containing your products will appear here.
          </p>
          <Link
            to="/products"
            className="inline-block bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300"
          >
            Browse Products
          </Link>
        </div>
      ) : (
        <>
          <div>
            {orders.map((order) => {
              const items = (
                Array.isArray(order?.items) ? order.items : []
              ).filter((item) => item?.sellerId === user?.id);
              const sellerSubtotal = items.reduce(
                (total, item) => total + Number(item?.subtotal ?? 0),
                0,
              );
              const nextStatus = {
                PENDING: "CONFIRMED",
                CONFIRMED: "SHIPPED",
                SHIPPED: "DELIVERED",
              }[order?.status];
              const actionLabel = {
                PENDING: "Confirm Order",
                CONFIRMED: "Mark as Shipped",
                SHIPPED: "Mark as Delivered",
              }[order?.status];
              const isUpdating = updatingOrderId === order?.id;

              return (
                <article
                  key={order?.id}
                  className="bg-white p-5 rounded-lg shadow mb-4"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:justify-between sm:items-start">
                    <div>
                      <h2 className="text-lg font-semibold">
                        Order #{order?.id ?? "-"}
                      </h2>
                      <p className="text-sm text-gray-500">
                        Placed on{" "}
                        {order?.createdAt
                          ? fmtDate(order.createdAt)
                          : "Date unavailable"}
                      </p>
                      <p className="text-sm text-gray-500">
                        Customer #{order?.customerId ?? "-"}
                      </p>
                    </div>
                    <div className="flex flex-wrap items-center gap-3 sm:justify-end">
                      <StatusBadge status={order?.status} />
                      <div className="text-right">
                        <p className="text-lg font-semibold">
                          Your items: {fmt(sellerSubtotal)}
                        </p>
                        <p className="text-xs text-gray-400">
                          Order total: {fmt(order?.totalPrice)}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="mt-4 border-t pt-4 space-y-3">
                    {items.map((item, index) => (
                      <div
                        key={
                          item?.id ?? `${item?.productId ?? "item"}-${index}`
                        }
                        className="flex items-center gap-3"
                      >
                        {item?.productImage ? (
                          <img
                            src={item.productImage}
                            alt={item?.productName || ""}
                            className="w-12 h-12 object-cover rounded"
                          />
                        ) : (
                          <div
                            className="w-12 h-12 rounded bg-gray-100 flex-shrink-0"
                            aria-hidden="true"
                          />
                        )}
                        <div className="min-w-0 flex-1">
                          <p className="font-medium truncate">
                            {item?.productName ?? "Product"}
                          </p>
                          <p className="text-sm text-gray-500">
                            x {item?.quantity ?? 0} · {fmt(item?.unitPrice)}
                          </p>
                        </div>
                        <p className="font-semibold shrink-0">
                          {fmt(item?.subtotal)}
                        </p>
                      </div>
                    ))}
                  </div>

                  <div className="mt-4 flex gap-2 flex-wrap">
                    <Link
                      to={`/orders/${order?.id}`}
                      className="inline-block bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300"
                    >
                      View Full Order
                    </Link>
                    {nextStatus ? (
                      <button
                        type="button"
                        onClick={() => handleStatusUpdate(order)}
                        disabled={isUpdating}
                        className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
                      >
                        {isUpdating ? "Updating..." : actionLabel}
                      </button>
                    ) : (
                      <span className="py-2 text-gray-500">Order complete</span>
                    )}
                  </div>
                  {updateError && updateErrorOrderId === order?.id && (
                    <p className="mt-2 text-sm text-red-600" role="alert">
                      {updateError}
                    </p>
                  )}
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
