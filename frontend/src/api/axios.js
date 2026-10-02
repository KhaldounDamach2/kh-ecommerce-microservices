import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "/api/auth",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

let moduleAccessToken = null;

export const setAccessToken = (token) => {
  moduleAccessToken = token;
};

export const getAccessToken = () => moduleAccessToken;

const clearAuthAndRedirect = () => {
  localStorage.removeItem("refreshToken");
  setAccessToken(null);
  window.location.href = "/login";
};

const NO_REFRESH_PATHS = ["/login", "/register", "/refresh", "/confirm"];

let refreshPromise = null;

axiosInstance.interceptors.request.use((config) => {
  if (moduleAccessToken) {
    config.headers.Authorization = `Bearer ${moduleAccessToken}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status !== 401 || !originalRequest) {
      return Promise.reject(error);
    }

    const isAuthEndpoint = NO_REFRESH_PATHS.some((path) =>
      originalRequest.url?.includes(path),
    );
    if (isAuthEndpoint) {
      return Promise.reject(error);
    }

    if (originalRequest._retry) {
      clearAuthAndRedirect();
      return Promise.reject(error);
    }

    const storedRefreshToken = localStorage.getItem("refreshToken");
    if (!storedRefreshToken) {
      clearAuthAndRedirect();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (!refreshPromise) {
      refreshPromise = axiosInstance
        .post("/refresh", {
          refreshToken: storedRefreshToken,
        })
        .then((response) => {
          setAccessToken(response.data.accessToken);
          localStorage.setItem("refreshToken", response.data.refreshToken);
          return response.data.accessToken;
        })
        .catch((refreshError) => {
          clearAuthAndRedirect();
          throw refreshError;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }

    try {
      const newAccessToken = await refreshPromise;
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return axiosInstance(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  },
);

export default axiosInstance;
