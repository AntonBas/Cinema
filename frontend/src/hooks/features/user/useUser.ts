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

export const useUser = () => {
    const profileApi = useApi<UserProfileResponse>();
    const updateProfileApi = useApi<UserProfileResponse>();
    const passwordApi = useApi<{ message: string }>();
    const emailApi = useApi<{ message: string }>();

    const loading = useDelayedLoading(
        profileApi.loading || updateProfileApi.loading || passwordApi.loading || emailApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getProfile = useCallback(async () => {
        return profileApi.execute(() => userApi.getProfile());
    }, [profileApi]);

    const updateProfile = useCallback(async (data: UserUpdateRequest) => {
        const response = await updateProfileApi.execute(
            () => userApi.updateProfile(data),
            { successMessage: 'Profile updated successfully' }
        );
        if (response) {
            profileApi.setData(response);
        }
        return response;
    }, [updateProfileApi, profileApi]);

    const updatePassword = useCallback(async (data: UserPasswordUpdateRequest) => {
        return passwordApi.execute(
            () => userApi.updatePassword(data),
            { successMessage: 'Password updated successfully' }
        );
    }, [passwordApi]);

    const requestEmailChange = useCallback(async (newEmail: string, password: string) => {
        const request: UserEmailChangeRequest = { newEmail, password };
        return emailApi.execute(
            () => userApi.requestEmailChange(request),
            { successMessage: 'Confirmation email sent to your new address' }
        );
    }, [emailApi]);

    return {
        profile: profileApi.data,
        loading,
        profileError: profileApi.error,
        updateError: updateProfileApi.error,
        passwordError: passwordApi.error,
        emailError: emailApi.error,
        getProfile,
        updateProfile,
        updatePassword,
        requestEmailChange,
    };
};