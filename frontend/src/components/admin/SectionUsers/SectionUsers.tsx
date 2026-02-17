import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { UserTable } from './UserTable/UserTable';
import { UserFilters } from './UserFilters/UserFilters';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { Notification, Pagination, LoadingSpinner } from '@/components/ui';
import type { UserRole, VerificationStatus } from '@/types/user';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './SectionUsers.module.css';

export const SectionUsers: React.FC = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
    const [verificationStatusFilter, setVerificationStatusFilter] = useState<VerificationStatus | ''>('');
    const [enabledFilter, setEnabledFilter] = useState<string>('');
    const [currentPage, setCurrentPage] = useState(0);

    const {
        users,
        pagination,
        loading,
        getUsers,
        refreshUsers
    } = useAdminUsers();

    const { notifications, showNotification, hideNotification } = useNotification();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const initialLoadRef = useRef(false);
    const filterTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const loadUsers = useCallback(async (showLoading: boolean = true) => {
        try {
            const params = {
                search: searchQuery || undefined,
                role: roleFilter || undefined,
                verificationStatus: verificationStatusFilter || undefined,
                enabled: enabledFilter === '' ? undefined : enabledFilter === 'true',
                page: currentPage,
                size: 10
            };

            if (showLoading) {
                await getUsers(params);
            } else {
                await refreshUsers(params);
            }
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to load users';
            showNotification(message, 'error');
        }
    }, [searchQuery, roleFilter, verificationStatusFilter, enabledFilter, currentPage, getUsers, refreshUsers, showNotification]);

    useEffect(() => {
        if (!initialLoadRef.current) {
            initialLoadRef.current = true;
            loadUsers(true);
        }
    }, [loadUsers]);

    useEffect(() => {
        if (!initialLoadRef.current) return;

        if (filterTimeoutRef.current) {
            clearTimeout(filterTimeoutRef.current);
        }

        filterTimeoutRef.current = setTimeout(() => {
            loadUsers(true);
        }, 300);

        return () => {
            if (filterTimeoutRef.current) {
                clearTimeout(filterTimeoutRef.current);
            }
        };
    }, [searchQuery, roleFilter, verificationStatusFilter, enabledFilter, currentPage, loadUsers]);

    const handleSearch = useCallback((query: string) => {
        setSearchQuery(query);
        setCurrentPage(0);
    }, []);

    const handleRoleFilterChange = useCallback((value: string) => {
        setRoleFilter(value as UserRole | '');
        setCurrentPage(0);
    }, []);

    const handleVerificationStatusChange = useCallback((value: string) => {
        setVerificationStatusFilter(value as VerificationStatus | '');
        setCurrentPage(0);
    }, []);

    const handleEnabledFilterChange = useCallback((value: string) => {
        setEnabledFilter(value);
        setCurrentPage(0);
    }, []);

    const handlePageChange = useCallback((page: number) => {
        setCurrentPage(page);
    }, []);

    const handleError = useCallback((error: string) => {
        showNotification(error, 'error');
    }, [showNotification]);

    const handleSuccess = useCallback(async (message: string) => {
        showNotification(message, 'success');
        await loadUsers(false);
    }, [showNotification, loadUsers]);

    const handleRefresh = useCallback(async () => {
        await loadUsers(false);
    }, [loadUsers]);

    const displayRange = useMemo(() => {
        if (!pagination) return { start: 0, end: 0 };
        const start = pagination.number * pagination.size + 1;
        const end = Math.min((pagination.number + 1) * pagination.size, pagination.totalElements);
        return { start, end };
    }, [pagination]);

    if (showDelayedLoading && !users.length) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading users..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
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

            <div className={styles.content}>
                <UserTable
                    users={users}
                    onRefresh={handleRefresh}
                    onError={handleError}
                    onSuccess={handleSuccess}
                />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    <div className={styles.resultsInfo}>
                        <span>
                            Showing {displayRange.start}-{displayRange.end} of {pagination.totalElements} users
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
        </div>
    );
};