import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest,
    UserEmailChangeRequest
} from '@/types/user';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/users';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, options);

    if (!response.ok) {
        throw await handleApiError(response);
    }

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

export const userApi = {
    getProfile: async (): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`, {
            headers: getAuthHeaders(),
        });
    },

    updateProfile: async (data: UserUpdateRequest): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(data),
        });
    },

    updatePassword: async (data: UserPasswordUpdateRequest): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${API_URL}/password`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify(data),
        });
    },

    requestEmailChange: async (data: UserEmailChangeRequest): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${API_URL}/email/change-request`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(data),
        });
    },
};