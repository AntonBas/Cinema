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
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Login failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.response?.status === 401) {
                errorMessage = 'Invalid email or password';
            } else if (err.response?.status === 500) {
                errorMessage = 'Server error. Please try again later.';
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const register = async (userData: RegisterRequest): Promise<ApiResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.register(userData);
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Registration failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.response?.status === 400) {
                errorMessage = 'Please check your input data';
            } else if (err.response?.status === 409) {
                errorMessage = 'Email already exists';
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const checkEmail = async (email: string): Promise<{ exists: boolean }> => {
        setError(null);
        try {
            return await authApi.checkEmail(email);
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Email check failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        }
    };

    const forgotPassword = async (email: string): Promise<ApiResponse> => {
        setError(null);
        try {
            return await authApi.forgotPassword(email);
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Password reset failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.response?.status === 404) {
                errorMessage = 'Email not found';
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        }
    };

    const resetPassword = async (token: string, newPassword: string): Promise<ApiResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            return await authApi.resetPassword(token, newPassword);
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Password reset failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.response?.status === 400) {
                errorMessage = 'Invalid or expired token';
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const verifyEmail = async (token: string): Promise<ApiResponse> => {
        setError(null);
        try {
            return await authApi.verifyEmail(token);
        } catch (err: any) {
            const backendData = err.response?.data;
            let errorMessage = 'Email verification failed';

            if (backendData && backendData.message) {
                errorMessage = backendData.message;
            } else if (err.response?.status === 400) {
                errorMessage = 'Invalid or expired verification link';
            } else if (err.message) {
                errorMessage = err.message;
            }

            setError(errorMessage);
            throw new Error(errorMessage);
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
        clearError
    };
};