import React, { useState } from 'react';
import { Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUserMutations } from '@/hooks/features';
import { type UserRole, UserRoleDisplay } from '@/types/user';
import type { AdminUser } from '@/types/user';
import styles from './UserActions.module.css';

interface UserActionsProps {
    user: AdminUser;
    onUpdate: () => void;
}

export const UserActions: React.FC<UserActionsProps> = ({ user, onUpdate }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [roleLoading, setRoleLoading] = useState(false);
    const [statusLoading, setStatusLoading] = useState(false);

    const { updateUserRole, updateUserStatus } = useAdminUserMutations();

    const handleRoleChange = async (value: string | number) => {
        const newRole = value as UserRole;
        setRoleLoading(true);
        try {
            await updateUserRole(user.id, newRole);
            setSelectedRole(newRole);
            onUpdate();
        } catch (error) {
            setSelectedRole(user.userRole);
        } finally {
            setRoleLoading(false);
        }
    };

    const handleStatusChange = async () => {
        setStatusLoading(true);
        try {
            await updateUserStatus(user.id, !user.enabled);
            setShowStatusModal(false);
            onUpdate();
        } finally {
            setStatusLoading(false);
        }
    };

    const roleOptions = Object.entries(UserRoleDisplay).map(([value, label]) => ({
        value,
        label,
    }));

    return (
        <div className={styles.actions}>
            <div className={styles.roleSection}>
                <label className={styles.label}>Role:</label>
                <Select
                    value={selectedRole}
                    onChange={handleRoleChange}
                    options={roleOptions}
                    disabled={roleLoading || statusLoading}
                    className={styles.roleSelect}
                />
                {roleLoading && <span className={styles.loadingText}>Updating...</span>}
            </div>

            <div className={styles.statusSection}>
                <Button
                    variant={user.enabled ? 'error' : 'success'}
                    size="small"
                    loading={statusLoading}
                    onClick={() => setShowStatusModal(true)}
                    disabled={roleLoading}
                    className={styles.statusButton}
                >
                    {user.enabled ? 'Block User' : 'Activate User'}
                </Button>
            </div>

            <ConfirmModal
                isOpen={showStatusModal}
                onConfirm={handleStatusChange}
                onCancel={() => setShowStatusModal(false)}
                title={user.enabled ? 'Block User' : 'Activate User'}
                message={`Are you sure you want to ${user.enabled ? 'block' : 'activate'} ${user.firstName} ${user.lastName}?`}
                confirmText={user.enabled ? 'Block User' : 'Activate User'}
                variant={user.enabled ? 'error' : 'success'}
                isLoading={statusLoading}
            />
        </div>
    );
};