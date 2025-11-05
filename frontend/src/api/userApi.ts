import { api } from '@/services/api';
import type { User, UserUpdateRequest } from '@/types/user';
import type { ApiResponse } from '@/types/api';

export const userApi = {
    getProfile: async (): Promise<User> => {
        const response = await api.get('/users/profile');
        return response.data;
    },

    updateProfile: async (updateData: UserUpdateRequest): Promise<User> => {
        const response = await api.put('/users/profile', updateData);
        return response.data;
    },

    updateEmail: async (newEmail: string): Promise<User> => {
        const response = await api.patch(`/users/email?newEmail=${encodeURIComponent(newEmail)}`);
        return response.data;
    },

    updatePassword: async (newPassword: string): Promise<ApiResponse> => {
        const response = await api.patch(`/users/password?newPassword=${encodeURIComponent(newPassword)}`);
        return response.data;
    }
};