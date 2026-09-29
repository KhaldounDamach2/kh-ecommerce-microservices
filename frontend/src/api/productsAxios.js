import axios from "axios";

const productsAxios = axios.create({
  baseURL: "http://localhost:8080/api",
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

let moduleAccessToken = null;

export const setAccessToken = (token) => {
  moduleAccessToken = token;
};

export const getAccessToken = () => moduleAccessToken;

productsAxios.interceptors.request.use((config) => {
  if (moduleAccessToken) {
    config.headers.Authorization = `Bearer ${moduleAccessToken}`;
  }
  return config;
});

productsAxios.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error),
);

export default productsAxios;
