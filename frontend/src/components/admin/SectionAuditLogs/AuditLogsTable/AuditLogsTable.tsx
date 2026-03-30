import React from 'react';
import { Badge } from '@/components/ui';
import type { AuditLogResponse } from '@/types/audit';
import { getEntityTypeDisplay, getActionDisplay } from '@/types/audit';
import styles from './AuditLogsTable.module.css';

interface AuditLogsTableProps {
    logs: AuditLogResponse[];
    loading: boolean;
}

const formatChangeValue = (value: string | null): React.ReactNode => {
    if (!value) return null;

    try {
        const parsed = JSON.parse(value);

        if (typeof parsed === 'object' && parsed !== null) {
            return (
                <div className={styles.changeObject}>
                    {Object.entries(parsed).map(([key, val]) => (
                        <div key={key} className={styles.changeField}>
                            <span className={styles.changeKey}>{formatFieldName(key)}:</span>
                            <span className={styles.changeValue}>{String(val)}</span>
                        </div>
                    ))}
                </div>
            );
        }

        return <span>{String(parsed)}</span>;
    } catch {
        return <span>{value.length > 100 ? value.substring(0, 100) + '...' : value}</span>;
    }
};

const formatFieldName = (field: string): string => {
    const fieldMap: Record<string, string> = {
        'points': 'Points',
        'moneyRatio': 'Money Ratio',
        'minPointsPerTransaction': 'Min Points',
        'maxPointsPerTransaction': 'Max Points',
        'bonusType': 'Bonus Type',
        'active': 'Active',
        'role': 'Role',
        'enabled': 'Enabled',
        'status': 'Status',
        'title': 'Title',
        'description': 'Description',
        'startDate': 'Start Date',
        'endDate': 'End Date',
        'displayName': 'Display Name',
        'priceMultiplier': 'Price Multiplier',
        'minAge': 'Min Age',
        'maxAge': 'Max Age',
        'firstName': 'First Name',
        'lastName': 'Last Name',
        'email': 'Email',
        'phoneNumber': 'Phone Number',
        'city': 'City',
        'dateOfBirth': 'Date of Birth'
    };
    return fieldMap[field] || field;
};

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
                            <td className={styles.td}>
                                {log.oldValue && (
                                    <div className={styles.oldValue}>
                                        <span className={styles.changeLabel}>From:</span>
                                        {formatChangeValue(log.oldValue)}
                                    </div>
                                )}
                                {log.newValue && (
                                    <div className={styles.newValue}>
                                        <span className={styles.changeLabel}>To:</span>
                                        {formatChangeValue(log.newValue)}
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