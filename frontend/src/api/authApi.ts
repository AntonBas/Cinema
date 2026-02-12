import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse
} from '@/types/auth';
import type { UserResponse } from '@/types/user';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/auth';
const TOKENS_URL = '/api/tokens';

const getPublicHeaders = (): HeadersInit => {
    return {
        'Content-Type': 'application/json'
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, options);
    if (!response.ok) throw await handleApiError(response);

    if (response.status === 204) {
        return undefined as T;
    }

    const text = await response.text();
    if (!text) {
        return undefined as T;
    }

    try {
        return JSON.parse(text);
    } catch {
        throw new Error('Invalid JSON response');
    }
};

export const authApi = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        return fetchApi<LoginResponse>(`${API_URL}/login`, {
            method: 'POST',
            headers: getPublicHeaders(),
            body: JSON.stringify(credentials),
        });
    },

    register: async (userData: RegisterRequest): Promise<UserResponse> => {
        return fetchApi<UserResponse>(`${API_URL}/register`, {
            method: 'POST',
            headers: getPublicHeaders(),
            body: JSON.stringify(userData),
        });
    },

    getCurrentUser: async (): Promise<UserResponse> => {
        const token = localStorage.getItem('authToken');
        if (!token) {
            throw new Error('No authentication token');
        }
        return fetchApi<UserResponse>(`${API_URL}/me`, {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`,
            },
        });
    },

    checkEmail: async (email: string): Promise<boolean> => {
        return fetchApi<boolean>(`${API_URL}/email/check?email=${encodeURIComponent(email)}`, {
            headers: getPublicHeaders(),
        });
    },

    forgotPassword: async (email: string): Promise<void> => {
        return fetchApi<void>(`${API_URL}/password/forgot?email=${encodeURIComponent(email)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
    },

    resetPassword: async (token: string, newPassword: string): Promise<void> => {
        return fetchApi<void>(`${API_URL}/password/reset?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
    },

    verifyEmail: async (token: string): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${TOKENS_URL}/email/verify?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
    },

    confirmEmailChange: async (token: string): Promise<UserResponse> => {
        return fetchApi<UserResponse>(`${TOKENS_URL}/email/change/confirm?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getPublicHeaders(),
        });
    }
};