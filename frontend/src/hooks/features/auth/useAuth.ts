import { useState, useEffect } from 'react';
import type { User, UserSimple } from '@/types/auth';
import { userApi } from '@/api/userApi';

export const useAuth = () => {
    const [user, setUser] = useState<User | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('authToken'));
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (token) {
            localStorage.setItem('authToken', token);
        } else {
            localStorage.removeItem('authToken');
            setUser(null);
        }
    }, [token]);

    useEffect(() => {
        const loadUserData = async () => {
            if (token) {
                try {
                    const userData = await userApi.getProfile();
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

    const login = (userData: UserSimple, authToken: string) => {
        const tempUser: User = {
            ...userData,
            dateOfBirth: '',
            city: '',
            phoneNumber: '',
            enabled: true
        };
        setUser(tempUser);
        setToken(authToken);
    };

    const logout = () => {
        setToken(null);
        setUser(null);
    };

    return {
        user,
        token,
        isLoading,
        isAuthenticated: !!user && !!token,
        login,
        logout
    };
};