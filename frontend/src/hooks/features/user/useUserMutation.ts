import { useState } from 'react';
import type { UserProfile, UserUpdateRequest, EmailChangeResponse, PasswordUpdateResponse } from '@/types/user';
import { userApi, type PasswordUpdateRequest } from '@/api/userApi';

export const useUserMutation = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateProfile = async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        setIsLoading(true);
        setError(null);
        try {
            return await userApi.updateProfile(updateData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update profile';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const requestEmailChange = async (newEmail: string): Promise<EmailChangeResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            return await userApi.requestEmailChange(newEmail);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to request email change';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const confirmEmailChange = async (token: string): Promise<UserProfile> => {
        setIsLoading(true);
        setError(null);
        try {
            return await userApi.confirmEmailChange(token);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to confirm email change';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const updatePassword = async (currentPassword: string, newPassword: string, passwordConfirm: string): Promise<PasswordUpdateResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            const passwordData: PasswordUpdateRequest = {
                currentPassword,
                newPassword,
                passwordConfirm
            };
            return await userApi.updatePassword(passwordData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update password';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const clearError = () => {
        setError(null);
    };

    return {
        isLoading,
        error,
        updateProfile,
        requestEmailChange,
        confirmEmailChange,
        updatePassword,
        clearError
    };
};