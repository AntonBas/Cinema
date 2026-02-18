import React, { useState, useMemo, useCallback } from 'react';
import { Badge, Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { UserRoleDisplay, VerificationStatusDisplay } from '@/types/user';
import type { AdminUserListResponse, UserRole, VerificationStatus } from '@/types/user';
import { toDisplayFormat } from '@/utils/dateUtils';
import styles from './UserTableRow.module.css';

interface UserTableRowProps {
    user: AdminUserListResponse;
    onUpdate: () => void;
    onError: (error: string) => void;
    onSuccess: (message: string) => void;
}

const getVerificationColor = (status: VerificationStatus): 'success' | 'secondary' => {
    return status === 'VERIFIED' ? 'success' : 'secondary';
};

const formatDateTime = (dateString: string | null | undefined): string => {
    if (!dateString) return 'Not verified';
    try {
        const date = new Date(dateString);
        const formattedDate = toDisplayFormat(date.toISOString().split('T')[0]);
        const time = date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        return `${formattedDate} ${time}`;
    } catch {
        return 'Not verified';
    }
};

const formatDate = (dateString: string | null | undefined): string => {
    if (!dateString) return '—';
    try {
        return toDisplayFormat(dateString.split('T')[0]);
    } catch {
        return '—';
    }
};

export const UserTableRow: React.FC<UserTableRowProps> = ({ user, onUpdate, onError, onSuccess }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [localUser, setLocalUser] = useState<AdminUserListResponse>(user);
    const [roleLoading, setRoleLoading] = useState(false);
    const [statusLoading, setStatusLoading] = useState(false);
    const [verificationLoading, setVerificationLoading] = useState(false);

    const {
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification
    } = useAdminUsers();

    const handleRoleChange = useCallback(async (value: string | number) => {
        const newRole = value as UserRole;
        if (newRole === localUser.userRole) return;

        setSelectedRole(newRole);
        setRoleLoading(true);

        try {
            await updateUserRole(localUser.id, newRole);
            setLocalUser(prev => ({ ...prev, userRole: newRole }));
            onSuccess('User role updated successfully');
            onUpdate();
        } catch (error) {
            setSelectedRole(localUser.userRole);
            const message = error instanceof Error ? error.message : 'Failed to update user role';
            onError(message);
        } finally {
            setRoleLoading(false);
        }
    }, [localUser.id, localUser.userRole, updateUserRole, onSuccess, onUpdate, onError]);

    const handleStatusChange = useCallback(async () => {
        setStatusLoading(true);
        try {
            const newEnabled = !localUser.enabled;
            await updateUserStatus(localUser.id, newEnabled);
            setLocalUser(prev => ({ ...prev, enabled: newEnabled }));
            setShowStatusModal(false);
            onSuccess(`User ${newEnabled ? 'activated' : 'blocked'} successfully`);
            onUpdate();
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update user status';
            onError(message);
        } finally {
            setStatusLoading(false);
        }
    }, [localUser.id, localUser.enabled, updateUserStatus, onSuccess, onUpdate, onError]);

    const handleVerificationChange = useCallback(async () => {
        setVerificationLoading(true);
        try {
            const newStatus: VerificationStatus = localUser.verificationStatus === 'VERIFIED' ? 'NOT_VERIFIED' : 'VERIFIED';
            await updateBirthDateVerification(localUser.id, newStatus);
            setLocalUser(prev => ({ ...prev, verificationStatus: newStatus }));
            setShowVerificationModal(false);
            onSuccess(`User verification ${newStatus === 'VERIFIED' ? 'granted' : 'revoked'} successfully`);
            onUpdate();
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update verification status';
            onError(message);
        } finally {
            setVerificationLoading(false);
        }
    }, [localUser.id, localUser.verificationStatus, updateBirthDateVerification, onSuccess, onUpdate, onError]);

    const roleOptions = useMemo(() =>
        Object.entries(UserRoleDisplay).map(([value, label]) => ({
            value,
            label,
        })),
        []);

    const isLoading = roleLoading || statusLoading || verificationLoading;
    const isVerified = localUser.verificationStatus === 'VERIFIED';
    const isEnabled = localUser.enabled;

    return (
        <>
            <tr className={styles.row}>
                <td className={styles.userCell}>
                    <div className={styles.userInfo}>
                        <div className={styles.userName}>
                            {localUser.firstName} {localUser.lastName}
                        </div>
                        <div className={styles.userEmail}>{localUser.email}</div>
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

                <td className={styles.verificationCell}>
                    <div className={styles.verificationInfo}>
                        <Badge
                            variant={getVerificationColor(localUser.verificationStatus)}
                            size="small"
                            className={styles.verificationBadge}
                        >
                            {VerificationStatusDisplay[localUser.verificationStatus]}
                        </Badge>
                        <div className={styles.verificationDate}>
                            {formatDateTime(localUser.verifiedAt)}
                        </div>
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
                </td>

                <td className={styles.statusCell}>
                    <Badge
                        variant={isEnabled ? 'success' : 'error'}
                        size="small"
                    >
                        {isEnabled ? 'Active' : 'Blocked'}
                    </Badge>
                </td>

                <td className={styles.dateCell}>
                    {formatDate(localUser.createdAt)}
                </td>

                <td className={styles.ticketsCell}>
                    <span className={styles.ticketsCount}>{localUser.ticketsCount ?? 0}</span>
                </td>

                <td className={styles.activityCell}>
                    {formatDate(localUser.lastActivity)}
                </td>

                <td className={styles.actionsCell}>
                    <Button
                        variant={isEnabled ? 'error' : 'success'}
                        size="small"
                        loading={statusLoading}
                        onClick={() => setShowStatusModal(true)}
                        disabled={isLoading}
                        className={styles.actionButton}
                    >
                        {isEnabled ? 'Block' : 'Activate'}
                    </Button>
                </td>
            </tr>

            <ConfirmModal
                isOpen={showStatusModal}
                onConfirm={handleStatusChange}
                onCancel={() => setShowStatusModal(false)}
                title={isEnabled ? 'Block User' : 'Activate User'}
                message={`Are you sure you want to ${isEnabled ? 'block' : 'activate'} ${localUser.firstName} ${localUser.lastName}?`}
                confirmText={isEnabled ? 'Block' : 'Activate'}
                variant={isEnabled ? 'error' : 'success'}
                isLoading={statusLoading}
            />

            <ConfirmModal
                isOpen={showVerificationModal}
                onConfirm={handleVerificationChange}
                onCancel={() => setShowVerificationModal(false)}
                title={isVerified ? 'Revoke Verification' : 'Verify User'}
                message={`Are you sure you want to ${isVerified ? 'revoke verification from' : 'verify'} ${localUser.firstName} ${localUser.lastName}?`}
                confirmText={isVerified ? 'Revoke' : 'Verify'}
                variant={isVerified ? 'error' : 'success'}
                isLoading={verificationLoading}
            />
        </>
    );
};