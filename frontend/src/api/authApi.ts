import { api } from '@/services/api';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse
} from '@/types/auth';
import type { UserResponse } from '@/types/user';

const API_URL = '/api/auth';
const TOKENS_URL = '/api/tokens';

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post<LoginResponse>(`${API_URL}/login`, credentials);
        return response.data;
    },

    register: async (userData: RegisterRequest): Promise<UserResponse> => {
        const response = await api.post<UserResponse>(`${API_URL}/register`, userData);
        return response.data;
    },

    getCurrentUser: async (): Promise<UserResponse> => {
        const response = await api.get<UserResponse>(`${API_URL}/me`);
        return response.data;
    },

    checkEmail: async (email: string): Promise<boolean> => {
        const response = await api.get<boolean>(`${API_URL}/email/check`, {
            params: { email }
        });
        return response.data;
    },

    forgotPassword: async (email: string): Promise<void> => {
        await api.post(`${API_URL}/password/forgot`, null, {
            params: { email }
        });
    },

    resetPassword: async (token: string, newPassword: string): Promise<void> => {
        await api.post(`${API_URL}/password/reset`, null, {
            params: { token, newPassword }
        });
    },

    verifyEmail: async (token: string): Promise<{ message: string }> => {
        const response = await api.post<{ message: string }>(`${TOKENS_URL}/email/verify`, null, {
            params: { token }
        });
        return response.data;
    },

    confirmEmailChange: async (token: string): Promise<UserResponse> => {
        const response = await api.post<UserResponse>(`${TOKENS_URL}/email/change/confirm`, null, {
            params: { token }
        });
        return response.data;
    }
};