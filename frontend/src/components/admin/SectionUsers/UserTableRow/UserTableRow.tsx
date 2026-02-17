import React, { useState } from 'react';
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

export const UserTableRow: React.FC<UserTableRowProps> = ({ user, onUpdate, onError, onSuccess }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [localUser, setLocalUser] = useState<AdminUserListResponse>(user);

    const {
        updateUserRole,
        updateUserStatus,
        updateBirthDateVerification,
        refreshUsers,
        loading
    } = useAdminUsers();

    const handleRoleChange = async (value: string | number) => {
        const newRole = value as UserRole;

        if (newRole === localUser.userRole) {
            return;
        }

        setSelectedRole(newRole);

        try {
            await updateUserRole(localUser.id, newRole);
            setLocalUser(prev => ({ ...prev, userRole: newRole }));
            onSuccess('User role updated successfully');
            await refreshUsers();
            onUpdate();
        } catch (error) {
            setSelectedRole(localUser.userRole);
            const message = error instanceof Error ? error.message : 'Failed to update user role';
            onError(message);
        }
    };

    const handleStatusChange = async () => {
        try {
            const newEnabled = !localUser.enabled;
            await updateUserStatus(localUser.id, newEnabled);
            setLocalUser(prev => ({ ...prev, enabled: newEnabled }));
            setShowStatusModal(false);
            onSuccess(`User ${newEnabled ? 'activated' : 'blocked'} successfully`);
            await refreshUsers();
            onUpdate();
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update user status';
            onError(message);
        }
    };

    const handleVerificationChange = async () => {
        try {
            const newVerificationStatus: VerificationStatus = localUser.verificationStatus === 'VERIFIED' ? 'NOT_VERIFIED' : 'VERIFIED';
            await updateBirthDateVerification(localUser.id, newVerificationStatus);
            setLocalUser(prev => ({ ...prev, verificationStatus: newVerificationStatus }));
            setShowVerificationModal(false);
            onSuccess(`User verification ${newVerificationStatus === 'VERIFIED' ? 'granted' : 'revoked'} successfully`);
            await refreshUsers();
            onUpdate();
        } catch (error) {
            const message = error instanceof Error ? error.message : 'Failed to update verification status';
            onError(message);
        }
    };

    const formatDateTime = (dateString: string | null) => {
        if (!dateString) return 'Not verified';
        const date = new Date(dateString);
        const formattedDate = toDisplayFormat(date.toISOString().split('T')[0]);
        const time = date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        return `${formattedDate} ${time}`;
    };

    const roleOptions = Object.entries(UserRoleDisplay).map(([value, label]) => ({
        value,
        label,
    }));

    const getStatusDisplay = (enabled: boolean): string => {
        return enabled ? 'Active' : 'Blocked';
    };

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
                        disabled={loading}
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
                            loading={loading}
                            onClick={() => setShowVerificationModal(true)}
                            className={styles.verificationButton}
                        >
                            {localUser.verificationStatus === 'VERIFIED' ? 'Revoke' : 'Verify'}
                        </Button>
                    </div>
                </td>

                <td className={styles.statusCell}>
                    <Badge
                        variant={localUser.enabled ? 'success' : 'error'}
                        size="small"
                    >
                        {getStatusDisplay(localUser.enabled)}
                    </Badge>
                </td>

                <td className={styles.dateCell}>
                    {toDisplayFormat(localUser.createdAt.split('T')[0])}
                </td>

                <td className={styles.ticketsCell}>
                    <span className={styles.ticketsCount}>{localUser.ticketsCount}</span>
                </td>

                <td className={styles.activityCell}>
                    <div className={styles.activityInfo}>
                        {toDisplayFormat(localUser.lastActivity.split('T')[0])}
                    </div>
                </td>

                <td className={styles.actionsCell}>
                    <Button
                        variant={localUser.enabled ? 'error' : 'success'}
                        size="small"
                        loading={loading}
                        onClick={() => setShowStatusModal(true)}
                        className={styles.actionButton}
                    >
                        {localUser.enabled ? 'Block' : 'Activate'}
                    </Button>
                </td>
            </tr>

            <ConfirmModal
                isOpen={showStatusModal}
                onConfirm={handleStatusChange}
                onCancel={() => setShowStatusModal(false)}
                title={localUser.enabled ? 'Block User' : 'Activate User'}
                message={`Are you sure you want to ${localUser.enabled ? 'block' : 'activate'} ${localUser.firstName} ${localUser.lastName}?`}
                confirmText={localUser.enabled ? 'Block User' : 'Activate User'}
                variant={localUser.enabled ? 'error' : 'success'}
                isLoading={loading}
            />

            <ConfirmModal
                isOpen={showVerificationModal}
                onConfirm={handleVerificationChange}
                onCancel={() => setShowVerificationModal(false)}
                title={localUser.verificationStatus === 'VERIFIED' ? 'Revoke Verification' : 'Verify User'}
                message={`Are you sure you want to ${localUser.verificationStatus === 'VERIFIED' ? 'revoke verification from' : 'verify'} ${localUser.firstName} ${localUser.lastName}?`}
                confirmText={localUser.verificationStatus === 'VERIFIED' ? 'Revoke' : 'Verify'}
                variant={localUser.verificationStatus === 'VERIFIED' ? 'error' : 'success'}
                isLoading={loading}
            />
        </>
    );
};