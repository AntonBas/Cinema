import { useState } from 'react';
import type { UserProfile, UserUpdateRequest, EmailChangeResponse, PasswordUpdateResponse } from '@/types/user';
import { userApi } from '@/api/userApi';

export const useUserMutation = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateProfile = async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        setIsLoading(true);
        setError(null);
        try {
            return await userApi.updateProfile(updateData);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Profile update failed';
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
            const message = err instanceof Error ? err.message : 'Email change request failed';
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
            const message = err instanceof Error ? err.message : 'Email change confirmation failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const updatePassword = async (newPassword: string): Promise<PasswordUpdateResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            return await userApi.updatePassword(newPassword);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Password update failed';
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