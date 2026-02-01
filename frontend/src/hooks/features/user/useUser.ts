import { useState, useCallback, useEffect } from 'react';
import type {
    UserProfile,
    UserUpdateRequest,
    UserPasswordUpdateRequest
} from '@/types/user';
import { userApi } from '@/api/userApi';
import { useApi } from '@/hooks/common/useApi';

export const useUser = () => {
    const profileApi = useApi<UserProfile>();
    const emailChangeApi = useApi<{ message: string }>();
    const passwordApi = useApi<{ message: string }>();

    const [user, setUser] = useState<UserProfile | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        const loadInitialProfile = async () => {
            const token = localStorage.getItem('authToken');
            if (!token) {
                setErrorMessage('No authentication token found. Please log in.');
                return;
            }
            try {
                const userData = await profileApi.callApi(() => userApi.getProfile(), {
                    showErrorNotification: false,
                    silent: true
                });
                setUser(userData);
                setErrorMessage(null);
            } catch (error) {
                console.error('Failed to load initial profile:', error);
                setErrorMessage(error instanceof Error ? error.message : 'Failed to load user data');
            }
        };

        loadInitialProfile();
    }, []);

    useEffect(() => {
        setUser(profileApi.data);
        if (profileApi.error) {
            setErrorMessage(profileApi.error.message);
        }
    }, [profileApi.data, profileApi.error]);

    useEffect(() => {
        if (emailChangeApi.error) {
            setErrorMessage(emailChangeApi.error.message);
        }
    }, [emailChangeApi.error]);

    useEffect(() => {
        if (passwordApi.error) {
            setErrorMessage(passwordApi.error.message);
        }
    }, [passwordApi.error]);

    const loadProfile = useCallback(async () => {
        try {
            const userData = await profileApi.callApi(() => userApi.getProfile(), {
                showErrorNotification: false,
                silent: true
            });
            setUser(userData);
            setErrorMessage(null);
            return userData;
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to load user data');
            throw error;
        }
    }, [profileApi]);

    const updateProfile = useCallback(async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        try {
            const updatedUser = await profileApi.callApi(() => userApi.updateProfile(updateData), {
                successMessage: 'Profile updated successfully',
                showErrorNotification: false
            });
            setUser(updatedUser);
            setErrorMessage(null);
            return updatedUser;
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to update profile');
            throw error;
        }
    }, [profileApi]);

    const requestEmailChange = useCallback(async (newEmail: string): Promise<{ message: string }> => {
        try {
            const result = await emailChangeApi.callApi(() => userApi.requestEmailChange(newEmail), {
                successMessage: 'Email change request sent. Check your new email.',
                showErrorNotification: false
            });
            setErrorMessage(null);
            return result;
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to request email change');
            throw error;
        }
    }, [emailChangeApi]);

    const confirmEmailChange = useCallback(async (token: string): Promise<UserProfile> => {
        try {
            const updatedUser = await profileApi.callApi(() => userApi.confirmEmailChange(token), {
                successMessage: 'Email updated successfully',
                showErrorNotification: false
            });
            setUser(updatedUser);
            setErrorMessage(null);
            return updatedUser;
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to confirm email change');
            throw error;
        }
    }, [profileApi]);

    const updatePassword = useCallback(async (
        currentPassword: string,
        newPassword: string,
        passwordConfirm: string
    ): Promise<{ message: string }> => {
        try {
            const passwordData: UserPasswordUpdateRequest = {
                currentPassword,
                newPassword,
                passwordConfirm
            };
            const result = await passwordApi.callApi(() => userApi.updatePassword(passwordData), {
                successMessage: 'Password updated successfully',
                showErrorNotification: false
            });
            setErrorMessage(null);
            return result;
        } catch (error) {
            setErrorMessage(error instanceof Error ? error.message : 'Failed to update password');
            throw error;
        }
    }, [passwordApi]);

    const refreshUser = useCallback(async () => {
        return loadProfile();
    }, [loadProfile]);

    const clearError = useCallback(() => {
        setErrorMessage(null);
    }, []);

    return {
        user,
        isLoading: profileApi.loading || emailChangeApi.loading || passwordApi.loading,
        error: errorMessage,
        loadProfile,
        updateProfile,
        requestEmailChange,
        confirmEmailChange,
        updatePassword,
        refreshUser,
        clearError,
        resetProfile: profileApi.reset
    };
};