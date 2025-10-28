import { api } from './api';
import type { User } from '../types/Auth';

export const userService = {
  getProfile: async (): Promise<User> => {
    const response = await api.get('/auth/profile');
    return response.data;
  }
};