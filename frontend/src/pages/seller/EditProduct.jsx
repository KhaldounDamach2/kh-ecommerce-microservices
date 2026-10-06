import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import ProductForm from "../../components/ProductForm";
import { getProduct, updateProduct } from "../../api/products";
import { useAuth } from "../../context/AuthContext";

export default function EditProduct() {
  const { hasRole, loading: authLoading } = useAuth();
  const isSeller = hasRole("SELLER");
  const navigate = useNavigate();
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!authLoading && !isSeller) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isSeller, navigate]);

  useEffect(() => {
    if (authLoading || !isSeller) return;

    const loadProduct = async () => {
      setLoading(true);
      setError(null);
      try {
        setProduct(await getProduct(id));
      } catch (requestError) {
        setError(requestError.response?.data?.message || requestError.message);
      } finally {
        setLoading(false);
      }
    };

    loadProduct();
  }, [authLoading, isSeller, id, navigate]);

  const handleSubmit = async (values) => {
    setSaving(true);
    setError(null);
    try {
      await updateProduct(id, values);
      navigate("/seller/products");
    } catch (requestError) {
      setError(requestError.response?.data?.message || requestError.message);
    } finally {
      setSaving(false);
    }
  };

  if (authLoading || !isSeller) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Edit Product</h1>
      {loading ? (
        <p className="text-center text-gray-600">Loading...</p>
      ) : error && !product ? (
        <div className="text-center">
          <p className="text-red-600 text-sm mb-4" role="alert">
            {error}
          </p>
          <Link
            to="/seller/products"
            className="text-blue-600 hover:text-blue-800"
          >
            Back to My Products
          </Link>
        </div>
      ) : (
        <ProductForm
          initialValues={product}
          submitLabel="Update Product"
          onSubmit={handleSubmit}
          loading={saving}
          error={error}
        />
      )}
    </div>
  );
}
