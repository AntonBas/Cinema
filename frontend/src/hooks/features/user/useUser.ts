import { useCallback, useRef, useEffect } from 'react';
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
    const initialLoadRef = useRef(false);
    const loadingPromiseRef = useRef<Promise<UserProfileResponse> | null>(null);

    const getProfile = useCallback(async (forceRefresh: boolean = false) => {
        if (forceRefresh) {
            profileApi.invalidateCache('user_profile');
        }

        if (!forceRefresh && profileApi.data) {
            return profileApi.data;
        }

        if (loadingPromiseRef.current) {
            return loadingPromiseRef.current;
        }

        const promise = profileApi.execute(
            () => userApi.getProfile(),
            {
                cacheKey: 'user_profile',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );

        loadingPromiseRef.current = promise;

        try {
            const result = await promise;
            return result;
        } finally {
            loadingPromiseRef.current = null;
        }
    }, [profileApi]);

    useEffect(() => {
        const token = localStorage.getItem('authToken');
        if (!initialLoadRef.current && token && !profileApi.data && !profileApi.loading) {
            initialLoadRef.current = true;
            getProfile();
        }
    }, [getProfile, profileApi.data, profileApi.loading]);

    const updateProfile = useCallback(async (updateData: UserUpdateRequest) => {
        const result = await profileApi.execute(
            () => userApi.updateProfile(updateData),
            {
                successMessage: 'Profile updated successfully',
            }
        );

        await getProfile(true);

        return result;
    }, [profileApi, getProfile]);

    const requestEmailChange = useCallback(async (newEmail: string) => {
        return emailChangeApi.execute(
            () => userApi.requestEmailChange(newEmail),
            {
                successMessage: 'Email change request sent. Check your new email.',
            }
        );
    }, [emailChangeApi]);

    const updatePassword = useCallback(async (passwordData: UserPasswordUpdateRequest) => {
        return passwordApi.execute(
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

    const loading = profileApi.loading || emailChangeApi.loading || passwordApi.loading;
    const error = !!(profileApi.error || emailChangeApi.error || passwordApi.error);

    return {
        user: profileApi.data,
        loading,
        error,
        isProfileLoading: profileApi.loading,
        isEmailChanging: emailChangeApi.loading,
        isPasswordUpdating: passwordApi.loading,
        getProfile,
        updateProfile,
        requestEmailChange,
        updatePassword,
        clearCache,
        logout,
        resetProfile: profileApi.reset,
        resetEmailChange: emailChangeApi.reset,
        resetPassword: passwordApi.reset,
    };
};