import React, { useState, useEffect, useCallback } from 'react';
import { UserTable } from './UserTable/UserTable';
import { UserFilters } from './UserFilters/UserFilters';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { Notification, Pagination, LoadingSpinner } from '@/components/ui';
import type { UserRole } from '@/types/user';
import styles from './SectionUsers.module.css';

export const SectionUsers: React.FC = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
    const [statusFilter, setStatusFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(0);

    const {
        users,
        pagination,
        loading,
        getUsers
    } = useAdminUsers();

    const { notifications, hideNotification, showNotification } = useNotification();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    useEffect(() => {
        const enabledFilter = statusFilter === '' ? undefined : statusFilter === 'true';
        getUsers({
            search: searchQuery || undefined,
            role: roleFilter || undefined,
            enabled: enabledFilter,
            page: currentPage,
            size: 10
        });
    }, [searchQuery, roleFilter, statusFilter, currentPage]);

    const handleSearch = useCallback((query: string) => {
        setSearchQuery(query);
        setCurrentPage(0);
    }, []);

    const handleRoleFilterChange = useCallback((value: string) => {
        setRoleFilter(value as UserRole | '');
        setCurrentPage(0);
    }, []);

    const handleStatusFilterChange = useCallback((value: string) => {
        setStatusFilter(value);
        setCurrentPage(0);
    }, []);

    const handlePageChange = useCallback((page: number) => {
        setCurrentPage(page);
    }, []);

    const handleError = useCallback((error: string) => {
        showNotification(error, 'error');
    }, [showNotification]);

    const handleSuccess = useCallback((message: string) => {
        showNotification(message, 'success');
    }, [showNotification]);

    const handleUserUpdate = useCallback(() => {
        const enabledFilter = statusFilter === '' ? undefined : statusFilter === 'true';
        getUsers({
            search: searchQuery || undefined,
            role: roleFilter || undefined,
            enabled: enabledFilter,
            page: currentPage,
            size: 10
        });
    }, [searchQuery, roleFilter, statusFilter, currentPage]);

    const getDisplayRange = useCallback(() => {
        if (!pagination) return { start: 0, end: 0 };
        const startItem = pagination.number * pagination.size + 1;
        const endItem = Math.min((pagination.number + 1) * pagination.size, pagination.totalElements);
        return { start: startItem, end: endItem };
    }, [pagination]);

    const { start, end } = getDisplayRange();

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
                    statusFilter={statusFilter}
                    onStatusFilterChange={handleStatusFilterChange}
                />
            </div>

            <div className={styles.content}>
                <UserTable
                    users={users}
                    onRefresh={handleUserUpdate}
                    onError={handleError}
                    onSuccess={handleSuccess}
                />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    <div className={styles.resultsInfo}>
                        <span>
                            Showing {start}-{end} of {pagination.totalElements} users
                            {searchQuery && ` for "${searchQuery}"`}
                        </span>
                    </div>

                    <Pagination
                        currentPage={pagination.number}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pagination.size}
                        onPageChange={handlePageChange}
                        className={styles.pagination}
                        showInfo={false}
                    />
                </div>
            )}

            {notifications.map((notification) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={4000}
                />
            ))}
        </div>
    );
};