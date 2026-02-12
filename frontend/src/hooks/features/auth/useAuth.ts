import { useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
    User
} from '@/types/auth';
import { authApi } from '@/api/authApi';
import { useApi } from '@/hooks/common/useApi';

export const useAuth = () => {
    const navigate = useNavigate();
    const loginApi = useApi<LoginResponse>();
    const registerApi = useApi<User>();
    const currentUserApi = useApi<User>();
    const checkEmailApi = useApi<boolean>();
    const forgotPasswordApi = useApi<void>();
    const resetPasswordApi = useApi<void>();
    const verifyEmailApi = useApi<{ message: string }>();
    const confirmEmailChangeApi = useApi<User>();

    const token = localStorage.getItem('authToken');
    const isAuthenticated = !!token;

    useEffect(() => {
        if (isAuthenticated && !currentUserApi.data && !currentUserApi.state.isLoading) {
            getCurrentUser();
        }
    }, [isAuthenticated]);

    const login = useCallback(async (credentials: LoginRequest) => {
        return loginApi.callApi(
            () => authApi.login(credentials),
            {
                cacheKey: `auth_login_${credentials.email}`,
                cacheTime: 0,
                onSuccess: async (response) => {
                    localStorage.setItem('authToken', response.token);
                    currentUserApi.invalidateCache();
                    await getCurrentUser();
                    navigate('/');
                },
                successMessage: 'Login successful',
            }
        );
    }, [loginApi, currentUserApi, navigate]);

    const register = useCallback(async (userData: RegisterRequest) => {
        return registerApi.callApi(
            () => authApi.register(userData),
            {
                successMessage: 'Registration successful. Please check your email.',
            }
        );
    }, [registerApi]);

    const getCurrentUser = useCallback(async () => {
        return currentUserApi.callApi(
            () => authApi.getCurrentUser(),
            {
                cacheKey: 'current_user',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
                onError: (error) => {
                    if (error.message.includes('401') || error.message.includes('403')) {
                        logout();
                    }
                }
            }
        );
    }, [currentUserApi]);

    const checkEmail = useCallback(async (email: string) => {
        return checkEmailApi.callApi(
            () => authApi.checkEmail(email),
            {
                cacheKey: `email_check_${email}`,
                cacheTime: 60 * 1000,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, [checkEmailApi]);

    const forgotPassword = useCallback(async (email: string) => {
        return forgotPasswordApi.callApi(
            () => authApi.forgotPassword(email),
            {
                successMessage: 'Password reset instructions sent to your email',
            }
        );
    }, [forgotPasswordApi]);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        return resetPasswordApi.callApi(
            () => authApi.resetPassword(token, newPassword),
            {
                successMessage: 'Password has been reset successfully',
            }
        );
    }, [resetPasswordApi]);

    const verifyEmail = useCallback(async (token: string) => {
        return verifyEmailApi.callApi(
            () => authApi.verifyEmail(token),
            {
                successMessage: 'Email verified successfully',
            }
        );
    }, [verifyEmailApi]);

    const confirmEmailChange = useCallback(async (token: string) => {
        return confirmEmailChangeApi.callApi(
            () => authApi.confirmEmailChange(token),
            {
                successMessage: 'Email changed successfully',
                onSuccess: () => {
                    currentUserApi.invalidateCache();
                },
            }
        );
    }, [confirmEmailChangeApi, currentUserApi]);

    const logout = useCallback(() => {
        localStorage.removeItem('authToken');
        loginApi.invalidateCache();
        currentUserApi.invalidateCache();
        checkEmailApi.invalidateCache();
        registerApi.invalidateCache();
        forgotPasswordApi.invalidateCache();
        resetPasswordApi.invalidateCache();
        verifyEmailApi.invalidateCache();
        confirmEmailChangeApi.invalidateCache();
        navigate('/login');
    }, [loginApi, currentUserApi, checkEmailApi, registerApi, forgotPasswordApi,
        resetPasswordApi, verifyEmailApi, confirmEmailChangeApi, navigate]);

    const clearAuthCache = useCallback(() => {
        loginApi.invalidateCache();
        currentUserApi.invalidateCache();
        checkEmailApi.invalidateCache();
        registerApi.invalidateCache();
        forgotPasswordApi.invalidateCache();
        resetPasswordApi.invalidateCache();
        verifyEmailApi.invalidateCache();
        confirmEmailChangeApi.invalidateCache();
    }, [loginApi, currentUserApi, checkEmailApi, registerApi, forgotPasswordApi,
        resetPasswordApi, verifyEmailApi, confirmEmailChangeApi]);

    return {
        user: currentUserApi.data,
        loading: currentUserApi.state.isLoading || loginApi.state.isLoading ||
            registerApi.state.isLoading || checkEmailApi.state.isLoading ||
            forgotPasswordApi.state.isLoading || resetPasswordApi.state.isLoading ||
            verifyEmailApi.state.isLoading || confirmEmailChangeApi.state.isLoading,
        error: currentUserApi.state.isError || loginApi.state.isError ||
            registerApi.state.isError || checkEmailApi.state.isError ||
            forgotPasswordApi.state.isError || resetPasswordApi.state.isError ||
            verifyEmailApi.state.isError || confirmEmailChangeApi.state.isError,
        isAuthenticating: loginApi.state.isLoading,
        isRegistering: registerApi.state.isLoading,
        isCheckingEmail: checkEmailApi.state.isLoading,
        login,
        register,
        getCurrentUser,
        checkEmail,
        forgotPassword,
        resetPassword,
        verifyEmail,
        confirmEmailChange,
        logout,
        clearAuthCache,
        resetLogin: loginApi.reset,
        resetRegister: registerApi.reset,
        resetCurrentUser: currentUserApi.reset,
        resetCheckEmail: checkEmailApi.reset,
        refetchCurrentUser: currentUserApi.refetch,
        isAuthenticated: !!localStorage.getItem('authToken'),
        isAdmin: currentUserApi.data?.userRole === 'ROLE_ADMIN',
    };
};