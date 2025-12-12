import React from 'react';
import { UserTableRow } from '../UserTableRow/UserTableRow';
import type { AdminUser } from '@/types/user';
import styles from './UserTable.module.css';

interface UserTableProps {
    users: AdminUser[];
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
        <div className={styles.tableContainer}>
            <table className={styles.table}>
                <thead>
                    <tr>
                        <th>User</th>
                        <th>Role</th>
                        <th>Verification</th>
                        <th>Status</th>
                        <th>Registration</th>
                        <th>Tickets</th>
                        <th>Last Activity</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
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
    );
};