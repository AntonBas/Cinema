import { useState } from 'react';
import type { AdminUser, AdminUsersResponse, UserRole } from '@/types/user';
import { adminApi } from '@/api/adminApi';

interface PaginationInfo {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
}

interface UseAdminUsersReturn {
    users: AdminUser[];
    pagination: PaginationInfo | null;
    loading: boolean;
    error: string | null;
    searchUsers: (params: { query?: string; role?: string; enabled?: boolean; page?: number; size?: number }) => Promise<void>;
    refreshUsers: () => void;
    updateUserRoleLocal: (userId: number, userRole: UserRole) => void;
    updateUserStatusLocal: (userId: number, enabled: boolean) => void;
}

export const useAdminUsers = (): UseAdminUsersReturn => {
    const [users, setUsers] = useState<AdminUser[]>([]);
    const [pagination, setPagination] = useState<PaginationInfo | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchUsers = async (params: { query?: string; role?: string; enabled?: boolean; page?: number; size?: number } = {}) => {
        setLoading(true);
        setError(null);
        try {
            const { query, role, enabled, page = 0, size = 10 } = params;
            const response: AdminUsersResponse = await adminApi.getUsers(page, size, query, role, enabled);

            setUsers(response.content);
            setPagination({
                currentPage: response.number,
                totalPages: response.totalPages,
                totalElements: response.totalElements,
                pageSize: response.size
            });
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load users';
            setError(message);
        } finally {
            setLoading(false);
        }
    };

    const refreshUsers = () => {
        if (pagination) {
            searchUsers({
                page: pagination.currentPage,
                size: pagination.pageSize
            });
        }
    };

    const updateUserRoleLocal = (userId: number, userRole: UserRole) => {
        setUsers(prevUsers =>
            prevUsers.map(user =>
                user.id === userId ? { ...user, userRole } : user
            )
        );
    };

    const updateUserStatusLocal = (userId: number, enabled: boolean) => {
        setUsers(prevUsers =>
            prevUsers.map(user =>
                user.id === userId ? { ...user, enabled } : user
            )
        );
    };

    return {
        users,
        pagination,
        loading,
        error,
        searchUsers,
        refreshUsers,
        updateUserRoleLocal,
        updateUserStatusLocal
    };
};