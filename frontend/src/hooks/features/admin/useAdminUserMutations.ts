import { useState } from 'react';
import type { UserRole } from '@/types/user';
import { adminApi } from '@/api/adminApi';

export const useAdminUserMutations = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateUserRole = async (userId: number, userRole: UserRole): Promise<void> => {
        setIsLoading(true);
        setError(null);
        try {
            await adminApi.updateUserRole(userId, { userRole });
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update user role';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const updateUserStatus = async (userId: number, enabled: boolean): Promise<void> => {
        setIsLoading(true);
        setError(null);
        try {
            await adminApi.updateUserStatus(userId, { enabled });
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update user status';
            setError(message);
            throw err;
        } finally {
            setIsLoading(false);
        }
    };

    const clearError = () => {
        setError(null);
    };

    return {
        isLoading,
        error,
        updateUserRole,
        updateUserStatus,
        clearError
    };
};