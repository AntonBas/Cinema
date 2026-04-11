import React, { useState, useMemo } from 'react';
import { Badge, Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { UserRoleDisplay, VerificationStatusDisplay } from '@/types/user';
import type { AdminUserListResponse, UserRole, VerificationStatus } from '@/types/user';
import { safeFormatDate } from '@/utils/dateUtils';
import styles from './UserTableRow.module.css';

interface UserTableRowProps {
    user: AdminUserListResponse;
    onUpdate: () => void;
}

const getVerificationColor = (status: VerificationStatus): 'success' | 'secondary' => {
    return status === 'VERIFIED' ? 'success' : 'secondary';
};

const formatDateTime = (dateString: string | null | undefined): string => {
    if (!dateString) return 'Not verified';
    try {
        const date = new Date(dateString);
        const formattedDate = safeFormatDate(date.toISOString().split('T')[0]);
        const time = date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        return `${formattedDate} ${time}`;
    } catch {
        return 'Not verified';
    }
};

export const UserTableRow: React.FC<UserTableRowProps> = ({ user, onUpdate }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);

    const { updateUserRole, updateUserStatus, updateBirthDateVerification, loading } = useAdminUsers();

    const handleRoleChange = async (value: string | number) => {
        const newRole = value as UserRole;
        if (newRole === user.userRole) return;
        await updateUserRole(user.id, newRole);
        onUpdate();
    };

    const handleStatusChange = async () => {
        await updateUserStatus(user.id, !user.enabled);
        setShowStatusModal(false);
        onUpdate();
    };

    const handleVerificationChange = async () => {
        const newStatus: VerificationStatus = user.verificationStatus === 'VERIFIED' ? 'NOT_VERIFIED' : 'VERIFIED';
        await updateBirthDateVerification(user.id, newStatus);
        setShowVerificationModal(false);
        onUpdate();
    };

    const roleOptions = useMemo(() =>
        Object.entries(UserRoleDisplay).map(([value, label]) => ({ value, label })),
        []);

    const isVerified = user.verificationStatus === 'VERIFIED';
    const isEnabled = user.enabled;

    return (
        <>
            <tr className={styles.row}>
                <td className={styles.userCell}>
                    <div className={styles.userInfo}>
                        <div className={styles.userName}>{user.firstName} {user.lastName}</div>
                        <div className={styles.userEmail}>{user.email}</div>
                    </div>
                </td>

                <td className={styles.roleCell}>
                    <Select
                        value={user.userRole}
                        onChange={handleRoleChange}
                        options={roleOptions}
                        disabled={loading}
                        className={styles.roleSelect}
                    />
                </td>

                <td className={styles.verificationCell}>
                    <div className={styles.verificationInfo}>
                        <Badge variant={getVerificationColor(user.verificationStatus)} size="small">
                            {VerificationStatusDisplay[user.verificationStatus]}
                        </Badge>
                        <div className={styles.verificationDate}>{formatDateTime(user.verifiedAt)}</div>
                        <Button
                            variant="secondary"
                            size="small"
                            loading={loading}
                            onClick={() => setShowVerificationModal(true)}
                            disabled={loading}
                        >
                            {isVerified ? 'Revoke' : 'Verify'}
                        </Button>
                    </div>
                </td>

                <td className={styles.statusCell}>
                    <Badge variant={isEnabled ? 'success' : 'error'} size="small">
                        {isEnabled ? 'Active' : 'Blocked'}
                    </Badge>
                </td>

                <td className={styles.ticketsCell}>
                    <span className={styles.ticketsCount}>{user.ticketsCount ?? 0}</span>
                </td>

                <td className={styles.activityCell}>
                    {safeFormatDate(user.lastActivity?.split('T')[0])}
                </td>

                <td className={styles.actionsCell}>
                    <Button
                        variant={isEnabled ? 'error' : 'success'}
                        size="small"
                        loading={loading}
                        onClick={() => setShowStatusModal(true)}
                        disabled={loading}
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
                message={`Are you sure you want to ${isEnabled ? 'block' : 'activate'} ${user.firstName} ${user.lastName}?`}
                confirmText={isEnabled ? 'Block' : 'Activate'}
                variant={isEnabled ? 'error' : 'success'}
                isLoading={loading}
            />

            <ConfirmModal
                isOpen={showVerificationModal}
                onConfirm={handleVerificationChange}
                onCancel={() => setShowVerificationModal(false)}
                title={isVerified ? 'Revoke Verification' : 'Verify User'}
                message={`Are you sure you want to ${isVerified ? 'revoke verification from' : 'verify'} ${user.firstName} ${user.lastName}?`}
                confirmText={isVerified ? 'Revoke' : 'Verify'}
                variant={isVerified ? 'error' : 'success'}
                isLoading={loading}
            />
        </>
    );
};