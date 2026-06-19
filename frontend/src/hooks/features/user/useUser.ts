import { useCallback, useRef } from 'react';
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

    const profileApiRef = useRef(profileApi);
    const updateProfileApiRef = useRef(updateProfileApi);
    const passwordApiRef = useRef(passwordApi);
    const emailApiRef = useRef(emailApi);

    profileApiRef.current = profileApi;
    updateProfileApiRef.current = updateProfileApi;
    passwordApiRef.current = passwordApi;
    emailApiRef.current = emailApi;

    const loading = useDelayedLoading(
        profileApi.loading || updateProfileApi.loading || passwordApi.loading || emailApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getProfile = useCallback(async () => {
        return profileApiRef.current.execute(() => userApi.getProfile());
    }, []);

    const updateProfile = useCallback(async (data: UserUpdateRequest) => {
        const response = await updateProfileApiRef.current.execute(
            () => userApi.updateProfile(data),
            { successMessage: 'Profile updated successfully' }
        );
        if (response) {
            profileApiRef.current.setData(response);
        }
        return response;
    }, []);

    const updatePassword = useCallback(async (data: UserPasswordUpdateRequest) => {
        return passwordApiRef.current.execute(
            () => userApi.updatePassword(data),
            { successMessage: 'Password updated successfully' }
        );
    }, []);

    const requestEmailChange = useCallback(async (newEmail: string, password: string) => {
        const request: UserEmailChangeRequest = { newEmail, password };
        return emailApiRef.current.execute(
            () => userApi.requestEmailChange(request),
            { successMessage: 'Confirmation email sent to your new address' }
        );
    }, []);

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