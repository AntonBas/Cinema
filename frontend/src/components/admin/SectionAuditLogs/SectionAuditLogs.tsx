// src/components/admin/SectionAuditLogs/SectionAuditLogs.tsx
import React, { useState, useEffect } from 'react';
import { useAuditLogs } from '@/hooks/features/audit/useAuditLogs';
import { AuditLogsFilters } from './AuditLogsFilters/AuditLogsFilters';
import { AuditLogsTable } from './AuditLogsTable/AuditLogsTable';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import styles from './SectionAuditLogs.module.css';

const ENTITY_TYPES = ['User', 'Booking', 'Payment', 'Refund', 'Bonus', 'Ticket', 'TicketType', 'Movie', 'Session', 'CinemaHall', 'Promotion', 'BonusRules'];
const ACTIONS = ['CREATED', 'UPDATED', 'DELETED', 'SUCCESS', 'FAILED', 'REFUND', 'CANCELLED', 'CONFIRMED', 'VALIDATED', 'CLAIMED', 'TOGGLE_STATUS', 'RESET_TO_DEFAULTS', 'REJECTED', 'RETRY', 'REACTIVATED', 'REGISTER', 'PASSWORD_CHANGED', 'PASSWORD_RESET_REQUESTED', 'PASSWORD_RESET_COMPLETED', 'EMAIL_CHANGE_REQUESTED', 'ROLE_CHANGED', 'STATUS_CHANGED', 'VERIFICATION_CHANGED', 'POINTS_ADDED', 'POINTS_SPENT', 'POINTS_ACCRUED', 'POINTS_REFUNDED'];

export const SectionAuditLogs: React.FC = () => {
    const [entityType, setEntityType] = useState('');
    const [action, setAction] = useState('');
    const [changedBy, setChangedBy] = useState('');

    const { auditLogs, pagination, loading, setPage, applyFilters, clearFilters, refresh } = useAuditLogs();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    useEffect(() => {
        applyFilters({ entityType: entityType || undefined, action: action || undefined, changedBy: changedBy || undefined });
    }, [entityType, action, changedBy]);

    const handleClearFilters = () => {
        setEntityType('');
        setAction('');
        setChangedBy('');
        clearFilters();
    };

    const displayRange = pagination ? {
        start: pagination.currentPage * pagination.pageSize + 1,
        end: Math.min((pagination.currentPage + 1) * pagination.pageSize, pagination.totalElements)
    } : { start: 0, end: 0 };

    const hasActiveFilters = entityType !== '' || action !== '' || changedBy !== '';

    if (showDelayedLoading && !auditLogs.length) {
        return (
            <div className={styles.loading}>
                <div className={styles.loadingSpinner} />
                <p>Loading audit logs...</p>
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
                <button onClick={refresh} className={styles.refreshButton}>
                    Refresh
                </button>
            </div>

            <AuditLogsFilters
                entityType={entityType}
                action={action}
                changedBy={changedBy}
                onEntityTypeChange={setEntityType}
                onActionChange={setAction}
                onChangedByChange={setChangedBy}
                onClear={handleClearFilters}
                entityTypes={ENTITY_TYPES}
                actions={ACTIONS}
            />

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {displayRange.start}-{displayRange.end} of {pagination.totalElements} audit logs
                    {hasActiveFilters && ' (filtered)'}
                </div>
            )}

            <div className={styles.tableContainer}>
                <AuditLogsTable logs={auditLogs} loading={loading} />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationWrapper}>
                    <Pagination
                        currentPage={pagination.currentPage}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pagination.pageSize}
                        onPageChange={setPage}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}
        </div>
    );
};