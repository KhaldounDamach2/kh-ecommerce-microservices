import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";

const fmt = (n) => `$${Number(n).toFixed(2)}`;

export default function Cart() {
  const {
    items,
    totalItems,
    totalPrice,
    removeItem,
    updateQuantity,
    clearCart,
  } = useCart();

  function handleClearCart() {
    if (window.confirm("Are you sure you want to clear your cart?")) {
      clearCart();
    }
  }

  return (
    <main className="container mx-auto max-w-4xl px-4 py-8">
      <h1 className="mb-6 text-3xl font-bold">Shopping Cart</h1>

      {items.length === 0 ? (
        <div className="py-12 text-center">
          <p className="mb-5 text-gray-600">Your cart is empty</p>
          <Link
            to="/products"
            className="inline-block rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
          >
            Browse Products
          </Link>
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {items.map((item) => (
              <article
                key={item.productId}
                className="flex flex-col gap-4 rounded-lg bg-white p-4 shadow sm:flex-row sm:items-center"
              >
                {item.imageUrl ? (
                  <img
                    src={item.imageUrl}
                    alt={item.name}
                    className="h-20 w-20 rounded object-cover"
                  />
                ) : (
                  <div
                    className="flex h-20 w-20 shrink-0 items-center justify-center rounded bg-gray-200 text-center text-xs text-gray-500"
                    aria-label="No product image"
                  >
                    No image
                  </div>
                )}

                <div className="min-w-0 flex-1">
                  <h2 className="font-semibold">{item.name}</h2>
                  <p className="text-sm text-gray-600">
                    {fmt(item.unitPrice)} each
                  </p>
                </div>

                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity - 1)
                    }
                    className="h-9 w-9 rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300"
                    aria-label={`Decrease quantity of ${item.name}`}
                  >
                    -
                  </button>
                  <span className="min-w-6 text-center" aria-label="Quantity">
                    {item.quantity}
                  </span>
                  <button
                    type="button"
                    onClick={() =>
                      updateQuantity(item.productId, item.quantity + 1)
                    }
                    className="h-9 w-9 rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300"
                    aria-label={`Increase quantity of ${item.name}`}
                  >
                    +
                  </button>
                </div>

                <p className="min-w-24 text-right font-semibold">
                  {fmt(item.unitPrice * item.quantity)}
                </p>
                <button
                  type="button"
                  onClick={() => removeItem(item.productId)}
                  className="text-sm text-red-600 hover:text-red-700"
                  aria-label={`Remove ${item.name} from cart`}
                >
                  Remove
                </button>
              </article>
            ))}
          </div>

          <section className="mt-6 border-t pt-4">
            <p className="text-gray-600">Total items: {totalItems}</p>
            <p className="mt-1 text-xl font-bold">Total: {fmt(totalPrice)}</p>
            <div className="mt-4 flex flex-col gap-3 sm:flex-row">
              <Link
                to="/products"
                className="rounded-md bg-gray-200 px-4 py-2 text-center text-gray-800 hover:bg-gray-300"
              >
                Continue Shopping
              </Link>
              <Link
                to="/checkout"
                className="rounded-md bg-blue-600 px-4 py-2 text-center text-white hover:bg-blue-700"
              >
                Proceed to Checkout
              </Link>
              <button
                type="button"
                onClick={handleClearCart}
                className="rounded-md bg-red-600 px-3 py-2 text-sm text-white hover:bg-red-700"
              >
                Clear Cart
              </button>
            </div>
          </section>
        </>
      )}
    </main>
  );
}
