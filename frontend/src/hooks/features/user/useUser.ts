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
    const getProfileApi = useApi<UserProfileResponse>();
    const updateProfileApi = useApi<UserProfileResponse>();
    const updatePasswordApi = useApi<{ message: string }>();
    const requestEmailChangeApi = useApi<{ message: string }>();

    const rawLoading = getProfileApi.loading || updateProfileApi.loading ||
        updatePasswordApi.loading || requestEmailChangeApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getProfileApi.error || updateProfileApi.error ||
        updatePasswordApi.error || requestEmailChangeApi.error);

    const getProfile = useCallback(async () => {
        const response = await getProfileApi.execute(
            () => userApi.getProfile(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getProfileApi]);

    const updateProfile = useCallback(async (data: UserUpdateRequest) => {
        const response = await updateProfileApi.execute(
            () => userApi.updateProfile(data),
            { successMessage: 'Profile updated successfully' }
        );

        if (response) {
            getProfileApi.setData(response);
        }

        return response || null;
    }, [updateProfileApi, getProfileApi]);

    const updatePassword = useCallback(async (data: UserPasswordUpdateRequest) => {
        const response = await updatePasswordApi.execute(
            () => userApi.updatePassword(data),
            { successMessage: 'Password updated successfully' }
        );
        return response || null;
    }, [updatePasswordApi]);

    const requestEmailChange = useCallback(async (newEmail: string, password: string) => {
        const request: UserEmailChangeRequest = { newEmail, password };
        const response = await requestEmailChangeApi.execute(
            () => userApi.requestEmailChange(request),
            { successMessage: 'Confirmation email sent to your new address' }
        );
        return response || null;
    }, [requestEmailChangeApi]);

    const resetAll = useCallback(() => {
        getProfileApi.reset();
        updateProfileApi.reset();
        updatePasswordApi.reset();
        requestEmailChangeApi.reset();
    }, [getProfileApi, updateProfileApi, updatePasswordApi, requestEmailChangeApi]);

    return {
        profile: getProfileApi.data,
        loading,
        error,
        isProfileUpdating: updateProfileApi.loading,
        isPasswordUpdating: updatePasswordApi.loading,
        isEmailChanging: requestEmailChangeApi.loading,
        getProfile,
        updateProfile,
        updatePassword,
        requestEmailChange,
        resetAll,
    };
};