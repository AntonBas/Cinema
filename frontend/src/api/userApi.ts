import type {
    UserProfile,
    UserUpdateRequest,
    EmailChangeResponse,
    PasswordUpdateResponse
} from '@/types/user';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/users';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

export interface PasswordUpdateRequest {
    currentPassword: string;
    newPassword: string;
    passwordConfirm: string;
}

export const userApi = {
    getProfile: async (): Promise<UserProfile> => {
        const response = await fetch(`${API_URL}/profile`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    updateProfile: async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        const response = await fetch(`${API_URL}/profile`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(updateData),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    requestEmailChange: async (newEmail: string): Promise<EmailChangeResponse> => {
        const response = await fetch(`${API_URL}/email/change-request?newEmail=${encodeURIComponent(newEmail)}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    confirmEmailChange: async (token: string): Promise<UserProfile> => {
        const response = await fetch(`${API_URL}/email/confirm-change?token=${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    updatePassword: async (passwordData: PasswordUpdateRequest): Promise<PasswordUpdateResponse> => {
        const response = await fetch(`${API_URL}/password`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify(passwordData),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    }
};