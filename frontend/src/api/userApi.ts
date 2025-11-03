import { api } from '@/services/api';
import type { User } from '@/types/auth';

export const userApi = {
    getProfile: async (): Promise<User> => {
        const response = await api.get('/auth/profile');
        return response.data;
    }
};