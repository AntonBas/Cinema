import { useState, useCallback } from 'react';
import type { AdminUserListResponse, UserRole, VerificationStatus } from '@/types/user';
import { adminApi } from '@/api/adminApi';
import { useApi } from '@/hooks/common/useApi';

export const useAdminUsers = () => {
    const [users, setUsers] = useState<AdminUserListResponse[]>([]);
    const [pagination, setPagination] = useState<{
        currentPage: number;
        totalPages: number;
        totalElements: number;
        pageSize: number;
    } | null>(null);
    const [currentParams, setCurrentParams] = useState<{
        query?: string;
        role?: string;
        enabled?: boolean;
        page?: number;
        size?: number;
    }>({});

    const { loading: fetchLoading, callApi: fetchCallApi } = useApi<AdminUserListResponse[]>();
    const { loading: mutationLoading, callApi: mutationCallApi } = useApi<void>();

    const fetchUsers = useCallback(async (params: {
        query?: string;
        role?: string;
        enabled?: boolean;
        page?: number;
        size?: number;
    } = {}) => {
        const { query, role, enabled, page = 0, size = 10 } = params;
        setCurrentParams(params);

        return fetchCallApi(async () => {
            const response = await adminApi.getUsers(page, size, query, role, enabled);
            setUsers(response.content);
            setPagination({
                currentPage: response.number,
                totalPages: response.totalPages,
                totalElements: response.totalElements,
                pageSize: response.size
            });
            return response.content;
        }, { showErrorNotification: false });
    }, [fetchCallApi]);

    const updateUserRole = useCallback(async (userId: number, userRole: UserRole) => {
        return mutationCallApi(async () => {
            await adminApi.updateUserRole(userId, { userRole });
            setUsers(prevUsers => prevUsers.map(user =>
                user.id === userId ? { ...user, userRole } : user
            ));
        });
    }, [mutationCallApi]);

    const updateUserStatus = useCallback(async (userId: number, enabled: boolean) => {
        return mutationCallApi(async () => {
            await adminApi.updateUserStatus(userId, { enabled });
            setUsers(prevUsers => prevUsers.map(user =>
                user.id === userId ? { ...user, enabled } : user
            ));
        });
    }, [mutationCallApi]);

    const updateBirthDateVerification = useCallback(async (userId: number, verificationStatus: VerificationStatus) => {
        return mutationCallApi(async () => {
            const response = await adminApi.updateBirthDateVerification(userId, { verificationStatus });
            setUsers(prevUsers => prevUsers.map(user =>
                user.id === userId ? { ...user, verificationStatus: response.verificationStatus } : user
            ));
        });
    }, [mutationCallApi]);

    const refresh = useCallback(() => {
        if (Object.keys(currentParams).length > 0) {
            fetchUsers(currentParams);
        }
    }, [currentParams, fetchUsers]);

    const clearUsers = useCallback(() => {
        setUsers([]);
        setPagination(null);
    }, []);

    return {
        users,
        pagination,
        loading: fetchLoading || mutationLoading,
        fetchUsers,
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification,
        refresh,
        clearUsers
    };
};