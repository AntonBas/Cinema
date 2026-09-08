import { createContext, useContext } from "react";
import type { UserResponse } from "@/types/user";
import type { LoginRequest, RegisterRequest } from "@/types/auth";

export interface AuthContextType {
  user: UserResponse | null;
  loading: boolean;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCashier: boolean;
  isContentManager: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<UserResponse>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
};
