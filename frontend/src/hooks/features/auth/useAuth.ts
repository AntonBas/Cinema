import { useCallback } from 'react';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
    User
} from '@/types/auth';
import { authApi } from '@/api/authApi';
import { useApi } from '@/hooks/common/useApi';

export const useAuth = () => {
    const loginApi = useApi<LoginResponse>();
    const registerApi = useApi<User>();
    const currentUserApi = useApi<User>();
    const checkEmailApi = useApi<boolean>();

    const login = useCallback(async (credentials: LoginRequest) => {
        return loginApi.callApi(
            () => authApi.login(credentials),
            {
                cacheKey: `auth_login_${credentials.email}`,
                cacheTime: 0,
                onSuccess: (response) => {
                    localStorage.setItem('authToken', response.token);
                    currentUserApi.invalidateCache();
                },
                successMessage: 'Login successful',
            }
        );
    }, [loginApi, currentUserApi]);

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
        const api = useApi<void>();
        return api.callApi(
            () => authApi.forgotPassword(email),
            {
                successMessage: 'Password reset instructions sent to your email',
            }
        );
    }, []);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        const api = useApi<void>();
        return api.callApi(
            () => authApi.resetPassword(token, newPassword),
            {
                successMessage: 'Password has been reset successfully',
            }
        );
    }, []);

    const verifyEmail = useCallback(async (token: string) => {
        const api = useApi<{ message: string }>();
        return api.callApi(
            () => authApi.verifyEmail(token),
            {
                successMessage: 'Email verified successfully',
            }
        );
    }, []);

    const confirmEmailChange = useCallback(async (token: string) => {
        const api = useApi<User>();
        return api.callApi(
            () => authApi.confirmEmailChange(token),
            {
                successMessage: 'Email changed successfully',
                onSuccess: () => {
                    currentUserApi.invalidateCache();
                },
            }
        );
    }, [currentUserApi]);

    const logout = useCallback(() => {
        localStorage.removeItem('authToken');
        loginApi.invalidateCache();
        currentUserApi.invalidateCache();
        checkEmailApi.invalidateCache();
        registerApi.invalidateCache();
    }, [loginApi, currentUserApi, checkEmailApi, registerApi]);

    const clearAuthCache = useCallback(() => {
        loginApi.invalidateCache();
        currentUserApi.invalidateCache();
        checkEmailApi.invalidateCache();
        registerApi.invalidateCache();
    }, [loginApi, currentUserApi, checkEmailApi, registerApi]);

    return {
        user: currentUserApi.data,

        loading: currentUserApi.state.isLoading || loginApi.state.isLoading ||
            registerApi.state.isLoading || checkEmailApi.state.isLoading,
        error: currentUserApi.state.isError || loginApi.state.isError ||
            registerApi.state.isError || checkEmailApi.state.isError,
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

        isAuthenticated: !!currentUserApi.data && !!localStorage.getItem('authToken'),
        isAdmin: currentUserApi.data?.userRole === 'ROLE_ADMIN',
    };
};