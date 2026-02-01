import React, { useState, useEffect } from 'react';
import { UserTable } from './UserTable/UserTable';
import { UserFilters } from './UserFilters/UserFilters';
import { useAdminUsers } from '@/hooks/features';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification, Pagination } from '@/components/ui';
import styles from './SectionUsers.module.css';

export const SectionUsers: React.FC = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [currentPage, setCurrentPage] = useState(0);

    const {
        users,
        pagination,
        loading,
        fetchUsers
    } = useAdminUsers();

    const { notifications, hideNotification, showNotification } = useNotification();

    useEffect(() => {
        const enabledFilter = statusFilter === '' ? undefined : statusFilter === 'true';
        fetchUsers({
            query: searchQuery || undefined,
            role: roleFilter || undefined,
            enabled: enabledFilter,
            page: currentPage,
            size: 10
        });
    }, [searchQuery, roleFilter, statusFilter, currentPage]);

    const handleSearch = (query: string) => {
        setSearchQuery(query);
        setCurrentPage(0);
    };

    const handleRoleFilterChange = (value: string) => {
        setRoleFilter(value);
        setCurrentPage(0);
    };

    const handleStatusFilterChange = (value: string) => {
        setStatusFilter(value);
        setCurrentPage(0);
    };

    const handlePageChange = (page: number) => {
        setCurrentPage(page);
    };

    const handleError = (error: string) => {
        showNotification(error, 'error');
    };

    const handleSuccess = (message: string) => {
        showNotification(message, 'success');
    };

    const handleUserUpdate = () => {
        const enabledFilter = statusFilter === '' ? undefined : statusFilter === 'true';
        fetchUsers({
            query: searchQuery || undefined,
            role: roleFilter || undefined,
            enabled: enabledFilter,
            page: currentPage,
            size: 10
        });
    };

    const getDisplayRange = () => {
        if (!pagination) return { start: 0, end: 0 };

        const startItem = pagination.currentPage * pagination.pageSize + 1;
        const endItem = Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements);

        return { start: startItem, end: endItem };
    };

    const { start, end } = getDisplayRange();

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
                {loading ? (
                    <div className={styles.loading}>Loading users...</div>
                ) : (
                    <UserTable
                        users={users}
                        onRefresh={handleUserUpdate}
                        onError={handleError}
                        onSuccess={handleSuccess}
                    />
                )}
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    {pagination && (
                        <div className={styles.resultsInfo}>
                            <span>
                                Showing {start}-{end} of {pagination.totalElements} users
                                {searchQuery && ` for "${searchQuery}"`}
                            </span>
                        </div>
                    )}

                    <Pagination
                        currentPage={pagination.currentPage}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pagination.pageSize}
                        onPageChange={handlePageChange}
                        className={styles.pagination}
                        showInfo={false}
                    />
                </div>
            )}

            {notifications.map((notification, index) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={4000}
                    position={index}
                />
            ))}
        </div>
    );
};