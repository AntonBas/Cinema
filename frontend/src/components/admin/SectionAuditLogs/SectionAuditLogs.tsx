import React, { useState, useEffect } from 'react';
import { useAuditLogs } from '@/hooks/features/audit/useAuditLogs';
import { AuditLogsFilters } from './AuditLogsFilters/AuditLogsFilters';
import { AuditLogsTable } from './AuditLogsTable/AuditLogsTable';
import { Pagination } from '@/components/ui';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { EntityTypeDisplay, ActionDisplay } from '@/types/audit';
import styles from './SectionAuditLogs.module.css';

const ENTITY_TYPES = Object.keys(EntityTypeDisplay);
const ACTIONS = Object.keys(ActionDisplay);

export const SectionAuditLogs: React.FC = () => {
    const [entityType, setEntityType] = useState('');
    const [action, setAction] = useState('');
    const [changedBy, setChangedBy] = useState('');

    const { auditLogs, pagination, loading, setPage, applyFilters, clearFilters } = useAuditLogs();

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
                <AuditLogsTable logs={auditLogs} />
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