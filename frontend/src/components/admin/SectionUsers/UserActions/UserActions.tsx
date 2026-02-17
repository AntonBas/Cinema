import React, { useState } from 'react';
import { Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import {
    type UserRole,
    type VerificationStatus,
    UserRoleDisplay,
    VerificationStatusDisplay
} from '@/types/user';
import type { AdminUserListResponse } from '@/types/user';
import styles from './UserActions.module.css';

interface UserActionsProps {
    user: AdminUserListResponse;
    onUpdate: () => void;
}

const getVerificationColor = (status: VerificationStatus): string => {
    return status === 'VERIFIED' ? 'verificationBadgeSuccess' : 'verificationBadgeSecondary';
};

export const UserActions: React.FC<UserActionsProps> = ({ user, onUpdate }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [roleLoading, setRoleLoading] = useState(false);
    const [statusLoading, setStatusLoading] = useState(false);
    const [verificationLoading, setVerificationLoading] = useState(false);

    const { updateUserRole, updateUserStatus, updateBirthDateVerification } = useAdminUsers();

    const handleRoleChange = async (value: string | number) => {
        const newRole = value as UserRole;
        if (newRole === user.userRole) return;

        setRoleLoading(true);
        try {
            await updateUserRole(user.id, newRole);
            setSelectedRole(newRole);
            onUpdate();
        } catch {
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

    const handleVerificationChange = async () => {
        setVerificationLoading(true);
        try {
            const newStatus: VerificationStatus = user.verificationStatus === 'VERIFIED' ? 'NOT_VERIFIED' : 'VERIFIED';
            await updateBirthDateVerification(user.id, newStatus);
            setShowVerificationModal(false);
            onUpdate();
        } finally {
            setVerificationLoading(false);
        }
    };

    const roleOptions = Object.entries(UserRoleDisplay).map(([value, label]) => ({
        value,
        label,
    }));

    const isLoading = roleLoading || statusLoading || verificationLoading;
    const isVerified = user.verificationStatus === 'VERIFIED';
    const isEnabled = user.enabled;

    return (
        <div className={styles.actions}>
            <div className={styles.verificationSection}>
                <label className={styles.label}>Verification:</label>
                <span className={`${styles.verificationBadge} ${styles[getVerificationColor(user.verificationStatus)]}`}>
                    {VerificationStatusDisplay[user.verificationStatus]}
                </span>
                <Button
                    variant="secondary"
                    size="small"
                    loading={verificationLoading}
                    onClick={() => setShowVerificationModal(true)}
                    disabled={isLoading}
                    className={styles.verificationButton}
                >
                    {isVerified ? 'Revoke' : 'Verify'}
                </Button>
            </div>

            <div className={styles.roleSection}>
                <label className={styles.label}>Role:</label>
                <Select
                    value={selectedRole}
                    onChange={handleRoleChange}
                    options={roleOptions}
                    disabled={isLoading}
                    className={styles.roleSelect}
                />
                {roleLoading && <span className={styles.loadingText}>Updating...</span>}
            </div>

            <div className={styles.statusSection}>
                <Button
                    variant={isEnabled ? 'error' : 'success'}
                    size="small"
                    loading={statusLoading}
                    onClick={() => setShowStatusModal(true)}
                    disabled={isLoading}
                    className={styles.statusButton}
                >
                    {isEnabled ? 'Block User' : 'Activate User'}
                </Button>
            </div>

            <ConfirmModal
                isOpen={showStatusModal}
                onConfirm={handleStatusChange}
                onCancel={() => setShowStatusModal(false)}
                title={isEnabled ? 'Block User' : 'Activate User'}
                message={`Are you sure you want to ${isEnabled ? 'block' : 'activate'} ${user.firstName} ${user.lastName}?`}
                confirmText={isEnabled ? 'Block User' : 'Activate User'}
                variant={isEnabled ? 'error' : 'success'}
                isLoading={statusLoading}
            />

            <ConfirmModal
                isOpen={showVerificationModal}
                onConfirm={handleVerificationChange}
                onCancel={() => setShowVerificationModal(false)}
                title={isVerified ? 'Revoke Verification' : 'Verify User'}
                message={`Are you sure you want to ${isVerified ? 'revoke verification from' : 'verify'} ${user.firstName} ${user.lastName}?`}
                confirmText={isVerified ? 'Revoke' : 'Verify'}
                variant={isVerified ? 'error' : 'success'}
                isLoading={verificationLoading}
            />
        </div>
    );
};