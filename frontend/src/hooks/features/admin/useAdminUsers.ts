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
        const cacheKey = `admin_users_${JSON.stringify(params || {})}`;

        const response = await usersApi.execute(
            () => adminApi.getUsers(params || {}),
            {
                cacheKey,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );

        return response || null;
    }, [usersApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        const roleData: UserRoleUpdateRequest = { userRole };

        const response = await mutationApi.execute(
            () => adminApi.updateUserRole(userId, roleData),
            {
                successMessage: 'User role updated successfully',
            }
        );

        usersApi.invalidateCache();
        return response || null;
    }, [mutationApi, usersApi]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        const statusData: UserStatusUpdateRequest = { enabled };

        const response = await mutationApi.execute(
            () => adminApi.updateUserStatus(userId, statusData),
            {
                successMessage: enabled ? 'User activated successfully' : 'User deactivated successfully',
            }
        );

        usersApi.invalidateCache();
        return response || null;
    }, [mutationApi, usersApi]);

    const updateBirthDateVerification = useCallback(async (
        userId: number,
        verificationStatus: VerificationStatus
    ) => {
        const verificationData: VerificationBirthDateRequest = { verificationStatus };

        const response = await mutationApi.execute(
            () => adminApi.updateBirthDateVerification(userId, verificationData),
            {
                successMessage: 'Verification status updated successfully',
            }
        );

        usersApi.invalidateCache();
        return response || null;
    }, [mutationApi, usersApi]);

    const refreshUsers = useCallback(async (params?: AdminUsersParams) => {
        usersApi.invalidateCache();
        return getUsers(params);
    }, [usersApi, getUsers]);

    const clearCache = useCallback(() => {
        usersApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [usersApi, mutationApi]);

    return {
        users: usersApi.data?.content || [],
        pagination: usersApi.data,
        loading,
        error,
        isSuccess: !!usersApi.data,
        isCached: usersApi.isCached,

        getUsers,
        refreshUsers,
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification,
        clearCache,
        resetUsers: usersApi.reset,

        currentPage: usersApi.data?.number || 0,
        totalPages: usersApi.data?.totalPages || 0,
        totalElements: usersApi.data?.totalElements || 0,
        pageSize: usersApi.data?.size || 10,
        isEmpty: usersApi.data?.empty || false,
        isFirstPage: usersApi.data?.first || true,
        isLastPage: usersApi.data?.last || true,
    };
};