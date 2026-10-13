import axiosInstance from "./axios";

export const getMyNotifications = ({ page = 0, size = 20 } = {}) =>
  axiosInstance
    .get("/notifications/mine", { params: { page, size } })
    .then((response) => response.data);

export const getUnreadCount = () =>
  axiosInstance
    .get("/notifications/mine/unread-count")
    .then((response) => response.data.count);

export const markAsRead = (id) =>
  axiosInstance.patch(`/notifications/${id}/read`);

export const markAllAsRead = () =>
  axiosInstance.patch("/notifications/mine/read-all");

// The bell listens for this, so its badge updates right away
// instead of waiting for the next poll.
export const NOTIFICATIONS_CHANGED = "notifications:changed";

export const announceNotificationsChanged = () =>
  window.dispatchEvent(new Event(NOTIFICATIONS_CHANGED));
