import api from "./axios";

export const register = async (data) => {
  const response = await api.post("/register", data);
  return response.data;
};

export const confirmEmail = async (token) => {
  const response = await api.post("/confirm", { token });
  return response.data;
};

export const login = async (data) => {
  const response = await api.post("/login", data);
  return response.data;
};

export const refresh = async (refreshToken) => {
  const response = await api.post("/refresh", { refreshToken });
  return response.data;
};

export const logout = async (refreshToken) => {
  const response = await api.post("/logout", { refreshToken });
  return response.data;
};
