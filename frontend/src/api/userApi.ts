import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest
} from '@/types/user';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/users';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    const headers: HeadersInit = {
        'Content-Type': 'application/json',
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
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

export const userApi = {
    getProfile: async (): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`);
    },

    updateProfile: async (updateData: UserUpdateRequest): Promise<UserProfileResponse> => {
        return fetchApi<UserProfileResponse>(`${API_URL}/profile`, {
            method: 'PUT',
            body: JSON.stringify(updateData),
        });
    },

    requestEmailChange: async (newEmail: string): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(
            `${API_URL}/email/change-request?newEmail=${encodeURIComponent(newEmail)}`,
            {
                method: 'POST',
            }
        );
    },

    updatePassword: async (passwordData: UserPasswordUpdateRequest): Promise<{ message: string }> => {
        return fetchApi<{ message: string }>(`${API_URL}/password`, {
            method: 'PATCH',
            body: JSON.stringify(passwordData),
        });
    }
};