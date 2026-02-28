import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApi } from '@/hooks/common/useApi';
import { authApi } from '@/api/authApi';
import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/auth';
import type { UserResponse } from '@/types/user';
import { useAuth } from '@/contexts/AuthContext';

export const useAuthActions = () => {
    const navigate = useNavigate();
    const { refreshUser, logout: contextLogout } = useAuth();

    const loginApi = useApi<LoginResponse>();
    const registerApi = useApi<UserResponse>();
    const checkEmailApi = useApi<boolean>();
    const forgotPasswordApi = useApi<void>();
    const resetPasswordApi = useApi<void>();
    const verifyEmailApi = useApi<{ message: string }>();
    const confirmEmailChangeApi = useApi<UserResponse>();

    const login = useCallback(async (credentials: LoginRequest) => {
        try {
            const response = await loginApi.execute(
                () => authApi.login(credentials),
                { successMessage: 'Login successful' }
            );

            if (response) {
                localStorage.setItem('authToken', response.token);
                await refreshUser();
                navigate('/');
            }

            return response;
        } catch (error) {
            throw error;
        }
    }, [loginApi, refreshUser, navigate]);

    const register = useCallback(async (userData: RegisterRequest) => {
        try {
            const response = await registerApi.execute(
                () => authApi.register(userData),
                { successMessage: 'Registration successful. Please check your email.' }
            );
            return response;
        } catch (error) {
            throw error;
        }
    }, [registerApi]);

    const checkEmail = useCallback(async (email: string) => {
        try {
            const response = await checkEmailApi.execute(
                () => authApi.checkEmail(email),
                { cacheKey: `email_check_${email}`, cacheTime: 60 * 1000 }
            );
            return response;
        } catch (error) {
            throw error;
        }
    }, [checkEmailApi]);

    const forgotPassword = useCallback(async (email: string) => {
        try {
            await forgotPasswordApi.execute(
                () => authApi.forgotPassword(email),
                { successMessage: 'Password reset instructions sent to your email' }
            );
        } catch (error) {
            throw error;
        }
    }, [forgotPasswordApi]);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        try {
            await resetPasswordApi.execute(
                () => authApi.resetPassword(token, newPassword),
                { successMessage: 'Password has been reset successfully' }
            );
        } catch (error) {
            throw error;
        }
    }, [resetPasswordApi]);

    const verifyEmail = useCallback(async (token: string) => {
        try {
            const response = await verifyEmailApi.execute(
                () => authApi.verifyEmail(token),
                { successMessage: 'Email verified successfully' }
            );
            return response;
        } catch (error) {
            throw error;
        }
    }, [verifyEmailApi]);

    const confirmEmailChange = useCallback(async (token: string) => {
        try {
            const result = await confirmEmailChangeApi.execute(
                () => authApi.confirmEmailChange(token),
                { successMessage: 'Email changed successfully' }
            );
            await refreshUser();
            return result;
        } catch (error) {
            throw error;
        }
    }, [confirmEmailChangeApi, refreshUser]);

    const logout = useCallback(() => {
        loginApi.invalidateCache();
        registerApi.invalidateCache();
        checkEmailApi.invalidateCache();
        forgotPasswordApi.invalidateCache();
        resetPasswordApi.invalidateCache();
        verifyEmailApi.invalidateCache();
        confirmEmailChangeApi.invalidateCache();
        contextLogout();
    }, [loginApi, registerApi, checkEmailApi, forgotPasswordApi,
        resetPasswordApi, verifyEmailApi, confirmEmailChangeApi, contextLogout]);

    return {
        isAuthenticating: loginApi.loading,
        isRegistering: registerApi.loading,
        isCheckingEmail: checkEmailApi.loading,
        isForgotPassword: forgotPasswordApi.loading,
        isResettingPassword: resetPasswordApi.loading,
        isVerifyingEmail: verifyEmailApi.loading,
        isConfirmingEmailChange: confirmEmailChangeApi.loading,
        login,
        register,
        checkEmail,
        forgotPassword,
        resetPassword,
        verifyEmail,
        confirmEmailChange,
        logout,
        resetLogin: loginApi.reset,
        resetRegister: registerApi.reset,
        resetCheckEmail: checkEmailApi.reset,
        resetForgotPassword: forgotPasswordApi.reset,
        resetResetPassword: resetPasswordApi.reset,
        resetVerifyEmail: verifyEmailApi.reset,
        resetConfirmEmailChange: confirmEmailChangeApi.reset,
    };
};