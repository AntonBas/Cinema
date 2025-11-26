import { useState } from 'react';
import { authApi } from '@/api/authApi';
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '@/types/auth';

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
            const message = err instanceof Error ? err.message : 'Failed to login';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: RegisterRequest): Promise<User> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.register(userData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to register';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const checkEmail = async (email: string): Promise<boolean> => {
        setError(null);
        try {
            return await authApi.checkEmail(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to check email';
            setError(message);
            throw err;
        }
    };

    const forgotPassword = async (email: string): Promise<void> => {
        setIsLoading(true);
        setError(null);
        try {
            await authApi.forgotPassword(email);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to send password reset';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const resetPassword = async (token: string, newPassword: string): Promise<void> => {
        setIsLoading(true);
        setError(null);
        try {
            await authApi.resetPassword(token, newPassword);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to reset password';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const verifyEmail = async (token: string): Promise<string> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.verifyEmail(token);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to verify email';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const confirmEmailChange = async (token: string): Promise<User> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.confirmEmailChange(token);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to confirm email change';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const clearError = () => {
        setError(null);
    };

    return {
        isLoading,
        error,
        login,
        register,
        checkEmail,
        forgotPassword,
        resetPassword,
        verifyEmail,
        confirmEmailChange,
        clearError
    };
};