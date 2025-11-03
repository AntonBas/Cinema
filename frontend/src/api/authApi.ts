import { api } from '@/services/api';
import type { LoginRequest, RegisterRequest, LoginResponse, ApiResponse, User } from '@/types/auth';

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post('/auth/login', credentials);

        if (response.data.token) {
            return {
                message: response.data.message,
                token: response.data.token,
                user: response.data.user
            };
        }

        const authHeader = response.headers['authorization'] || response.headers['Authorization'];
        const token = authHeader?.replace('Bearer ', '');

        if (token) {
            return {
                message: response.data.message,
                token: token,
                user: response.data.user
            };
        }

        throw new Error('No authorization token received from server');
    },

    register: async (userData: RegisterRequest): Promise<ApiResponse> => {
        const response = await api.post('/auth/registration', userData);
        return response.data;
    },

    checkEmail: async (email: string): Promise<{ exists: boolean }> => {
        const response = await api.get(`/auth/check-email?email=${email}`);
        return response.data;
    },

    forgotPassword: async (email: string): Promise<ApiResponse> => {
        const response = await api.post(`/auth/forgot-password?email=${email}`);
        return response.data;
    },

    resetPassword: async (token: string, newPassword: string): Promise<ApiResponse> => {
        const response = await api.post(`/auth/reset-password?token=${token}&newPassword=${newPassword}`);
        return response.data;
    },

    verifyEmail: async (token: string): Promise<ApiResponse> => {
        const response = await api.get(`/auth/verify-email?token=${token}`);
        return response.data;
    }
};