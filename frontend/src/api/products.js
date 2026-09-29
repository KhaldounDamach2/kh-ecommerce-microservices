import productsAxios from "./productsAxios";

export const listProducts = ({
  page = 0,
  size = 20,
  sort = "createdAt,desc",
} = {}) =>
  productsAxios
    .get("/products", { params: { page, size, sort } })
    .then((response) => response.data);

export const getProduct = (id) =>
  productsAxios.get(`/products/${id}`).then((response) => response.data);

export const searchProducts = (
  q,
  { page = 0, size = 20, sort = "createdAt,desc" } = {},
) =>
  productsAxios
    .get("/products/search", { params: { q, page, size, sort } })
    .then((response) => response.data);

export const getMyProducts = ({
  page = 0,
  size = 20,
  sort = "createdAt,desc",
} = {}) =>
  productsAxios
    .get("/products/mine", { params: { page, size, sort } })
    .then((response) => response.data);

export const createProduct = (data) => {
  const { name, description, price, stock, category, imageUrl } = data;
  return productsAxios
    .post("/products", { name, description, price, stock, category, imageUrl })
    .then((response) => response.data);
};

export const updateProduct = (id, data) =>
  productsAxios.put(`/products/${id}`, data).then((response) => response.data);

export const deleteProduct = (id) =>
  productsAxios.delete(`/products/${id}`).then(() => undefined);
