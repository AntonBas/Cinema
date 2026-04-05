import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { UserTable } from './UserTable/UserTable';
import { UserFilters } from './UserFilters/UserFilters';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { Notification } from '@/components/ui/Notification/Notification';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { UserRole, VerificationStatus } from '@/types/user';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './SectionUsers.module.css';

export const SectionUsers: React.FC = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
    const [verificationStatusFilter, setVerificationStatusFilter] = useState<VerificationStatus | ''>('');
    const [enabledFilter, setEnabledFilter] = useState<string>('');

    const { params, setPage } = usePagination({ size: 10 });

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
    const filterTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const loadingDataRef = useRef(false);

    const loadUsers = useCallback(async (showLoading: boolean = true) => {
        if (loadingDataRef.current) return;

        loadingDataRef.current = true;

        try {
            const requestParams = {
                search: searchQuery || undefined,
                role: roleFilter || undefined,
                verificationStatus: verificationStatusFilter || undefined,
                enabled: enabledFilter === '' ? undefined : enabledFilter === 'true',
                page: params.page || 0,
                size: params.size || 10
            };

            if (showLoading) {
                await getUsers(requestParams);
            } else {
                await refreshUsers(requestParams);
            }
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to load users';
            showNotification(message, 'error');
        } finally {
            loadingDataRef.current = false;
        }
    }, [searchQuery, roleFilter, verificationStatusFilter, enabledFilter, params.page, params.size, getUsers, refreshUsers, showNotification]);

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
    }, [searchQuery, roleFilter, verificationStatusFilter, enabledFilter, params.page, loadUsers]);

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

    const handlePageChange = useCallback((page: number) => {
        setPage(page);
    }, [setPage]);

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

    const hasActiveFilters = searchQuery !== '' || roleFilter !== '' || verificationStatusFilter !== '' || enabledFilter !== '';

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

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {displayRange.start}-{displayRange.end} of {pagination.totalElements} users
                    {searchQuery && ` for "${searchQuery}"`}
                    {hasActiveFilters && ' (filtered)'}
                </div>
            )}

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
                    <Pagination
                        currentPage={params.page || 0}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={params.size || 10}
                        onPageChange={handlePageChange}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}
        </div>
    );
};