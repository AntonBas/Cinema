import { useCallback } from 'react';
import type {
    AdminUserListResponse,
    UserRole,
    VerificationStatus,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest,
    UserResponse
} from '@/types/user';
import type { PageResponse } from '@/types/pagination';
import { adminApi } from '@/api/adminApi';
import { useApi } from '@/hooks/common/useApi';

export const useAdminUsers = () => {
    const usersApi = useApi<PageResponse<AdminUserListResponse>>();
    const updateRoleApi = useApi<void>();
    const updateStatusApi = useApi<void>();
    const updateVerificationApi = useApi<UserResponse>();

    const getUsers = useCallback(async (params?: {
        page?: number;
        size?: number;
        search?: string;
        role?: UserRole;
        verificationStatus?: VerificationStatus;
        enabled?: boolean;
    }) => {
        return usersApi.execute(
            () => adminApi.getUsers(params || {}),
            {
                cacheKey: `admin_users_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [usersApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        const roleData: UserRoleUpdateRequest = { userRole };

        return updateRoleApi.execute(
            () => adminApi.updateUserRole(userId, roleData),
            {
                successMessage: 'User role updated successfully',
                onSuccess: () => {
                    usersApi.invalidateCache();
                },
            }
        );
    }, [updateRoleApi, usersApi]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        const statusData: UserStatusUpdateRequest = { enabled };

        return updateStatusApi.execute(
            () => adminApi.updateUserStatus(userId, statusData),
            {
                successMessage: enabled ? 'User activated successfully' : 'User deactivated successfully',
                onSuccess: () => {
                    usersApi.invalidateCache();
                },
            }
        );
    }, [updateStatusApi, usersApi]);

    const updateBirthDateVerification = useCallback(async (
        userId: number,
        verificationStatus: VerificationStatus
    ) => {
        const verificationData: VerificationBirthDateRequest = { verificationStatus };

        return updateVerificationApi.execute(
            () => adminApi.updateBirthDateVerification(userId, verificationData),
            {
                successMessage: 'Verification status updated successfully',
                onSuccess: () => {
                    usersApi.invalidateCache();
                },
            }
        );
    }, [updateVerificationApi, usersApi]);

    const clearCache = useCallback(() => {
        usersApi.invalidateCache();
        updateRoleApi.invalidateCache();
        updateStatusApi.invalidateCache();
        updateVerificationApi.invalidateCache();
    }, [usersApi, updateRoleApi, updateStatusApi, updateVerificationApi]);

    const loading = usersApi.loading || updateRoleApi.loading ||
        updateStatusApi.loading || updateVerificationApi.loading;

    const error = !!(usersApi.error || updateRoleApi.error ||
        updateStatusApi.error || updateVerificationApi.error);

    return {
        users: usersApi.data?.content || [],
        pagination: usersApi.data,

        loading,
        error,
        isSuccess: !!usersApi.data,
        isCached: usersApi.isCached,

        getUsers,
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