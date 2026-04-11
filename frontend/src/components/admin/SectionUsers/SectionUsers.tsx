import React, { useState, useEffect, useCallback } from 'react';
import { UserTable } from './UserTable/UserTable';
import { UserFilters } from './UserFilters/UserFilters';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { UserRole, VerificationStatus } from '@/types/user';
import styles from './SectionUsers.module.css';

export const SectionUsers: React.FC = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
    const [verificationStatusFilter, setVerificationStatusFilter] = useState<VerificationStatus | ''>('');
    const [enabledFilter, setEnabledFilter] = useState<string>('');

    const { params, setPage } = usePagination({ size: 10 });
    const { users, pagination, loading, getUsers } = useAdminUsers();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const currentPage = params.page ?? 0;
    const pageSize = params.size ?? 10;

    const loadUsers = useCallback(() => {
        getUsers({
            search: searchQuery || undefined,
            role: roleFilter || undefined,
            verificationStatus: verificationStatusFilter || undefined,
            enabled: enabledFilter === '' ? undefined : enabledFilter === 'true',
            page: currentPage,
            size: pageSize
        });
    }, [searchQuery, roleFilter, verificationStatusFilter, enabledFilter, currentPage, pageSize, getUsers]);

    useEffect(() => {
        loadUsers();
    }, [loadUsers]);

    const handleSearch = useCallback((query: string) => {
        setSearchQuery(query);
        setPage(0);
    }, [setPage]);

    const handleRoleFilterChange = useCallback((value: string) => {
        setRoleFilter(value as UserRole | '');
        setPage(0);
    }, [setPage]);

    const handleVerificationStatusChange = useCallback((value: string) => {
        setVerificationStatusFilter(value as VerificationStatus | '');
        setPage(0);
    }, [setPage]);

    const handleEnabledFilterChange = useCallback((value: string) => {
        setEnabledFilter(value);
        setPage(0);
    }, [setPage]);

    if (showDelayedLoading && !users.length) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading users..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <header className={styles.header}>
                <h1>User Management</h1>
                <p>Manage user roles, verification status and account settings</p>
            </header>

            <div className={styles.searchSection}>
                <UserFilters
                    onSearchChange={handleSearch}
                    roleFilter={roleFilter}
                    onRoleFilterChange={handleRoleFilterChange}
                    verificationStatusFilter={verificationStatusFilter}
                    onVerificationStatusChange={handleVerificationStatusChange}
                    enabledFilter={enabledFilter}
                    onEnabledFilterChange={handleEnabledFilterChange}
                />
            </div>

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {pagination.number * pagination.size + 1}-
                    {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                    {pagination.totalElements} users
                </div>
            )}

            <div className={styles.content}>
                <UserTable users={users} onRefresh={loadUsers} />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    <Pagination
                        currentPage={currentPage}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pageSize}
                        onPageChange={setPage}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}
        </div>
    );
};