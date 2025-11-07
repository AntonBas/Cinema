import { api } from '@/services/api';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
    RegisterResponse,
    CheckEmailResponse,
    ApiResponse,
    User
} from '@/types/auth';

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post('/api/auth/login', credentials);
        return response.data;
    },

    register: async (userData: RegisterRequest): Promise<RegisterResponse> => {
        const response = await api.post('/api/auth/registration', userData);
        return response.data;
    },

    getCurrentUser: async (): Promise<User> => {
        const response = await api.get('/api/auth/me');
        return response.data;
    },

    checkEmail: async (email: string): Promise<CheckEmailResponse> => {
        const response = await api.get(`/api/auth/check-email?email=${encodeURIComponent(email)}`);
        return response.data;
    },

    forgotPassword: async (email: string): Promise<ApiResponse> => {
        const response = await api.post(`/api/auth/forgot-password?email=${encodeURIComponent(email)}`);
        return response.data;
    },

    resetPassword: async (token: string, newPassword: string): Promise<ApiResponse> => {
        const response = await api.post(`/api/auth/reset-password?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`);
        return response.data;
    },

    verifyEmail: async (token: string): Promise<ApiResponse> => {
        const response = await api.get(`/api/auth/verify-email?token=${encodeURIComponent(token)}`);
        return response.data;
    },

    confirmEmailChange: async (token: string): Promise<ApiResponse> => {
        const response = await api.post(`/api/auth/email/confirm-change?token=${encodeURIComponent(token)}`);
        return response.data;
    }
};