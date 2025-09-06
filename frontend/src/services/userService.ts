import { api } from './api';
import type { User } from '../types/auth';

export const userService = {
  getProfile: (): Promise<User> => 
    api.get('/auth/profile').then(response => response.data),
};