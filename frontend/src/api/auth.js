import api from "./axios";

export const register = async (data) => {
  const response = await api.post("/auth/register", data);
  return response.data;
};

export const confirmEmail = async (token) => {
  const response = await api.post("/auth/confirm", { token });
  return response.data;
};

export const login = async (data) => {
  const response = await api.post("/auth/login", data);
  return response.data;
};

export const refresh = async (refreshToken) => {
  const response = await api.post("/auth/refresh", { refreshToken });
  return response.data;
};

export const logout = async (refreshToken) => {
  const response = await api.post("/auth/logout", { refreshToken });
  return response.data;
};
