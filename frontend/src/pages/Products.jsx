import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listProducts, searchProducts } from "../api/products";

export default function Products() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function loadProducts() {
      setLoading(true);
      setError("");

      try {
        const response = searchQuery
          ? await searchProducts(searchQuery, { page, size: 12 })
          : await listProducts({ page, size: 12 });

        if (!cancelled) {
          setProducts(Array.isArray(response?.content) ? response.content : []);
          setTotalPages(response?.totalPages ?? 0);
          setTotalElements(response?.totalElements ?? 0);
          setPage(response?.number ?? page);
        }
      } catch {
        if (!cancelled) {
          setError("Unable to load products. Please try again.");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadProducts();
    return () => {
      cancelled = true;
    };
  }, [page, searchQuery, retryCount]);

  function handleSearch(event) {
    event.preventDefault();
    setPage(0);
    setSearchQuery(searchInput.trim());
  }

  function clearSearch() {
    setSearchInput("");
    setSearchQuery("");
    setPage(0);
  }

  return (
    <main className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6">Browse Products</h1>

      <form
        onSubmit={handleSearch}
        className="flex flex-col sm:flex-row gap-3 mb-8"
      >
        <input
          type="search"
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search products..."
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button
          type="submit"
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
        >
          Search
        </button>
        {searchQuery && (
          <button
            type="button"
            onClick={clearSearch}
            className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300"
          >
            Clear
          </button>
        )}
      </form>

      {loading ? (
        <p className="text-center text-gray-600 py-12" role="status">
          Loading...
        </p>
      ) : error ? (
        <div className="text-center py-12">
          <p className="text-red-600 text-sm mb-4">{error}</p>
          <button
            type="button"
            onClick={() => setRetryCount((count) => count + 1)}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : products.length === 0 ? (
        <p className="text-center text-gray-600 py-12">
          {searchQuery ? "No products match your search" : "No products found"}
        </p>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {products.map((product) => (
              <Link
                key={product.id}
                to={`/products/${product.id}`}
                className="bg-white rounded-lg shadow hover:shadow-lg transition overflow-hidden"
              >
                <div className="aspect-square bg-gray-100 flex items-center justify-center">
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
                <div className="p-6">
                  <h2 className="text-lg font-semibold truncate">
                    {product.name ?? "Unnamed product"}
                  </h2>
                  <p className="text-sm text-gray-500">
                    {product.category ?? "Uncategorized"}
                  </p>
                  <p className="font-semibold mt-3">
                    ${Number(product.price ?? 0).toFixed(2)}
                  </p>
                  {Number(product.stock) === 0 ? (
                    <p className="text-sm text-red-600 mt-2">Out of stock</p>
                  ) : (
                    <p className="text-sm text-gray-600 mt-2">
                      In stock: {product.stock ?? 0}
                    </p>
                  )}
                </div>
              </Link>
            ))}
          </div>

          <nav
            aria-label="Product pages"
            className="flex items-center justify-center gap-4 mt-8"
          >
            <button
              type="button"
              onClick={() =>
                setPage((currentPage) => Math.max(0, currentPage - 1))
              }
              disabled={page === 0}
              className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              Previous
            </button>
            <span>
              Page {page + 1} of {totalPages} ({totalElements} products)
            </span>
            <button
              type="button"
              onClick={() => setPage((currentPage) => currentPage + 1)}
              disabled={page >= totalPages - 1}
              className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              Next
            </button>
          </nav>
        </>
      )}
    </main>
  );
}
