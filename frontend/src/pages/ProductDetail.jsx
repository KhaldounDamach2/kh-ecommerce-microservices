import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getProduct } from "../api/products";

export default function ProductDetail() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadProduct() {
      setLoading(true);
      setError("");

      try {
        const response = await getProduct(id);
        if (!cancelled) {
          setProduct(response ?? null);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err?.response?.status === 404 ? "not-found" : "generic");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadProduct();
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) {
    return (
      <main className="max-w-5xl mx-auto px-4 py-8">
        <p className="text-center text-gray-600 py-12" role="status">
          Loading product...
        </p>
      </main>
    );
  }

  if (error === "not-found") {
    return (
      <main className="max-w-5xl mx-auto px-4 py-8 text-center">
        <h1 className="text-2xl font-bold mb-4">Product not found</h1>
        <Link to="/products" className="text-blue-600 hover:underline">
          Back to products
        </Link>
      </main>
    );
  }

  if (error || !product) {
    return (
      <main className="max-w-5xl mx-auto px-4 py-8 text-center">
        <p className="text-red-600 text-sm mb-4">
          Unable to load this product. Please try again later.
        </p>
        <Link to="/products" className="text-blue-600 hover:underline">
          Back to products
        </Link>
      </main>
    );
  }

  const inStock = Number(product.stock ?? 0) > 0;

  return (
    <main className="max-w-5xl mx-auto px-4 py-8">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="aspect-square bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden">
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name ?? "Product"}
              className="object-cover w-full h-full"
            />
          ) : (
            <span className="text-gray-500">No image</span>
          )}
        </div>

        <section>
          <Link
            to="/products"
            className="text-blue-600 text-sm mb-2 inline-block"
          >
            ← Back to products
          </Link>
          <h1 className="text-3xl font-bold mb-2">
            {product.name ?? "Unnamed product"}
          </h1>
          <span className="bg-gray-100 text-gray-700 px-3 py-1 rounded-full text-sm inline-block mb-4">
            {product.category ?? "Uncategorized"}
          </span>
          <p className="text-2xl font-semibold text-blue-600 mb-4">
            ${Number(product.price ?? 0).toFixed(2)}
          </p>
          <p className={`mb-6 ${inStock ? "text-green-600" : "text-red-600"}`}>
            {inStock ? `In stock: ${product.stock}` : "Out of stock"}
          </p>
          <p className="text-gray-700 leading-relaxed mb-6">
            {product.description ?? "No description available."}
          </p>
          <p className="text-sm text-gray-500">
            Sold by seller #{product.sellerId ?? "Unknown"}
          </p>
        </section>
      </div>
    </main>
  );
}
