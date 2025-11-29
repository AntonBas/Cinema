import React, { useState, useEffect } from 'react';
import { Badge, Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUserMutations } from '@/hooks/features';
import { UserRoleDisplay, UserStatusDisplay } from '@/types/user';
import type { AdminUser, UserRole } from '@/types/user';
import styles from './UserTableRow.module.css';

interface UserTableRowProps {
    user: AdminUser;
    onUpdate: () => void;
    onError: (error: string) => void;
    onSuccess: (message: string) => void;
}

export const UserTableRow: React.FC<UserTableRowProps> = ({ user, onUpdate, onError, onSuccess }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [currentUser, setCurrentUser] = useState<AdminUser>(user);

    useEffect(() => {
        setCurrentUser(user);
        setSelectedRole(user.userRole);
    }, [user]);

    const { updateUserRole, updateUserStatus, isLoading } = useAdminUserMutations();

    const handleRoleChange = async (value: string | number) => {
        const newRole = value as UserRole;

        if (newRole === currentUser.userRole) {
            return;
        }

        setSelectedRole(newRole);

        try {
            await updateUserRole(currentUser.id, newRole);
            setCurrentUser(prev => ({ ...prev, userRole: newRole }));
            onUpdate();
            onSuccess('User role updated successfully');
        } catch (error) {
            setSelectedRole(currentUser.userRole);
            const message = error instanceof Error ? error.message : 'Failed to update user role';
            onError(message);
        }
    };

    const handleStatusChange = async () => {
        try {
            await updateUserStatus(currentUser.id, !currentUser.enabled);
            setCurrentUser(prev => ({ ...prev, enabled: !prev.enabled }));
            setShowStatusModal(false);
            onUpdate();
            onSuccess(`User ${!currentUser.enabled ? 'activated' : 'blocked'} successfully`);
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update user status';
            onError(message);
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    };

    const roleOptions = Object.entries(UserRoleDisplay).map(([value, label]) => ({
        value,
        label,
    }));

    return (
        <>
            <tr className={styles.row}>
                <td className={styles.userCell}>
                    <div className={styles.userInfo}>
                        <div className={styles.userName}>
                            {currentUser.firstName} {currentUser.lastName}
                        </div>
                        <div className={styles.userEmail}>{currentUser.email}</div>
                    </div>
                </td>

                <td className={styles.roleCell}>
                    <Select
                        value={selectedRole}
                        onChange={handleRoleChange}
                        options={roleOptions}
                        disabled={isLoading}
                        className={styles.roleSelect}
                    />
                </td>

                <td className={styles.statusCell}>
                    <Badge
                        variant={currentUser.enabled ? 'success' : 'error'}
                        size="small"
                    >
                        {UserStatusDisplay[String(currentUser.enabled)]}
                    </Badge>
                </td>

                <td className={styles.dateCell}>
                    {formatDate(currentUser.createdAt)}
                </td>

                <td className={styles.ticketsCell}>
                    <span className={styles.ticketsCount}>{currentUser.ticketsCount}</span>
                </td>

                <td className={styles.actionsCell}>
                    <Button
                        variant={currentUser.enabled ? 'error' : 'success'}
                        size="small"
                        loading={isLoading}
                        onClick={() => setShowStatusModal(true)}
                        className={styles.actionButton}
                    >
                        {currentUser.enabled ? 'Block' : 'Activate'}
                    </Button>
                </td>
            </tr>

            <ConfirmModal
                isOpen={showStatusModal}
                onConfirm={handleStatusChange}
                onCancel={() => setShowStatusModal(false)}
                title={currentUser.enabled ? 'Block User' : 'Activate User'}
                message={`Are you sure you want to ${currentUser.enabled ? 'block' : 'activate'} ${currentUser.firstName} ${currentUser.lastName}?`}
                confirmText={currentUser.enabled ? 'Block User' : 'Activate User'}
                variant={currentUser.enabled ? 'error' : 'success'}
                isLoading={isLoading}
            />
        </>
    );
};