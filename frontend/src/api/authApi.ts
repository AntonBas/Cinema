import { api, API_BASE_URL } from "@/services/api";
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ResendVerificationResponse,
} from "@/types/auth";
import type { UserResponse } from "@/types/user";

const API_URL = "/api/auth";

export const authApi = {
  login: (credentials: LoginRequest) =>
    api.post<AuthResponse>(`${API_URL}/login`, credentials),

  register: (userData: RegisterRequest) =>
    api.post<UserResponse>(`${API_URL}/register`, userData),

  getCurrentUser: () => api.get<UserResponse>(`${API_URL}/me`),

  checkEmail: (email: string) =>
    api.get<boolean>(`${API_URL}/email/check`, {
      params: { email },
    }),

  forgotPassword: (email: string) =>
    api.post<void>(`${API_URL}/password/forgot`, null, {
      params: { email },
    }),

  resetPassword: (token: string, newPassword: string) =>
    api.post<void>(`${API_URL}/password/reset`, { token, newPassword }),

  oauth2Exchange: (code: string) =>
    api.post<AuthResponse>(`${API_URL}/oauth2/exchange`, { code }),

  logout: () => api.post<void>(`${API_URL}/logout`),

  resendVerification: (email: string) =>
    api.post<ResendVerificationResponse>(`${API_URL}/resend-verification`, {
      email,
    }),

  getResendVerificationStatus: (email: string) =>
    api.get<ResendVerificationResponse>(
      `${API_URL}/resend-verification/status`,
      { params: { email } },
    ),

  getGoogleAuthUrl: (): string => {
    return `${API_BASE_URL}/oauth2/authorize/google`;
  },
};
