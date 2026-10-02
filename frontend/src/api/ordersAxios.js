import axios from "axios";

const ordersAxios = axios.create({
  baseURL: "/api",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

let moduleAccessToken = null;

export const setAccessToken = (token) => {
  moduleAccessToken = token;
};

export const getAccessToken = () => moduleAccessToken;

ordersAxios.interceptors.request.use((config) => {
  if (moduleAccessToken) {
    config.headers.Authorization = `Bearer ${moduleAccessToken}`;
  }
  return config;
});

ordersAxios.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error),
);

export default ordersAxios;
