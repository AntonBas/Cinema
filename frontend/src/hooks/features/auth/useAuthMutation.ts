import { useState } from 'react';
import { authApi } from '@/api/authApi';
import type { LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth';

export const useAuthMutation = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const login = async (credentials: LoginRequest): Promise<LoginResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await authApi.login(credentials);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Login failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: RegisterRequest) => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.register(userData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Registration failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const checkEmail = async (email: string) => {
        try {
            return await authApi.checkEmail(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Email check failed';
            setError(message);
            throw err;
        }
    };

    const forgotPassword = async (email: string) => {
        try {
            return await authApi.forgotPassword(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Password reset failed';
            setError(message);
            throw err;
        }
    };

    return {
        isLoading,
        error,
        login,
        register,
        checkEmail,
        forgotPassword
    };
};