import { useCallback } from 'react';
import type {
    AdminUserListResponse,
    UserRole,
    VerificationStatus,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest,
} from '@/types/user';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { adminApi } from '@/api/adminApi';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface AdminUsersParams extends SearchParams {
    role?: UserRole;
    verificationStatus?: VerificationStatus;
    enabled?: boolean;
}

export const useAdminUsers = () => {
    const usersApi = useApi<PageResponse<AdminUserListResponse>>();
    const mutationApi = useApi<AdminUserListResponse>();

    const rawLoading = usersApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(usersApi.error || mutationApi.error);

    const getUsers = useCallback(async (params?: AdminUsersParams) => {
        const response = await usersApi.execute(
            () => adminApi.getUsers(params || {}),
            {
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [usersApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        const roleData: UserRoleUpdateRequest = { userRole };

        const user = usersApi.data?.content?.find(u => u.id === userId);
        const userName = user ? `${user.firstName} ${user.lastName}` : 'User';

        const response = await mutationApi.execute(
            () => adminApi.updateUserRole(userId, roleData),
            {
                successMessage: `${userName} role updated successfully`,
            }
        );
        return response || null;
    }, [mutationApi, usersApi.data]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        const statusData: UserStatusUpdateRequest = { enabled };

        const user = usersApi.data?.content?.find(u => u.id === userId);
        const userName = user ? `${user.firstName} ${user.lastName}` : 'User';

        const response = await mutationApi.execute(
            () => adminApi.updateUserStatus(userId, statusData),
            {
                successMessage: enabled
                    ? `${userName} activated successfully`
                    : `${userName} deactivated successfully`,
            }
        );
        return response || null;
    }, [mutationApi, usersApi.data]);

    const updateBirthDateVerification = useCallback(async (
        userId: number,
        verificationStatus: VerificationStatus
    ) => {
        const verificationData: VerificationBirthDateRequest = { verificationStatus };

        const user = usersApi.data?.content?.find(u => u.id === userId);
        const userName = user ? `${user.firstName} ${user.lastName}` : 'User';
        const statusText = verificationStatus === 'VERIFIED' ? 'verified' : 'unverified';

        const response = await mutationApi.execute(
            () => adminApi.updateBirthDateVerification(userId, verificationData),
            {
                successMessage: `${userName} birth date ${statusText}`,
            }
        );
        return response || null;
    }, [mutationApi, usersApi.data]);

    const refreshUsers = useCallback(async (params?: AdminUsersParams) => {
        return getUsers(params);
    }, [getUsers]);

    return {
        users: usersApi.data?.content || [],
        pagination: usersApi.data,
        loading,
        error,
        isSuccess: !!usersApi.data,
        getUsers,
        refreshUsers,
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification,
        resetUsers: usersApi.reset,
    };
};