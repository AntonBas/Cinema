import React, { useCallback, useEffect, useMemo, useState } from "react";
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

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    setUser(response.data.user);
  }, []);

  const register = useCallback(async (userData: RegisterRequest) => {
    const response = await authApi.register(userData);
    return response.data;
  }, []);

  const logout = useCallback(() => {
    authApi.logout().finally(() => {
      setUser(null);
      window.location.href = "/login";
    });
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const response = await authApi.getCurrentUser();
      setUser(response.data);
    } catch (error) {
      console.error("Failed to refresh user:", error);
      setUser(null);
    }
  }, []);

  const value = useMemo(
    () => ({
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
    }),
    [user, loading, isAuthenticated, isAdmin, isCashier, isContentManager, login, register, logout, refreshUser],
  );

  return (
    <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
};
