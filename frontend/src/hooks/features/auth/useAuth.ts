import { useCallback, useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import type {
    LoginRequest,
    RegisterRequest,
    LoginResponse,
} from '@/types/auth';
import type { UserResponse } from '@/types/user';
import { authApi } from '@/api/authApi';
import { useApi } from '@/hooks/common/useApi';

const CACHE_CONFIG = {
    CURRENT_USER: {
        key: 'current_user',
        time: 5 * 60 * 1000,
    },
    EMAIL_CHECK: {
        time: 60 * 1000,
    },
} as const;

export const useAuth = () => {
    const navigate = useNavigate();
    const fetchedRef = useRef(false);

    const loginApi = useApi<LoginResponse>();
    const registerApi = useApi<UserResponse>();
    const currentUserApi = useApi<UserResponse>();
    const checkEmailApi = useApi<boolean>();
    const forgotPasswordApi = useApi<void>();
    const resetPasswordApi = useApi<void>();
    const verifyEmailApi = useApi<{ message: string }>();
    const confirmEmailChangeApi = useApi<UserResponse>();

    const token = localStorage.getItem('authToken');
    const isAuthenticated = !!token;
    const user = currentUserApi.data;
    const isAdmin = user?.userRole === 'ROLE_ADMIN';

    const loading = useMemo(() =>
        loginApi.loading || registerApi.loading || currentUserApi.loading ||
        checkEmailApi.loading || forgotPasswordApi.loading || resetPasswordApi.loading ||
        verifyEmailApi.loading || confirmEmailChangeApi.loading,
        [loginApi.loading, registerApi.loading, currentUserApi.loading, checkEmailApi.loading,
        forgotPasswordApi.loading, resetPasswordApi.loading, verifyEmailApi.loading,
        confirmEmailChangeApi.loading]);

    const error = useMemo(() =>
        !!(loginApi.error || registerApi.error || currentUserApi.error ||
            checkEmailApi.error || forgotPasswordApi.error || resetPasswordApi.error ||
            verifyEmailApi.error || confirmEmailChangeApi.error),
        [loginApi.error, registerApi.error, currentUserApi.error, checkEmailApi.error,
        forgotPasswordApi.error, resetPasswordApi.error, verifyEmailApi.error,
        confirmEmailChangeApi.error]);

    useEffect(() => {
        if (isAuthenticated && !user && !currentUserApi.loading && !currentUserApi.error && !fetchedRef.current) {
            fetchedRef.current = true;
            getCurrentUser();
        }
    }, [isAuthenticated, user, currentUserApi.loading, currentUserApi.error]);

    const login = useCallback(async (credentials: LoginRequest) => {
        try {
            const response = await loginApi.execute(
                () => authApi.login(credentials),
                {
                    successMessage: 'Login successful'
                }
            );

            if (response) {
                localStorage.setItem('authToken', response.token);
                currentUserApi.invalidateCache();
                fetchedRef.current = false;
                currentUserApi.setData(response.user);
                navigate('/');
            }

            return response;
        } catch (error) {
            throw error;
        }
    }, [loginApi, currentUserApi, navigate]);

    const register = useCallback(async (userData: RegisterRequest) => {
        return registerApi.execute(
            () => authApi.register(userData),
            {
                successMessage: 'Registration successful. Please check your email.'
            }
        );
    }, [registerApi]);

    const getCurrentUser = useCallback(async () => {
        try {
            const result = await currentUserApi.execute(
                () => authApi.getCurrentUser(),
                {
                    cacheKey: CACHE_CONFIG.CURRENT_USER.key,
                    cacheTime: CACHE_CONFIG.CURRENT_USER.time,
                    showErrorNotification: false,
                    onError: (error) => {
                        if (error.message.includes('401') || error.message.includes('403')) {
                            logout();
                        }
                    }
                }
            );

            return result;
        } catch {
            return null;
        }
    }, [currentUserApi]);

    const checkEmail = useCallback(async (email: string) => {
        return checkEmailApi.execute(
            () => authApi.checkEmail(email),
            {
                cacheKey: `email_check_${email}`,
                cacheTime: CACHE_CONFIG.EMAIL_CHECK.time,
                showErrorNotification: false
            }
        );
    }, [checkEmailApi]);

    const forgotPassword = useCallback(async (email: string) => {
        return forgotPasswordApi.execute(
            () => authApi.forgotPassword(email),
            {
                successMessage: 'Password reset instructions sent to your email'
            }
        );
    }, [forgotPasswordApi]);

    const resetPassword = useCallback(async (token: string, newPassword: string) => {
        return resetPasswordApi.execute(
            () => authApi.resetPassword(token, newPassword),
            {
                successMessage: 'Password has been reset successfully'
            }
        );
    }, [resetPasswordApi]);

    const verifyEmail = useCallback(async (token: string) => {
        return verifyEmailApi.execute(
            () => authApi.verifyEmail(token),
            {
                successMessage: 'Email verified successfully'
            }
        );
    }, [verifyEmailApi]);

    const confirmEmailChange = useCallback(async (token: string) => {
        return confirmEmailChangeApi.execute(
            () => authApi.confirmEmailChange(token),
            {
                successMessage: 'Email changed successfully',
                onSuccess: () => {
                    currentUserApi.invalidateCache();
                }
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
        fetchedRef.current = false;
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
        user,
        loading,
        error,
        isAuthenticating: loginApi.loading,
        isRegistering: registerApi.loading,
        isCheckingEmail: checkEmailApi.loading,
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
        isAuthenticated,
        isAdmin,
    };
};