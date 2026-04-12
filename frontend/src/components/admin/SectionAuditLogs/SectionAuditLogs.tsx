import React, { useEffect } from 'react';
import { useAuditLogs } from '@/hooks/features/audit/useAuditLogs';
import { AuditLogsFilters } from './AuditLogsFilters/AuditLogsFilters';
import { AuditLogsTable } from './AuditLogsTable/AuditLogsTable';
import { Pagination } from '@/components/ui';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import styles from './SectionAuditLogs.module.css';

export const SectionAuditLogs: React.FC = () => {
    const {
        auditLogs,
        pagination,
        loading,
        setPage,
        filters,
        applyFilters,
        clearFilters,
        refresh
    } = useAuditLogs();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const handleEntityTypeChange = (value: string) => {
        applyFilters({ entityType: value || undefined });
    };

    const handleActionChange = (value: string) => {
        applyFilters({ action: value || undefined });
    };

    const handleChangedByChange = (value: string) => {
        applyFilters({ changedBy: value || undefined });
    };

    const handleClearFilters = () => {
        clearFilters();
    };

    const entityTypes = ['User', 'Bonus', 'Promotion', 'TicketType', 'Movie', 'Session'];
    const actions = ['CREATED', 'UPDATED', 'DELETED', 'TOGGLE', 'CLAIMED', 'REFUNDED'];

    const hasActiveFilters = !!(filters.entityType || filters.action || filters.changedBy);

    useEffect(() => {
        refresh();
    }, [refresh]);

    if (showDelayedLoading && !auditLogs.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading audit logs..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div>
                    <h1 className={styles.title}>Audit Logs</h1>
                    <p className={styles.subtitle}>Track all changes and user activities in the system</p>
                </div>
            </div>

            <AuditLogsFilters
                entityType={filters.entityType || ''}
                action={filters.action || ''}
                changedBy={filters.changedBy || ''}
                onEntityTypeChange={handleEntityTypeChange}
                onActionChange={handleActionChange}
                onChangedByChange={handleChangedByChange}
                onClear={handleClearFilters}
                entityTypes={entityTypes}
                actions={actions}
            />

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {pagination.number * pagination.size + 1}-
                    {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                    {pagination.totalElements} audit logs
                    {hasActiveFilters && ' (filtered)'}
                </div>
            )}

            <div className={styles.tableContainer}>
                <AuditLogsTable logs={auditLogs} />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    <Pagination
                        currentPage={pagination.number}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pagination.size}
                        onPageChange={setPage}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}
        </div>
    );
};