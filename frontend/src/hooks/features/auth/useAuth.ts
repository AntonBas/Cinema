import { useState, useEffect, useCallback, useRef } from 'react';
import type { User, LoginRequest, RegisterRequest, LoginResponse } from '@/types/auth';
import { authApi } from '@/api/authApi';
import { useApi } from '@/hooks/common/useApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [authLoading, setAuthLoading] = useState(true);
    const isMountedRef = useRef(true);

    const loginHook = useApi<LoginResponse>();
    const registerHook = useApi<User>();
    const checkEmailHook = useApi<boolean>();
    const forgotPasswordHook = useApi<void>();
    const resetPasswordHook = useApi<void>();
    const verifyEmailHook = useApi<string>();
    const confirmEmailChangeHook = useApi<User>();

    useEffect(() => {
        isMountedRef.current = true;

        const loadUser = async () => {
            if (!token) {
                if (isMountedRef.current) {
                    setUser(null);
                    setAuthLoading(false);
                }
                return;
            }

            try {
                const userData = await authApi.getCurrentUser();
                if (isMountedRef.current) {
                    setUser(userData);
                }
            } catch (error) {
                console.error('Failed to load user:', error);
                if (isMountedRef.current) {
                    setUser(null);
                    setToken(null);
                    localStorage.removeItem('authToken');
                }
            } finally {
                if (isMountedRef.current) {
                    setAuthLoading(false);
                }
            }
        };

        setAuthLoading(true);
        loadUser();

        return () => {
            isMountedRef.current = false;
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
        try {
            const response = await loginHook.callApi(async () => {
                const data = await authApi.login(credentials);
                return data;
            });

            if (isMountedRef.current) {
                setUser(response.user);
                setToken(response.token);
            }

            return response;
        } catch (error) {
            if (isMountedRef.current) {
                setUser(null);
                setToken(null);
            }
            throw error;
        }
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
            if (isMountedRef.current) {
                setUser(prevUser => prevUser?.id === updatedUser.id ? updatedUser : prevUser);
            }
            return updatedUser;
        });
    }, [confirmEmailChangeHook]);

    const logout = useCallback(() => {
        if (isMountedRef.current) {
            setUser(null);
            setToken(null);
        }
        localStorage.removeItem('authToken');
    }, []);

    const updateUser = useCallback((userData: User) => {
        if (isMountedRef.current) {
            setUser(userData);
        }
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