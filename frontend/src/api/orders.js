import axiosInstance from "./axios";

export const placeOrder = (data) =>
  axiosInstance
    .post("/orders", { items: data.items })
    .then((response) => response.data);

export const getMyOrders = ({ page = 0, size = 10 } = {}) =>
  axiosInstance
    .get("/orders/mine", { params: { page, size } })
    .then((response) => response.data);

export const getSellerOrders = ({ page = 0, size = 10 } = {}) =>
  axiosInstance
    .get("/orders/seller", { params: { page, size } })
    .then((response) => response.data);

export const getOrder = (id) =>
  axiosInstance.get(`/orders/${id}`).then((response) => response.data);

export const updateOrderStatus = (id, status) =>
  axiosInstance
    .patch(`/orders/${id}/status`, { status })
    .then((response) => response.data);
