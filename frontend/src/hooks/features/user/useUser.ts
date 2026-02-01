import { useState, useCallback, useRef, useEffect } from 'react';
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
    const isMounted = useRef(true);

    const getProfileHook = useApi<UserProfile>();
    const updateProfileHook = useApi<UserProfile>();
    const requestEmailChangeHook = useApi<EmailChangeResponse>();
    const confirmEmailChangeHook = useApi<UserProfile>();
    const updatePasswordHook = useApi<PasswordUpdateResponse>();

    useEffect(() => {
        return () => {
            isMounted.current = false;
        };
    }, []);

    const loadUserProfile = useCallback(async () => {
        return getProfileHook.callApi(async () => {
            const userData = await userApi.getProfile();
            if (isMounted.current) {
                setUser(userData);
            }
            return userData;
        }, { showErrorNotification: false });
    }, [getProfileHook]);

    const updateProfile = useCallback(async (updateData: UserUpdateRequest): Promise<UserProfile> => {
        return updateProfileHook.callApi(async () => {
            const updatedUser = await userApi.updateProfile(updateData);
            if (isMounted.current) {
                setUser(updatedUser);
            }
            return updatedUser;
        }, { showErrorNotification: false });
    }, [updateProfileHook]);

    const requestEmailChange = useCallback(async (newEmail: string): Promise<EmailChangeResponse> => {
        return requestEmailChangeHook.callApi(async () => {
            return await userApi.requestEmailChange(newEmail);
        }, { showErrorNotification: false });
    }, [requestEmailChangeHook]);

    const confirmEmailChange = useCallback(async (token: string): Promise<UserProfile> => {
        return confirmEmailChangeHook.callApi(async () => {
            const updatedUser = await userApi.confirmEmailChange(token);
            if (isMounted.current) {
                setUser(updatedUser);
            }
            return updatedUser;
        }, { showErrorNotification: false });
    }, [confirmEmailChangeHook]);

    const updatePassword = useCallback(async (currentPassword: string, newPassword: string, passwordConfirm: string): Promise<PasswordUpdateResponse> => {
        return updatePasswordHook.callApi(async () => {
            const passwordData: UserPasswordUpdateRequest = {
                currentPassword,
                newPassword,
                passwordConfirm
            };
            return await userApi.updatePassword(passwordData);
        }, { showErrorNotification: false });
    }, [updatePasswordHook]);

    const refreshUser = useCallback(() => {
        loadUserProfile();
    }, [loadUserProfile]);

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