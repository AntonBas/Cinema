import React from 'react';
import { Badge } from '@/components/ui';
import type { AuditLogResponse } from '@/types/audit';
import { getEntityTypeDisplay, getActionDisplay } from '@/types/audit';
import styles from './AuditLogsTable.module.css';

interface AuditLogsTableProps {
    logs: AuditLogResponse[];
    loading: boolean;
}

export const AuditLogsTable: React.FC<AuditLogsTableProps> = ({ logs, loading }) => {
    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <div className={styles.loadingSpinner} />
                <p>Loading audit logs...</p>
            </div>
        );
    }

    if (logs.length === 0) {
        return (
            <div className={styles.emptyContainer}>
                <p className={styles.emptyText}>No audit logs found</p>
            </div>
        );
    }

    const getBadgeVariant = (action: string): 'success' | 'error' | 'warning' | 'info' => {
        if (action.includes('CREATED') || action.includes('REGISTER')) return 'success';
        if (action.includes('DELETED') || action.includes('REJECTED')) return 'error';
        if (action.includes('SUCCESS') || action.includes('CONFIRMED')) return 'success';
        if (action.includes('FAILED') || action.includes('CANCELLED')) return 'error';
        if (action.includes('UPDATED') || action.includes('TOGGLE')) return 'warning';
        if (action.includes('REFUND') || action.includes('POINTS')) return 'warning';
        return 'info';
    };

    return (
        <div className={styles.tableWrapper}>
            <table className={styles.table}>
                <thead className={styles.tableHead}>
                    <tr>
                        <th className={styles.th}>Time</th>
                        <th className={styles.th}>User</th>
                        <th className={styles.th}>Entity Type</th>
                        <th className={styles.th}>Action</th>
                        <th className={styles.th}>Entity ID</th>
                        <th className={styles.th}>Changes</th>
                    </tr>
                </thead>
                <tbody className={styles.tableBody}>
                    {logs.map(log => (
                        <tr key={log.id} className={styles.tr}>
                            <td className={styles.td}>
                                {new Date(log.changedAt).toLocaleString()}
                            </td>
                            <td className={styles.td}>{log.changedBy}</td>
                            <td className={styles.td}>{getEntityTypeDisplay(log.entityType)}</td>
                            <td className={styles.td}>
                                <Badge variant={getBadgeVariant(log.action)}>
                                    {getActionDisplay(log.action)}
                                </Badge>
                            </td>
                            <td className={styles.td}>{log.entityId}</td>
                            <td className={styles.td}>
                                {log.oldValue && (
                                    <div className={styles.oldValue}>
                                        Old: {log.oldValue.length > 100 ? log.oldValue.substring(0, 100) + '...' : log.oldValue}
                                    </div>
                                )}
                                {log.newValue && (
                                    <div className={styles.newValue}>
                                        New: {log.newValue.length > 100 ? log.newValue.substring(0, 100) + '...' : log.newValue}
                                    </div>
                                )}
                                {!log.oldValue && !log.newValue && '-'}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};