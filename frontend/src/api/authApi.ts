import { api } from '@/services/api';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse
} from '@/types/auth';
import type { UserResponse } from '@/types/user';

const API_URL = '/api/auth';

export const authApi = {
    login: (credentials: LoginRequest) =>
        api.post<LoginResponse>(`${API_URL}/login`, credentials),

    register: (userData: RegisterRequest) =>
        api.post<UserResponse>(`${API_URL}/register`, userData),

    getCurrentUser: () =>
        api.get<UserResponse>(`${API_URL}/me`),

    checkEmail: (email: string) =>
        api.get<boolean>(`${API_URL}/email/check`, {
            params: { email }
        }),

    forgotPassword: (email: string) =>
        api.post<void>(`${API_URL}/password/forgot`, null, {
            params: { email }
        }),

    resetPassword: (token: string, newPassword: string) =>
        api.post<void>(`${API_URL}/password/reset`, null, {
            params: { token, newPassword }
        }),

    oauth2Success: (token: string, userId: number, email: string) =>
        api.get<LoginResponse>(`${API_URL}/oauth2/success`, {
            params: { token, userId, email }
        }),

    getGoogleAuthUrl: () => {
        return 'http://localhost:8080/oauth2/authorize/google';
    }
};