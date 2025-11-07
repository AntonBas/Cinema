import { useState, useEffect } from 'react';
import type { User } from '@/types/auth';
import { authApi } from '@/api/authApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const loadUserData = async () => {
            if (token) {
                try {
                    const userData = await authApi.getCurrentUser();
                    setUser(userData);
                } catch (error) {
                    console.error('Failed to load user data:', error);
                    setToken(null);
                }
            }
            setIsLoading(false);
        };

        loadUserData();
    }, [token]);

    useEffect(() => {
        if (token) {
            localStorage.setItem('authToken', token);
        } else {
            localStorage.removeItem('authToken');
            setUser(null);
        }
    }, [token]);

    const login = (userData: User, authToken: string) => {
        setUser(userData);
        setToken(authToken);
    };

    const logout = () => {
        setToken(null);
        setUser(null);
    };

    const updateUser = (userData: User) => {
        setUser(userData);
    };

    return {
        user,
        token,
        isLoading,
        isAuthenticated: !!user && !!token,
        isAdmin: user?.userRole === 'ROLE_ADMIN',
        login,
        logout,
        updateUser
    };
};