import { useState, useEffect } from 'react';
import type {
    UserProfile,
    UserUpdateRequest,
    EmailChangeResponse,
    PasswordUpdateResponse,
    UserPasswordUpdateRequest
} from '@/types/user';
import { userApi } from '@/api/userApi';
import { useApi } from '@/hooks/common/useApi';

export const useUser = () => {
    const [user, setUser] = useState<UserProfile | null>(null);

    const getProfileHook = useApi<UserProfile>();
    const updateProfileHook = useApi<UserProfile>();
    const requestEmailChangeHook = useApi<EmailChangeResponse>();
    const confirmEmailChangeHook = useApi<UserProfile>();
    const updatePasswordHook = useApi<PasswordUpdateResponse>();

    useEffect(() => {
        loadUserProfile();
    }, []);

    const loadUserProfile = async () => {
        return getProfileHook.callApi(async () => {
            const userData = await userApi.getProfile();
            setUser(userData);
            return userData;
        }, { showErrorNotification: false });
    };

    const updateProfile = async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        return updateProfileHook.callApi(async () => {
            const updatedUser = await userApi.updateProfile(updateData);
            setUser(updatedUser);
            return updatedUser;
        }, { showErrorNotification: false });
    };

    const requestEmailChange = async (newEmail: string): Promise<EmailChangeResponse> => {
        return requestEmailChangeHook.callApi(async () => {
            return await userApi.requestEmailChange(newEmail);
        }, { showErrorNotification: false });
    };

    const confirmEmailChange = async (token: string): Promise<UserProfile> => {
        return confirmEmailChangeHook.callApi(async () => {
            const updatedUser = await userApi.confirmEmailChange(token);
            setUser(updatedUser);
            return updatedUser;
        }, { showErrorNotification: false });
    };

    const updatePassword = async (currentPassword: string, newPassword: string, passwordConfirm: string): Promise<PasswordUpdateResponse> => {
        return updatePasswordHook.callApi(async () => {
            const passwordData: UserPasswordUpdateRequest = {
                currentPassword,
                newPassword,
                passwordConfirm
            };
            return await userApi.updatePassword(passwordData);
        }, { showErrorNotification: false });
    };

    const refreshUser = () => {
        loadUserProfile();
    };

    return {
        user,
        isLoading: getProfileHook.loading || updateProfileHook.loading ||
            requestEmailChangeHook.loading || confirmEmailChangeHook.loading ||
            updatePasswordHook.loading,
        updateProfile,
        requestEmailChange,
        confirmEmailChange,
        updatePassword,
        refreshUser,
    };
};