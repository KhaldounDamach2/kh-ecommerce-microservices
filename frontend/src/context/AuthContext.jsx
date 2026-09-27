import { createContext, useContext, useEffect, useState } from "react";
import * as authApi from "../api/auth";
import { setAccessToken } from "../api/axios";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessTokenState] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedRefreshToken = localStorage.getItem("refreshToken");

    if (!storedRefreshToken) {
      setLoading(false);
      return;
    }

    authApi
      .refresh(storedRefreshToken)
      .then((resp) => {
        setAccessToken(resp.accessToken);
        setAccessTokenState(resp.accessToken);
        setUser(resp.user);
        localStorage.setItem("refreshToken", resp.refreshToken);
      })
      .catch(() => {
        localStorage.removeItem("refreshToken");
        setAccessToken(null);
        setAccessTokenState(null);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const login = async (email, password) => {
    const resp = await authApi.login({ email, password });
    localStorage.setItem("refreshToken", resp.refreshToken);
    setAccessToken(resp.accessToken);
    setAccessTokenState(resp.accessToken);
    setUser(resp.user);
    return resp.user;
  };

  const register = async (data) => {
    return authApi.register(data);
  };

  const confirmEmail = async (token) => {
    return authApi.confirmEmail(token);
  };

  const logout = async () => {
    const storedRefreshToken = localStorage.getItem("refreshToken");
    try {
      if (storedRefreshToken) {
        await authApi.logout(storedRefreshToken);
      }
    } catch {
      // best effort
    }
    localStorage.removeItem("refreshToken");
    setAccessToken(null);
    setAccessTokenState(null);
    setUser(null);
  };

  const isAuthenticated = Boolean(user);
  const hasRole = (role) => user?.role === role;

  const value = {
    user,
    accessToken,
    loading,
    login,
    register,
    confirmEmail,
    logout,
    isAuthenticated,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};

export default AuthContext;
