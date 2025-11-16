import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
    RegisterResponse,
    CheckEmailResponse,
    ApiResponse,
    User
} from '@/types/auth';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/auth';

const getHeaders = () => ({
    'Content-Type': 'application/json',
});

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(credentials),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    register: async (userData: RegisterRequest): Promise<RegisterResponse> => {
        const response = await fetch(`${API_URL}/registration`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(userData),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    getCurrentUser: async (): Promise<User> => {
        const token = localStorage.getItem('authToken');
        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_URL}/me`, {
            headers,
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    checkEmail: async (email: string): Promise<CheckEmailResponse> => {
        const response = await fetch(`${API_URL}/check-email?email=${encodeURIComponent(email)}`, {
            headers: getHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    forgotPassword: async (email: string): Promise<ApiResponse> => {
        const response = await fetch(`${API_URL}/forgot-password?email=${encodeURIComponent(email)}`, {
            method: 'POST',
            headers: getHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    resetPassword: async (token: string, newPassword: string): Promise<ApiResponse> => {
        const response = await fetch(`${API_URL}/reset-password?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`, {
            method: 'POST',
            headers: getHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    verifyEmail: async (token: string): Promise<ApiResponse> => {
        const response = await fetch(`${API_URL}/verify-email?token=${encodeURIComponent(token)}`, {
            headers: getHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    confirmEmailChange: async (token: string): Promise<ApiResponse> => {
        const response = await fetch(`${API_URL}/email/confirm-change?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    }
};