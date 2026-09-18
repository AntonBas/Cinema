import React, { useEffect, useState } from "react";
import { authApi } from "@/api/authApi";
import { setUnauthorizedHandler } from "@/services/api";
import type { UserResponse } from "@/types/user";
import type { LoginRequest, RegisterRequest } from "@/types/auth";
import { AuthContext } from "./AuthContext";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const isAuthenticated = !!user;
  const isAdmin = user?.userRole === "ROLE_ADMIN";
  const isCashier = user?.userRole === "ROLE_CASHIER";
  const isContentManager = user?.userRole === "ROLE_CONTENT_MANAGER";

  useEffect(() => {
    authApi
      .getCurrentUser()
      .then((response) => setUser(response.data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setUser(response.data.user);
  };

  const register = async (userData: RegisterRequest) => {
    const response = await authApi.register(userData);
    return response.data;
  };

  const logout = () => {
    authApi.logout().finally(() => {
      setUser(null);
      window.location.href = "/login";
    });
  };

  const refreshUser = async () => {
    try {
      const response = await authApi.getCurrentUser();
      setUser(response.data);
    } catch (error) {
      console.error("Failed to refresh user:", error);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isAuthenticated,
        isAdmin,
        isCashier,
        isContentManager,
        login,
        register,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};
