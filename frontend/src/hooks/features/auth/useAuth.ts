import { useState, useEffect, useCallback } from 'react';
import type { User } from '@/types/auth';
import { authApi } from '@/api/authApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [isLoading, setIsLoading] = useState(true);

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
                const userData = await authApi.getCurrentUser();
                if (isMounted) {
                    setUser(userData);
                }
            } catch (error) {
                console.error('Failed to load user data:', error);
                if (isMounted) {
                    setUser(null);
                    setToken(null);
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
        if (token) localStorage.setItem('authToken', token);
        else localStorage.removeItem('authToken');
    }, [token]);

    const login = useCallback((userData: User, authToken: string) => {
        setUser(userData);
        setToken(authToken);
    }, []);

    const logout = useCallback(() => {
        setUser(null);
        setToken(null);
    }, []);

    const updateUser = useCallback((userData: User) => {
        setUser(userData);
    }, []);

    return {
        user,
        token,
        isLoading,
        isAuthenticated: !!token && !!user,
        isAdmin: user?.userRole === 'ROLE_ADMIN',
        login,
        logout,
        updateUser,
    };
};
