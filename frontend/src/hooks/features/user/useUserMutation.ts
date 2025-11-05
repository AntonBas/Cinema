import { useState } from 'react';
import type { User, UserUpdateRequest } from '@/types/user';
import type { ApiResponse } from '@/types/api';
import { userApi } from '@/api/userApi';

export const useUserMutation = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateProfile = async (updateData: UserUpdateRequest): Promise<User> => {
        setIsLoading(true);
        setError(null);
        try {
            const updatedUser = await userApi.updateProfile(updateData);
            return updatedUser;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Profile update failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const requestEmailChange = async (newEmail: string): Promise<ApiResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await userApi.requestEmailChange(newEmail);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Email change request failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const confirmEmailChange = async (token: string): Promise<User> => {
        setIsLoading(true);
        setError(null);
        try {
            const updatedUser = await userApi.confirmEmailChange(token);
            return updatedUser;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Email change confirmation failed';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const updatePassword = async (newPassword: string): Promise<ApiResponse> => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await userApi.updatePassword(newPassword);
            return response;
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