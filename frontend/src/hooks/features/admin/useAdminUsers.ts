import { useCallback, useRef } from 'react';
import type {
    AdminUserListResponse,
    UserRole,
    VerificationStatus,
    UserRoleUpdateRequest,
    UserStatusUpdateRequest,
    VerificationBirthDateRequest,
} from '@/types/user';
import type { PageResponse } from '@/types/pagination';
import { adminApi } from '@/api/adminApi';
import { useApi } from '@/hooks/common/useApi';

export const useAdminUsers = () => {
    const usersApi = useApi<PageResponse<AdminUserListResponse>>();
    const updateRoleApi = useApi<AdminUserListResponse>();
    const updateStatusApi = useApi<AdminUserListResponse>();
    const updateVerificationApi = useApi<AdminUserListResponse>();

    const pendingRequestRef = useRef<Promise<PageResponse<AdminUserListResponse> | null> | null>(null);

    const getUsers = useCallback(async (params?: {
        page?: number;
        size?: number;
        search?: string;
        role?: UserRole;
        verificationStatus?: VerificationStatus;
        enabled?: boolean;
    }) => {
        const cacheKey = `admin_users_${JSON.stringify(params || {})}`;

        if (pendingRequestRef.current) {
            return pendingRequestRef.current;
        }

        const request = usersApi.execute(
            () => adminApi.getUsers(params || {}),
            {
                cacheKey,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        ).then(response => response?.data || null);

        pendingRequestRef.current = request;

        try {
            return await request;
        } finally {
            pendingRequestRef.current = null;
        }
    }, [usersApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        const roleData: UserRoleUpdateRequest = { userRole };

        const response = await updateRoleApi.execute(
            () => adminApi.updateUserRole(userId, roleData),
            {
                successMessage: 'User role updated successfully',
            }
        );

        usersApi.invalidateCache();
        return response?.data || null;
    }, [updateRoleApi, usersApi]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        const statusData: UserStatusUpdateRequest = { enabled };

        const response = await updateStatusApi.execute(
            () => adminApi.updateUserStatus(userId, statusData),
            {
                successMessage: enabled ? 'User activated successfully' : 'User deactivated successfully',
            }
        );

        usersApi.invalidateCache();
        return response?.data || null;
    }, [updateStatusApi, usersApi]);

    const updateBirthDateVerification = useCallback(async (
        userId: number,
        verificationStatus: VerificationStatus
    ) => {
        const verificationData: VerificationBirthDateRequest = { verificationStatus };

        const response = await updateVerificationApi.execute(
            () => adminApi.updateBirthDateVerification(userId, verificationData),
            {
                successMessage: 'Verification status updated successfully',
            }
        );

        usersApi.invalidateCache();
        return response?.data || null;
    }, [updateVerificationApi, usersApi]);

    const refreshUsers = useCallback(async (params?: {
        page?: number;
        size?: number;
        search?: string;
        role?: UserRole;
        verificationStatus?: VerificationStatus;
        enabled?: boolean;
    }) => {
        usersApi.invalidateCache();
        return getUsers(params);
    }, [usersApi, getUsers]);

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