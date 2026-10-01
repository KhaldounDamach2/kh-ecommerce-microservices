import ordersAxios from "./ordersAxios";

export const placeOrder = (data) =>
  ordersAxios
    .post("/orders", { items: data.items })
    .then((response) => response.data);

export const getMyOrders = ({ page = 0, size = 10 } = {}) =>
  ordersAxios
    .get("/orders/mine", { params: { page, size } })
    .then((response) => response.data);

export const getSellerOrders = ({ page = 0, size = 10 } = {}) =>
  ordersAxios
    .get("/orders/seller", { params: { page, size } })
    .then((response) => response.data);

export const getOrder = (id) =>
  ordersAxios.get(`/orders/${id}`).then((response) => response.data);

export const updateOrderStatus = (id, status) =>
  ordersAxios
    .patch(`/orders/${id}/status`, { status })
    .then((response) => response.data);
