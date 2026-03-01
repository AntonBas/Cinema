import { useCallback } from 'react';
import { useApi } from '@/hooks/common/useApi';
import { userApi } from '@/api/userApi';
import type {
    UserProfileResponse,
    UserUpdateRequest,
    UserPasswordUpdateRequest,
    UserEmailChangeRequest
} from '@/types/user';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

const CACHE_CONFIG = {
    PROFILE: {
        key: 'user_profile',
        time: 5 * 60 * 1000,
    }
} as const;

export const useUser = () => {
    const profileApi = useApi<UserProfileResponse>();
    const mutationApi = useApi<UserProfileResponse | { message: string }>();

    const rawLoading = profileApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(profileApi.error || mutationApi.error);

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
        return response || null;
    }, [profileApi]);

    const updateProfile = useCallback(async (data: UserUpdateRequest) => {
        const response = await mutationApi.execute(
            () => userApi.updateProfile(data),
            {
                successMessage: 'Profile updated successfully',
                showErrorNotification: false
            }
        );

        profileApi.invalidateCache(CACHE_CONFIG.PROFILE.key);

        if (response) {
            profileApi.setData(response);
        }

        return response || null;
    }, [mutationApi, profileApi]);

    const updatePassword = useCallback(async (data: UserPasswordUpdateRequest) => {
        const response = await mutationApi.execute(
            () => userApi.updatePassword(data),
            {
                successMessage: 'Password updated successfully',
                showErrorNotification: false
            }
        );
        return response || null;
    }, [mutationApi]);

    const requestEmailChange = useCallback(async (newEmail: string, password: string) => {
        const request: UserEmailChangeRequest = {
            newEmail,
            password
        };
        const response = await mutationApi.execute(
            () => userApi.requestEmailChange(request),
            {
                successMessage: 'Confirmation email sent to your new address',
                showErrorNotification: false
            }
        );
        return response || null;
    }, [mutationApi]);

    const clearCache = useCallback(() => {
        profileApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [profileApi, mutationApi]);

    const resetAll = useCallback(() => {
        profileApi.reset();
        mutationApi.reset();
    }, [profileApi, mutationApi]);

    return {
        profile: profileApi.data,

        loading,
        error,
        isProfileUpdating: mutationApi.loading,
        isPasswordUpdating: mutationApi.loading,
        isEmailChanging: mutationApi.loading,

        getProfile,
        updateProfile,
        updatePassword,
        requestEmailChange,

        clearCache,
        resetAll,
    };
};