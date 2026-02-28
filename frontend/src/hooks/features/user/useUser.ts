import { useCallback } from 'react';
import { useApi } from '@/hooks/common/useApi';
import { userApi } from '@/api/userApi';
import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest,
    UserEmailChangeRequest
} from '@/types/user';

const CACHE_CONFIG = {
    PROFILE: {
        key: 'user_profile',
        time: 5 * 60 * 1000,
    }
} as const;

export const useUser = () => {
    const profileApi = useApi<UserProfileResponse>();
    const updateProfileApi = useApi<UserProfileResponse>();
    const updatePasswordApi = useApi<{ message: string }>();
    const requestEmailChangeApi = useApi<{ message: string }>();

    const getProfile = useCallback(async (skipCache: boolean = false) => {
        if (skipCache) {
            profileApi.invalidateCache(CACHE_CONFIG.PROFILE.key);
        }

        const response = await profileApi.execute(
            () => userApi.getProfile(),
            {
                cacheKey: CACHE_CONFIG.PROFILE.key,
                cacheTime: CACHE_CONFIG.PROFILE.time,
                showErrorNotification: false
            }
        );
        return response?.data || null;
    }, [profileApi]);

    const updateProfile = useCallback(async (data: UserUpdateRequest) => {
        try {
            const response = await updateProfileApi.execute(
                () => userApi.updateProfile(data),
                {
                    successMessage: 'Profile updated successfully',
                    showErrorNotification: false
                }
            );

            profileApi.invalidateCache(CACHE_CONFIG.PROFILE.key);

            if (response?.data) {
                profileApi.setData(response.data);
            }

            return response?.data || null;
        } catch (error) {
            throw error;
        }
    }, [updateProfileApi, profileApi]);

    const updatePassword = useCallback(async (data: UserPasswordUpdateRequest) => {
        const response = await updatePasswordApi.execute(
            () => userApi.updatePassword(data),
            {
                successMessage: 'Password updated successfully',
                showErrorNotification: false
            }
        );
        return response?.data || null;
    }, [updatePasswordApi]);

    const requestEmailChange = useCallback(async (newEmail: string, password: string) => {
        const request: UserEmailChangeRequest = {
            newEmail,
            password
        };
        const response = await requestEmailChangeApi.execute(
            () => userApi.requestEmailChange(request),
            {
                successMessage: 'Confirmation email sent to your new address',
                showErrorNotification: false
            }
        );
        return response?.data || null;
    }, [requestEmailChangeApi]);

    return {
        profile: profileApi.data,
        isLoading: profileApi.loading || updateProfileApi.loading || updatePasswordApi.loading || requestEmailChangeApi.loading,
        isProfileUpdating: updateProfileApi.loading,
        isPasswordUpdating: updatePasswordApi.loading,
        isEmailChanging: requestEmailChangeApi.loading,
        error: profileApi.error || updateProfileApi.error || updatePasswordApi.error || requestEmailChangeApi.error,
        getProfile,
        updateProfile,
        updatePassword,
        requestEmailChange,
        resetProfile: profileApi.reset,
        resetUpdateProfile: updateProfileApi.reset,
        resetUpdatePassword: updatePasswordApi.reset,
        resetRequestEmailChange: requestEmailChangeApi.reset,
    };
};