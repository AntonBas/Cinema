import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
    CheckEmailResponse,
    User
} from '@/types/auth';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/auth';

const getPublicHeaders = (): HeadersInit => {
    return {
        'Content-Type': 'application/json'
    };
};

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: getPublicHeaders(),
            body: JSON.stringify(credentials),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    // Змінюємо тип повернення на User (з auth.ts)
    register: async (userData: RegisterRequest): Promise<User> => {
        const response = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: getPublicHeaders(),
            body: JSON.stringify(userData),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    getCurrentUser: async (): Promise<User> => {
        const token = localStorage.getItem('authToken');
        const response = await fetch(`${API_URL}/me`, {
            headers: {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` }),
            },
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    checkEmail: async (email: string): Promise<boolean> => {
        const response = await fetch(`${API_URL}/email/check?email=${encodeURIComponent(email)}`, {
            headers: getPublicHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        const data: CheckEmailResponse = await response.json();
        return data.exists;
    },

    forgotPassword: async (email: string): Promise<void> => {
        const response = await fetch(`${API_URL}/password/forgot?email=${encodeURIComponent(email)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
    },

    resetPassword: async (token: string, newPassword: string): Promise<void> => {
        const response = await fetch(`${API_URL}/password/reset?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
    },

    verifyEmail: async (token: string): Promise<string> => {
        const response = await fetch(`${API_URL}/email/verify?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        const data = await response.json();
        return data.message;
    },

    confirmEmailChange: async (token: string): Promise<User> => {
        const response = await fetch(`${API_URL}/email/change/confirm?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    }
};