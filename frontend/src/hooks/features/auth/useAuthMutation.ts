import { useState } from 'react';
import { authApi } from '@/api/authApi';
import type { LoginRequest, RegisterRequest, LoginResponse, ApiResponse } from '@/types/auth';

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

    const register = async (userData: RegisterRequest): Promise<ApiResponse> => {
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

    const checkEmail = async (email: string): Promise<{ exists: boolean }> => {
        try {
            return await authApi.checkEmail(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Email check failed';
            setError(message);
            throw err;
        }
    };

    const forgotPassword = async (email: string): Promise<ApiResponse> => {
        try {
            return await authApi.forgotPassword(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Password reset failed';
            setError(message);
            throw err;
        }
    };

    const resetPassword = async (token: string, newPassword: string): Promise<ApiResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.resetPassword(token, newPassword);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Password reset failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const verifyEmail = async (token: string): Promise<ApiResponse> => {
        try {
            return await authApi.verifyEmail(token);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Email verification failed';
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
        forgotPassword,
        resetPassword,
        verifyEmail
    };
};