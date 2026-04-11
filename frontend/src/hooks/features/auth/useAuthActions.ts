import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '@/hooks/common/useApi';
import { authApi } from '@/api/authApi';
import type { LoginRequest, RegisterRequest } from '@/types/auth';
import { useAuth } from '@/context/AuthContext';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useAuthActions = () => {
    const navigate = useNavigate();
    const { refreshUser, logout: contextLogout } = useAuth();

    const authApiHook = useApi();

    const loading = useDelayedLoading(authApiHook.loading, { delay: 150, minDisplayTime: 300 });

    const handleAuthSuccess = useCallback(async (token: string) => {
        localStorage.setItem('authToken', token);
        await refreshUser();
        navigate('/');
    }, [refreshUser, navigate]);

    const login = useCallback(async (credentials: LoginRequest) => {
        const response = await authApiHook.execute(
            () => authApi.login(credentials),
            { successMessage: 'Login successful' }
        );
        if (response) {
            await handleAuthSuccess(response.token);
        }
        return response;
    }, [authApiHook, handleAuthSuccess]);

    const register = useCallback(async (userData: RegisterRequest) => {
        return authApiHook.execute(
            () => authApi.register(userData),
            { successMessage: 'Registration successful' }
        );
    }, [authApiHook]);

    const checkEmail = useCallback(async (email: string) => {
        return authApiHook.execute(() => authApi.checkEmail(email));
    }, [authApiHook]);

    const forgotPassword = useCallback(async (email: string) => {
        return authApiHook.execute(
            () => authApi.forgotPassword(email),
            { successMessage: 'Password reset instructions sent to your email' }
        );
    }, [authApiHook]);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        return authApiHook.execute(
            () => authApi.resetPassword(token, newPassword),
            { successMessage: 'Password has been reset successfully' }
        );
    }, [authApiHook]);

    const oauth2Success = useCallback(async (token: string, userId: number, email: string) => {
        const response = await authApiHook.execute(
            () => authApi.oauth2Success(token, userId, email),
            { successMessage: 'Login successful' }
        );
        if (response) {
            await handleAuthSuccess(response.token);
        }
        return response;
    }, [authApiHook, handleAuthSuccess]);

    const loginWithGoogle = useCallback(() => {
        window.location.href = authApi.getGoogleAuthUrl();
    }, []);

    return {
        loading,
        error: authApiHook.error,
        login,
        register,
        checkEmail,
        forgotPassword,
        resetPassword,
        oauth2Success,
        loginWithGoogle,
        logout: contextLogout,
    };
};