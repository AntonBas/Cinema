import { api } from './api';
import type { LoginRequest, RegisterRequest, LoginResponse, ApiResponse } from '../types/auth';

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post('/auth/login', credentials);

        console.log('🔐 Login response data:', response.data);

        if (response.data.token) {
            console.log('✅ Token found in response body');
            return {
                message: response.data.message,
                token: response.data.token,
                user: response.data.user
            };
        }

        const authHeader = response.headers['authorization'] || response.headers['Authorization'];
        const token = authHeader?.replace('Bearer ', '');

        if (token) {
            console.log('✅ Token found in authorization header');
            return {
                message: response.data.message,
                token: token,
                user: response.data.user
            };
        }

        console.error('❌ No token found in response');
        console.error('Response data:', response.data);
        console.error('Response headers:', response.headers);
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