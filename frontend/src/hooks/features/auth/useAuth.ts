import { useState, useEffect, useCallback } from 'react';
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth';
import { authApi } from '@/api/authApi';
import { useApi } from '@/hooks/common/useApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [authLoading, setAuthLoading] = useState(true);

    const loginHook = useApi<LoginResponse>();
    const registerHook = useApi<User>();
    const checkEmailHook = useApi<boolean>();
    const forgotPasswordHook = useApi<void>();
    const resetPasswordHook = useApi<void>();
    const verifyEmailHook = useApi<string>();
    const confirmEmailChangeHook = useApi<User>();

    useEffect(() => {
        let isMounted = true;

        const loadUser = async () => {
            if (!token) {
                if (isMounted) {
                    setUser(null);
                    setAuthLoading(false);
                }
                return;
            }

            try {
                const userData = await authApi.getCurrentUser();
                if (isMounted) setUser(userData);
            } catch {
                if (isMounted) {
                    setUser(null);
                    setToken(null);
                    localStorage.removeItem('authToken');
                }
            } finally {
                if (isMounted) setAuthLoading(false);
            }
        };

        setAuthLoading(true);
        loadUser();

        return () => {
            isMounted = false;
        };
    }, [token]);

    useEffect(() => {
        if (token) {
            localStorage.setItem('authToken', token);
        } else {
            localStorage.removeItem('authToken');
        }
    }, [token]);

    const login = useCallback(async (credentials: LoginRequest): Promise<LoginResponse> => {
        return loginHook.callApi(async () => {
            const response = await authApi.login(credentials);
            setUser(response.user);
            setToken(response.token);
            return response;
        });
    }, [loginHook]);

    const register = useCallback(async (userData: RegisterRequest): Promise<User> => {
        return registerHook.callApi(async () => {
            return await authApi.register(userData);
        });
    }, [registerHook]);

    const checkEmail = useCallback(async (email: string): Promise<boolean> => {
        return checkEmailHook.callApi(async () => {
            return await authApi.checkEmail(email);
        });
    }, [checkEmailHook]);

    const forgotPassword = useCallback(async (email: string): Promise<void> => {
        return forgotPasswordHook.callApi(async () => {
            await authApi.forgotPassword(email);
        });
    }, [forgotPasswordHook]);

    const resetPassword = useCallback(async (token: string, newPassword: string): Promise<void> => {
        return resetPasswordHook.callApi(async () => {
            await authApi.resetPassword(token, newPassword);
        });
    }, [resetPasswordHook]);

    const verifyEmail = useCallback(async (token: string): Promise<string> => {
        return verifyEmailHook.callApi(async () => {
            return await authApi.verifyEmail(token);
        });
    }, [verifyEmailHook]);

    const confirmEmailChange = useCallback(async (token: string): Promise<User> => {
        return confirmEmailChangeHook.callApi(async () => {
            const updatedUser = await authApi.confirmEmailChange(token);
            setUser(prevUser => prevUser?.id === updatedUser.id ? updatedUser : prevUser);
            return updatedUser;
        });
    }, [confirmEmailChangeHook]);

    const logout = useCallback(() => {
        setUser(null);
        setToken(null);
        localStorage.removeItem('authToken');
    }, []);

    const updateUser = useCallback((userData: User) => {
        setUser(userData);
    }, []);

    return {
        user,
        token,
        isLoading: authLoading,
        isMutating: loginHook.loading || registerHook.loading || checkEmailHook.loading ||
            forgotPasswordHook.loading || resetPasswordHook.loading ||
            verifyEmailHook.loading || confirmEmailChangeHook.loading,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.userRole === 'ROLE_ADMIN',
        login,
        register,
        checkEmail,
        forgotPassword,
        resetPassword,
        verifyEmail,
        confirmEmailChange,
        logout,
        updateUser,
    };
};