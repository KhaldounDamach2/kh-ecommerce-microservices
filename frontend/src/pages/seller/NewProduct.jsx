import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import ProductForm from "../../components/ProductForm";
import { createProduct } from "../../api/products";
import { useAuth } from "../../context/AuthContext";

export default function NewProduct() {
  const { hasRole, loading: authLoading } = useAuth();
  const isSeller = hasRole("SELLER");
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!authLoading && !isSeller) {
      navigate("/login", { replace: true });
    }
  }, [authLoading, isSeller, navigate]);

  const handleSubmit = async (values) => {
    setLoading(true);
    setError(null);
    try {
      await createProduct(values);
      navigate("/seller/products");
    } catch (requestError) {
      if ([401, 403].includes(requestError.response?.status)) {
        navigate("/login", { replace: true });
        return;
      }
      setError(requestError.response?.data?.message || requestError.message);
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || !isSeller) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-8">Add New Product</h1>
      <ProductForm
        submitLabel="Create Product"
        onSubmit={handleSubmit}
        loading={loading}
        error={error}
      />
    </div>
  );
}
