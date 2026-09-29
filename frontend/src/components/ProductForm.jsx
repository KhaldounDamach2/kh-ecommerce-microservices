import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

const emptyValues = {
  name: "",
  description: "",
  price: "",
  stock: "",
  category: "",
  imageUrl: "",
};

export default function ProductForm({
  initialValues = null,
  onSubmit,
  submitLabel = "Save",
  loading = false,
  error = null,
}) {
  const [values, setValues] = useState(emptyValues);
  const [validationError, setValidationError] = useState(null);

  useEffect(() => {
    setValues({
      ...emptyValues,
      ...(initialValues || {}),
      price: initialValues?.price ?? "",
      stock: initialValues?.stock ?? "",
    });
    setValidationError(null);
  }, [initialValues]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((currentValues) => ({ ...currentValues, [name]: value }));
    setValidationError(null);
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!values.name.trim()) {
      setValidationError("Name is required.");
      return;
    }

    if (values.name.trim().length > 200) {
      setValidationError("Name must be 200 characters or fewer.");
      return;
    }

    if (values.price === "" || Number(values.price) < 0) {
      setValidationError("Price is required and must be at least 0.");
      return;
    }

    if (values.stock === "" || Number(values.stock) < 0) {
      setValidationError("Stock is required and must be at least 0.");
      return;
    }

    onSubmit({
      ...values,
      name: values.name.trim(),
      price: Number(values.price),
      stock: parseInt(values.stock, 10),
    });
  };

  const displayedError = validationError || error;

  return (
    <form
      onSubmit={handleSubmit}
      className="max-w-2xl mx-auto p-6 bg-white rounded-lg shadow"
    >
      <div className="space-y-5">
        <div>
          <label
            htmlFor="name"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            Name
          </label>
          <input
            id="name"
            name="name"
            type="text"
            required
            maxLength={200}
            value={values.name}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label
            htmlFor="description"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            Description
          </label>
          <textarea
            id="description"
            name="description"
            rows={4}
            value={values.description}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label
              htmlFor="price"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Price
            </label>
            <input
              id="price"
              name="price"
              type="number"
              step="0.01"
              min="0"
              required
              value={values.price}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label
              htmlFor="stock"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Stock
            </label>
            <input
              id="stock"
              name="stock"
              type="number"
              min="0"
              required
              value={values.stock}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div>
          <label
            htmlFor="category"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            Category
          </label>
          <input
            id="category"
            name="category"
            type="text"
            value={values.category}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        <div>
          <label
            htmlFor="imageUrl"
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            Image URL
          </label>
          <input
            id="imageUrl"
            name="imageUrl"
            type="text"
            value={values.imageUrl}
            onChange={handleChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {displayedError && (
        <p className="text-red-600 text-sm mt-5" role="alert">
          {displayedError}
        </p>
      )}

      <div className="flex items-center gap-3 mt-6">
        <button
          type="submit"
          disabled={loading}
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50"
        >
          {submitLabel}
        </button>
        <Link
          to="/seller/products"
          className="bg-gray-200 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-300"
        >
          Cancel
        </Link>
      </div>
    </form>
  );
}
