import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest
} from '@/types/user';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/users';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

export const userApi = {
    getProfile: async (): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`, {
            headers: getAuthHeaders(),
        });
    },

    updateProfile: async (updateData: UserUpdateRequest): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(updateData),
        });
    },

    requestEmailChange: async (newEmail: string): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${API_URL}/email/change-request?newEmail=${encodeURIComponent(newEmail)}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });
    },

    confirmEmailChange: async (token: string): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/email/confirm-change?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });
    },

    updatePassword: async (passwordData: UserPasswordUpdateRequest): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${API_URL}/password`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify(passwordData),
        });
    }
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        ...options,
        headers: {
            ...getAuthHeaders(),
            ...options.headers,
        },
    });

    if (!response.ok) {
        throw await handleApiError(response);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }

    return undefined as T;
};