import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '@/hooks/common/useApi';
import { authApi } from '@/api/authApi';
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/auth';
import type { UserResponse } from '@/types/user';
import { useAuth } from '@/contexts/AuthContext';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useAuthActions = () => {
    const navigate = useNavigate();
    const { refreshUser, logout: contextLogout } = useAuth();

    const authApiInstance = useApi<LoginResponse | UserResponse | boolean | { message: string }>();
    const mutationApi = useApi<void | { message: string } | UserResponse>();

    const rawLoading = authApiInstance.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(authApiInstance.error || mutationApi.error);

    const login = useCallback(async (credentials: LoginRequest) => {
        const response = await authApiInstance.execute(
            () => authApi.login(credentials),
            { successMessage: 'Login successful' }
        );

        if (response) {
            localStorage.setItem('authToken', (response as LoginResponse).token);
            await refreshUser();
            navigate('/');
        }

        return response;
    }, [authApiInstance, refreshUser, navigate]);

    const register = useCallback(async (userData: RegisterRequest) => {
        const response = await authApiInstance.execute(
            () => authApi.register(userData),
            { successMessage: 'Registration successful. Please check your email.' }
        );
        return response;
    }, [authApiInstance]);

    const checkEmail = useCallback(async (email: string) => {
        const response = await authApiInstance.execute(
            () => authApi.checkEmail(email),
            { cacheKey: `email_check_${email}`, cacheTime: 60 * 1000 }
        );
        return response;
    }, [authApiInstance]);

    const forgotPassword = useCallback(async (email: string) => {
        await mutationApi.execute(
            () => authApi.forgotPassword(email),
            { successMessage: 'Password reset instructions sent to your email' }
        );
    }, [mutationApi]);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        await mutationApi.execute(
            () => authApi.resetPassword(token, newPassword),
            { successMessage: 'Password has been reset successfully' }
        );
    }, [mutationApi]);

    const verifyEmail = useCallback(async (token: string) => {
        const response = await mutationApi.execute(
            () => authApi.verifyEmail(token),
            { successMessage: 'Email verified successfully' }
        );
        return response;
    }, [mutationApi]);

    const confirmEmailChange = useCallback(async (token: string) => {
        const response = await mutationApi.execute(
            () => authApi.confirmEmailChange(token),
            { successMessage: 'Email changed successfully' }
        );
        await refreshUser();
        return response;
    }, [mutationApi, refreshUser]);

    const logout = useCallback(() => {
        authApiInstance.invalidateCache();
        mutationApi.invalidateCache();
        contextLogout();
    }, [authApiInstance, mutationApi, contextLogout]);

    const clearCache = useCallback(() => {
        authApiInstance.invalidateCache();
        mutationApi.invalidateCache();
    }, [authApiInstance, mutationApi]);

    const resetAll = useCallback(() => {
        authApiInstance.reset();
        mutationApi.reset();
    }, [authApiInstance, mutationApi]);

    return {
        loading,
        error,
        isAuthenticating: authApiInstance.loading,
        isRegistering: authApiInstance.loading,
        isCheckingEmail: authApiInstance.loading,
        isForgotPassword: mutationApi.loading,
        isResettingPassword: mutationApi.loading,
        isVerifyingEmail: mutationApi.loading,
        isConfirmingEmailChange: mutationApi.loading,

        login,
        register,
        checkEmail,
        forgotPassword,
        resetPassword,
        verifyEmail,
        confirmEmailChange,
        logout,

        clearCache,
        resetAll,
    };
};