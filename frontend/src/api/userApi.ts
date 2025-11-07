import { api } from '@/services/api';
import type {
    UserProfile,
    UserUpdateRequest,
    EmailChangeResponse,
    PasswordUpdateResponse
} from '@/types/user';

export const userApi = {
    getProfile: async (): Promise<UserProfile> => {
        const response = await api.get('/api/users/profile');
        return response.data;
    },

    updateProfile: async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        const response = await api.put('/api/users/profile', updateData);
        return response.data;
    },

    requestEmailChange: async (newEmail: string): Promise<EmailChangeResponse> => {
        const response = await api.post(`/api/users/email/change-request?newEmail=${encodeURIComponent(newEmail)}`);
        return response.data;
    },

    confirmEmailChange: async (token: string): Promise<UserProfile> => {
        const response = await api.post(`/api/users/email/confirm-change?token=${encodeURIComponent(token)}`);
        return response.data;
    },

    updatePassword: async (newPassword: string): Promise<PasswordUpdateResponse> => {
        const response = await api.patch(`/api/users/password?newPassword=${encodeURIComponent(newPassword)}`);
        return response.data;
    }
};