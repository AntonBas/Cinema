import { api } from './api';
import type { LoginRequest, RegisterRequest, LoginResponse } from '../types/auth';

export const authService = {
    login: async (credentials: LoginRequest): Promise<LoginResponse> => {
        const response = await api.post('/auth/login', credentials);
        
        const authHeader = response.headers['authorization'] || response.headers['Authorization'];
        const token = authHeader?.replace('Bearer ', '');
        
        if (!token) {
            console.error('Authorization header:', authHeader);
            console.error('All response headers:', response.headers);
            throw new Error('No authorization token received from server');
        }
        
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