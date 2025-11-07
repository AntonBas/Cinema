import { useState, useEffect } from 'react';
import type { UserProfile } from '@/types/user';
import { userApi } from '@/api/userApi';

export const useUser = () => {
    const [user, setUser] = useState<UserProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadUserProfile();
    }, []);

    const loadUserProfile = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const userData = await userApi.getProfile();
            setUser(userData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load user profile';
            setError(message);
            console.error('Failed to load user data:', err);
        } finally {
            setIsLoading(false);
        }
    };

    const updateUser = (userData: UserProfile) => {
        setUser(userData);
    };

    const refreshUser = () => {
        loadUserProfile();
    };

    return {
        user,
        isLoading,
        error,
        updateUser,
        refreshUser
    };
};