import { useCallback } from 'react';
import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest
} from '@/types/user';
import { userApi } from '@/api/userApi';
import { useApi } from '@/hooks/common/useApi';

export const useUser = () => {
    const profileApi = useApi<UserProfileResponse>();
    const emailChangeApi = useApi<{ message: string }>();
    const passwordApi = useApi<{ message: string }>();

    const getProfile = useCallback(async () => {
        return profileApi.callApi(
            () => userApi.getProfile(),
            {
                cacheKey: 'user_profile',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [profileApi]);

    const updateProfile = useCallback(async (updateData: UserUpdateRequest) => {
        return profileApi.callApi(
            () => userApi.updateProfile(updateData),
            {
                successMessage: 'Profile updated successfully',
                onSuccess: () => {
                    profileApi.invalidateCache('user_profile');
                },
            }
        );
    }, [profileApi]);

    const requestEmailChange = useCallback(async (newEmail: string) => {
        return emailChangeApi.callApi(
            () => userApi.requestEmailChange(newEmail),
            {
                successMessage: 'Email change request sent. Check your new email.',
            }
        );
    }, [emailChangeApi]);

    const updatePassword = useCallback(async (passwordData: UserPasswordUpdateRequest) => {
        return passwordApi.callApi(
            () => userApi.updatePassword(passwordData),
            {
                successMessage: 'Password updated successfully',
            }
        );
    }, [passwordApi]);

    const clearCache = useCallback(() => {
        profileApi.invalidateCache();
        emailChangeApi.invalidateCache();
        passwordApi.invalidateCache();
    }, [profileApi, emailChangeApi, passwordApi]);

    const logout = useCallback(() => {
        localStorage.removeItem('authToken');
        clearCache();
        window.location.href = '/login';
    }, [clearCache]);

    return {
        user: profileApi.data,

        loading: profileApi.state.isLoading || emailChangeApi.state.isLoading || passwordApi.state.isLoading,
        error: profileApi.state.isError || emailChangeApi.state.isError || passwordApi.state.isError,
        isProfileLoading: profileApi.state.isLoading,
        isEmailChanging: emailChangeApi.state.isLoading,
        isPasswordUpdating: passwordApi.state.isLoading,

        getProfile,
        updateProfile,
        requestEmailChange,
        updatePassword,
        clearCache,
        logout,

        resetProfile: profileApi.reset,
        resetEmailChange: emailChangeApi.reset,
        resetPassword: passwordApi.reset,
        refetchProfile: profileApi.refetch,
    };
};