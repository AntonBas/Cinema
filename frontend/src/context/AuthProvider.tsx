import React, { useCallback, useEffect, useMemo, useState } from "react";
import { authApi } from "@/features/auth/api/authApi";
import { setUnauthorizedHandler } from "@/services/api";
import type { UserResponse } from "@/features/user/types/user";
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
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null));
    return () => setUnauthorizedHandler(null);
  }, []);

  const logout = useCallback((redirectTo = "/") => {
    authApi.logout().finally(() => {
      setUser(null);
      window.location.href = redirectTo;
    });
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      setUser(await authApi.getCurrentUser());
    } catch {
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
      logout,
      refreshUser,
    }),
    [
      user,
      loading,
      isAuthenticated,
      isAdmin,
      isCashier,
      isContentManager,
      logout,
      refreshUser,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
