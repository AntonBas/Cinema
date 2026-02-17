import React, { useMemo } from 'react';
import { UserTableRow } from '../UserTableRow/UserTableRow';
import type { AdminUserListResponse } from '@/types/user';
import styles from './UserTable.module.css';

interface UserTableProps {
    users: AdminUserListResponse[];
    onRefresh: () => void;
    onError: (error: string) => void;
    onSuccess: (message: string) => void;
}

export const UserTable: React.FC<UserTableProps> = ({
    users,
    onRefresh,
    onError,
    onSuccess,
}) => {
    const tableHeaders = useMemo(() => [
        'User',
        'Role',
        'Verification',
        'Status',
        'Registration',
        'Tickets',
        'Last Activity',
        'Actions'
    ], []);

    if (users.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>👥</div>
                <h3>No users found</h3>
                <p>There are no users matching your criteria.</p>
            </div>
        );
    }

    return (
        <div className={styles.tableWrapper}>
            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead className={styles.tableHead}>
                        <tr>
                            {tableHeaders.map((header) => (
                                <th key={header}>{header}</th>
                            ))}
                        </tr>
                    </thead>
                    <tbody className={styles.tableBody}>
                        {users.map((user) => (
                            <UserTableRow
                                key={user.id}
                                user={user}
                                onUpdate={onRefresh}
                                onError={onError}
                                onSuccess={onSuccess}
                            />
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};