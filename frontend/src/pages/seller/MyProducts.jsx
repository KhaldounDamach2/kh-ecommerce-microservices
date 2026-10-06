import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { deleteProduct, getMyProducts } from "../../api/products";
import { useAuth } from "../../context/AuthContext";

const pageSize = 10;

export default function MyProducts() {
  const { hasRole, loading: authLoading } = useAuth();
  const isSeller = hasRole("SELLER");
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    if (!authLoading && !isSeller) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isSeller, navigate]);

  const loadProducts = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await getMyProducts({ page, size: pageSize });
      setProducts(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (requestError) {
      setError(requestError.response?.data?.message || requestError.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!authLoading && isSeller) {
      loadProducts();
    }
  }, [authLoading, isSeller, page]);

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this product?")) return;

    try {
      await deleteProduct(id);
      if (products.length === 1 && page > 0) {
        setPage((currentPage) => currentPage - 1);
      } else {
        loadProducts();
      }
    } catch (requestError) {
      setError(requestError.response?.data?.message || requestError.message);
    }
  };

  if (authLoading || !isSeller) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
        <h1 className="text-3xl font-bold">My Products</h1>
        <Link
          to="/seller/products/new"
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-center"
        >
          Add New Product
        </Link>
      </div>

      {loading ? (
        <p className="text-center text-gray-600">Loading...</p>
      ) : error ? (
        <div className="text-center">
          <p className="text-red-600 text-sm mb-4" role="alert">
            {error}
          </p>
          <button
            type="button"
            onClick={loadProducts}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Retry
          </button>
        </div>
      ) : products.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 mb-4">
            You haven't added any products yet
          </p>
          <Link
            to="/seller/products/new"
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            Add New Product
          </Link>
        </div>
      ) : (
        <>
          <div className="overflow-x-auto bg-white rounded-lg shadow">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  {[
                    "Image",
                    "Name",
                    "Category",
                    "Price",
                    "Stock",
                    "Actions",
                  ].map((heading) => (
                    <th
                      key={heading}
                      className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
                    >
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {products.map((product) => (
                  <tr key={product.id}>
                    <td className="px-6 py-4">
                      {product.imageUrl ? (
                        <img
                          src={product.imageUrl}
                          alt={product.name}
                          className="h-12 w-12 object-cover rounded"
                        />
                      ) : (
                        <div
                          className="h-12 w-12 bg-gray-100 rounded"
                          aria-label="No image"
                        />
                      )}
                    </td>
                    <td className="px-6 py-4 font-medium text-gray-900">
                      {product.name}
                    </td>
                    <td className="px-6 py-4 text-gray-600">
                      {product.category || "-"}
                    </td>
                    <td className="px-6 py-4 text-gray-600">
                      ${Number(product.price).toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-gray-600">{product.stock}</td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <Link
                          to={`/seller/products/${product.id}/edit`}
                          className="text-blue-600 hover:text-blue-800"
                        >
                          Edit
                        </Link>
                        <button
                          type="button"
                          onClick={() => handleDelete(product.id)}
                          className="text-red-600 hover:text-red-800"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-center gap-4 mt-6">
            <button
              type="button"
              disabled={page === 0}
              onClick={() => setPage((currentPage) => currentPage - 1)}
              className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              Previous
            </button>
            <span>
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
    </div>
  );
}
