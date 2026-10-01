import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { placeOrder } from "../api/orders";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

const fmt = (n) => `$${Number(n).toFixed(2)}`;

export default function Checkout() {
  const { isAuthenticated, user } = useAuth();
  const { items, totalPrice, clearCart } = useCart();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  if (!isAuthenticated)
    return <Navigate to="/login?returnTo=/checkout" replace />;
  if (user?.role !== "CUSTOMER") return <Navigate to="/" replace />;
  if (items.length === 0 && !success) return <Navigate to="/cart" replace />;

  async function handlePlaceOrder() {
    setSubmitting(true);
    setError(null);

    const payload = {
      items: items.map((item) => ({
        productId: item.productId,
        quantity: item.quantity,
      })),
    };

    try {
      const response = await placeOrder(payload);
      clearCart();
      setSuccess(true);
      navigate(`/orders/${response.id}`);
    } catch (err) {
      setError(err.response?.data?.message || err.message);
      setSubmitting(false);
    }
  }

  return (
    <main className="container mx-auto max-w-3xl px-4 py-8">
      <h1 className="mb-6 text-3xl font-bold">Checkout</h1>

      <section className="mb-6 rounded-lg bg-white p-6 shadow">
        <h2 className="mb-4 text-xl font-semibold">Order Summary</h2>
        <ul className="divide-y divide-gray-200">
          {items.map((item) => (
            <li
              key={item.productId}
              className="flex justify-between gap-4 py-3"
            >
              <span>
                {item.name}{" "}
                <span className="text-gray-600">× {item.quantity}</span>
              </span>
              <span className="shrink-0">
                {fmt(item.unitPrice * item.quantity)}
              </span>
            </li>
          ))}
        </ul>
        <p className="mt-4 border-t pt-4 text-right text-xl font-bold">
          Total: {fmt(totalPrice)}
        </p>
      </section>

      <section className="mb-6 rounded-lg bg-white p-6 shadow">
        <h2 className="mb-2 text-xl font-semibold">Shipping info</h2>
        <p className="italic text-gray-500">
          Shipping address will be collected in a future version.
        </p>
      </section>

      {error && (
        <p className="mb-4 text-red-600" role="alert">
          {error}
        </p>
      )}

      <button
        type="button"
        onClick={handlePlaceOrder}
        disabled={submitting || items.length === 0}
        className="w-full rounded-md bg-blue-600 px-4 py-3 text-white hover:bg-blue-700 disabled:opacity-50"
      >
        {submitting ? "Processing..." : `Place Order — ${fmt(totalPrice)}`}
      </button>

      <Link
        to="/cart"
        className="mt-4 inline-block rounded-md bg-gray-200 px-4 py-2 text-gray-800 hover:bg-gray-300"
      >
        Back to Cart
      </Link>
    </main>
  );
}
