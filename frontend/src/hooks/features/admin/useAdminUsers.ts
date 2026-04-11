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

    const loading = useDelayedLoading(
        usersApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getUserName = useCallback((userId: number): string => {
        const user = usersApi.data?.content?.find(u => u.id === userId);
        return user ? `${user.firstName} ${user.lastName}` : 'User';
    }, [usersApi.data]);

    const getUsers = useCallback(async (params?: AdminUsersParams) => {
        return usersApi.execute(() => adminApi.getUsers(params || {}));
    }, [usersApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        const roleData: UserRoleUpdateRequest = { userRole };
        return mutationApi.execute(
            () => adminApi.updateUserRole(userId, roleData),
            { successMessage: `${getUserName(userId)} role updated successfully` }
        );
    }, [mutationApi, getUserName]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        const statusData: UserStatusUpdateRequest = { enabled };
        return mutationApi.execute(
            () => adminApi.updateUserStatus(userId, statusData),
            {
                successMessage: enabled
                    ? `${getUserName(userId)} activated successfully`
                    : `${getUserName(userId)} deactivated successfully`,
            }
        );
    }, [mutationApi, getUserName]);

    const updateBirthDateVerification = useCallback(async (
        userId: number,
        verificationStatus: VerificationStatus
    ) => {
        const verificationData: VerificationBirthDateRequest = { verificationStatus };
        const statusText = verificationStatus === 'VERIFIED' ? 'verified' : 'unverified';
        return mutationApi.execute(
            () => adminApi.updateBirthDateVerification(userId, verificationData),
            { successMessage: `${getUserName(userId)} birth date ${statusText}` }
        );
    }, [mutationApi, getUserName]);

    return {
        users: usersApi.data?.content || [],
        pagination: usersApi.data,
        loading,
        error: usersApi.error || mutationApi.error,
        getUsers,
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification,
        reset: usersApi.reset,
    };
};