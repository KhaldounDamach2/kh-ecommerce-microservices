import { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { getOrder } from "../../api/orders";
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

function StatusBadge({ status, large = false }) {
  return (
    <span
      className={`inline-block px-2 py-1 rounded-full text-xs font-medium ${large ? "text-sm px-3" : ""} ${statusColor[status] || "bg-gray-100 text-gray-800"}`}
    >
      {status || "UNKNOWN"}
    </span>
  );
}

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    if (authLoading || !isAuthenticated) return;

    let cancelled = false;

    async function loadOrder() {
      setLoading(true);
      setError(null);

      try {
        const response = await getOrder(id);
        if (cancelled) return;
        if (!response) {
          setOrder(null);
          setError(404);
          return;
        }
        setOrder(response);
      } catch (requestError) {
        if (!cancelled) {
          setOrder(null);
          setError(requestError?.response?.status ?? "generic");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadOrder();
    return () => {
      cancelled = true;
    };
  }, [authLoading, id, isAuthenticated, retryCount]);

  if (authLoading) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl">
        <p className="text-center text-gray-600" role="status">
          Loading order...
        </p>
      </main>
    );
  }
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  const retry = () => setRetryCount((count) => count + 1);
  const backLink = (
    <button
      type="button"
      onClick={() => navigate(-1)}
      className="text-blue-600 hover:text-blue-800 mb-4"
    >
      ← Back
    </button>
  );

  if (loading) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl">
        {backLink}
        <p className="text-center text-gray-600 py-12" role="status">
          Loading order...
        </p>
      </main>
    );
  }

  if (error === 404) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl text-center">
        <div className="text-left">{backLink}</div>
        <h1 className="text-2xl font-bold mb-4">Order not found</h1>
        <Link to="/orders" className="text-blue-600 hover:underline">
          Back to orders
        </Link>
      </main>
    );
  }

  if (error === 403) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl text-center">
        <div className="text-left">{backLink}</div>
        <h1 className="text-2xl font-bold mb-4">
          You don't have access to this order
        </h1>
        <Link to="/" className="text-blue-600 hover:underline">
          Back to home
        </Link>
      </main>
    );
  }

  if (error || !order) {
    return (
      <main className="container mx-auto px-4 py-8 max-w-4xl text-center">
        <div className="text-left">{backLink}</div>
        <p className="text-red-600 mb-4" role="alert">
          Unable to load this order. Please try again.
        </p>
        <button
          type="button"
          onClick={retry}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
        >
          Retry
        </button>
      </main>
    );
  }

  const items = Array.isArray(order.items) ? order.items : [];

  return (
    <main className="container mx-auto px-4 py-8 max-w-4xl">
      {backLink}
      <header className="mb-8">
        <div className="flex flex-wrap items-center gap-3 mb-2">
          <h1 className="text-3xl font-bold">Order #{order.id ?? id}</h1>
          <StatusBadge status={order.status} large />
        </div>
        <p className="text-gray-600">
          Placed on{" "}
          {order.createdAt ? fmtDate(order.createdAt) : "Date unavailable"}
        </p>
      </header>

      <section className="bg-white p-6 rounded-lg shadow mb-6">
        <h2 className="font-semibold mb-2">Customer information</h2>
        <p className="text-gray-700">
          Order placed by customer #{order.customerId ?? "Unknown"}
        </p>
      </section>

      <section className="bg-white rounded-lg shadow overflow-hidden">
        <h2 className="text-lg font-semibold p-6 pb-4">Items</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {["Image", "Product", "Unit Price", "Quantity", "Subtotal"].map(
                  (heading) => (
                    <th
                      key={heading}
                      scope="col"
                      className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase"
                    >
                      {heading}
                    </th>
                  ),
                )}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((item, index) => (
                <tr key={item?.id ?? `${item?.productId ?? "item"}-${index}`}>
                  <td className="px-4 py-4">
                    {item?.productImage ? (
                      <img
                        src={item.productImage}
                        alt={item?.productName ?? ""}
                        className="w-16 h-16 object-cover rounded"
                      />
                    ) : (
                      <div
                        className="w-16 h-16 bg-gray-100 rounded flex items-center justify-center text-xs text-gray-500"
                        aria-label="No product image"
                      >
                        No image
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-4 font-medium">
                    <Link
                      to={`/products/${item?.productId ?? ""}`}
                      className="text-blue-600 hover:text-blue-800"
                    >
                      {item?.productName ?? "Product"}
                    </Link>
                  </td>
                  <td className="px-4 py-4 text-gray-700">
                    {fmt(item?.unitPrice)}
                  </td>
                  <td className="px-4 py-4 text-gray-700">
                    {item?.quantity ?? 0}
                  </td>
                  <td className="px-4 py-4 text-right font-bold">
                    {fmt(item?.subtotal)}
                  </td>
                </tr>
              ))}
              {items.length === 0 && (
                <tr>
                  <td
                    colSpan={5}
                    className="px-4 py-6 text-center text-gray-500"
                  >
                    No items in this order.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <div className="border-t p-6 text-right">
          <p className="text-xl font-bold">Total: {fmt(order.totalPrice)}</p>
        </div>
      </section>

      <section className="mt-6 text-sm text-gray-600">
        <div className="flex items-center gap-2">
          <span>Current status:</span>
          <StatusBadge status={order.status} />
        </div>
        <p className="mt-2">
          Sellers can update order status from the seller page.
        </p>
      </section>
    </main>
  );
}
