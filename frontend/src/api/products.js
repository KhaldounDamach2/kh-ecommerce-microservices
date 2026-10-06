import axiosInstance from "./axios";

export const listProducts = ({
  page = 0,
  size = 20,
  sort = "createdAt,desc",
} = {}) =>
  axiosInstance
    .get("/products", { params: { page, size, sort } })
    .then((response) => response.data);

export const getProduct = (id) =>
  axiosInstance.get(`/products/${id}`).then((response) => response.data);

export const searchProducts = (
  q,
  { page = 0, size = 20, sort = "createdAt,desc" } = {},
) =>
  axiosInstance
    .get("/products/search", { params: { q, page, size, sort } })
    .then((response) => response.data);

export const getMyProducts = ({
  page = 0,
  size = 20,
  sort = "createdAt,desc",
} = {}) =>
  axiosInstance
    .get("/products/mine", { params: { page, size, sort } })
    .then((response) => response.data);

export const createProduct = (data) => {
  const { name, description, price, stock, category, imageUrl } = data;
  return axiosInstance
    .post("/products", { name, description, price, stock, category, imageUrl })
    .then((response) => response.data);
};

export const updateProduct = (id, data) =>
  axiosInstance.put(`/products/${id}`, data).then((response) => response.data);

export const deleteProduct = (id) =>
  axiosInstance.delete(`/products/${id}`).then(() => undefined);
