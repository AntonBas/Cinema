import { api } from './api';
import type { LoginRequest, RegisterRequest, LoginResponse } from '../types/auth';

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post('/auth/login', credentials);
        const token = response.headers['authorization']?.replace('Bearer ', '');
        return {
            message: response.data,
            token: token
        };
    },

    register: (userData: RegisterRequest) => {
        return api.post('/auth/registration', userData);
    },

    checkEmail: (email: string) => {
        return api.get(`/auth/check-email?email=${email}`);
    }
};