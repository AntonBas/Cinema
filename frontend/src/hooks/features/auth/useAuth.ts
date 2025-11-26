import { useState, useEffect, useCallback } from 'react';
import type { User, LoginResponse } from '@/types/auth';
import { authApi } from '@/api/authApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let isMounted = true;

        const loadUser = async () => {
            if (!token) {
                if (isMounted) {
                    setUser(null);
                    setIsLoading(false);
                }
                return;
            }

            try {
                setError(null);
                const userData = await authApi.getCurrentUser();
                if (isMounted) {
                    setUser(userData);
                }
            } catch (error) {
                console.error('Failed to load user data:', error);
                if (isMounted) {
                    setUser(null);
                    setToken(null);
                    setError('Failed to load user data');
                }
            } finally {
                if (isMounted) setIsLoading(false);
            }
        };

        setIsLoading(true);
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

    const login = useCallback((loginResponse: LoginResponse) => {
        setUser(loginResponse.user);
        setToken(loginResponse.token);
        setError(null);
    }, []);

    const logout = useCallback(() => {
        setUser(null);
        setToken(null);
        setError(null);
    }, []);

    const updateUser = useCallback((userData: User) => {
        setUser(userData);
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        user,
        token,
        isLoading,
        error,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.userRole === 'ROLE_ADMIN',
        login,
        logout,
        updateUser,
        clearError,
    };
};