import React, { useEffect, useRef, useState } from "react";
import { authApi } from "@/api/authApi";
import type { UserResponse } from "@/types/user";
import type { LoginRequest, RegisterRequest } from "@/types/auth";
import { AuthContext } from "./AuthContext";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const fetchedRef = useRef(false);

  const token = localStorage.getItem("authToken");
  const isAuthenticated = !!token;
  const isAdmin = user?.userRole === "ROLE_ADMIN";
  const isCashier = user?.userRole === "ROLE_CASHIER";
  const isContentManager = user?.userRole === "ROLE_CONTENT_MANAGER";

  useEffect(() => {
    if (isAuthenticated && !fetchedRef.current) {
      fetchedRef.current = true;
      setLoading(true);

      authApi
        .getCurrentUser()
        .then((response) => setUser(response.data))
        .catch(() => {
          localStorage.removeItem("authToken");
          setUser(null);
        })
        .finally(() => setLoading(false));
    } else if (!isAuthenticated) {
      setLoading(false);
    }
  }, [isAuthenticated]);

  const login = async (credentials: LoginRequest) => {
    const response = await authApi.login(credentials);
    localStorage.setItem("authToken", response.data.token);
    setUser(response.data.user);
  };

  const register = async (userData: RegisterRequest) => {
    const response = await authApi.register(userData);
    return response.data;
  };

  const logout = () => {
    localStorage.removeItem("authToken");
    setUser(null);
    fetchedRef.current = false;
    window.location.href = "/login";
  };

  const refreshUser = async () => {
    const currentToken = localStorage.getItem("authToken");
    if (currentToken) {
      try {
        const response = await authApi.getCurrentUser();
        setUser(response.data);
      } catch (error) {
        console.error("Failed to refresh user:", error);
        localStorage.removeItem("authToken");
        setUser(null);
      }
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
