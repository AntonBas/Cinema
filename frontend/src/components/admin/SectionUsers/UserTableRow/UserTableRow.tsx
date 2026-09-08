import React, { useState, useMemo } from 'react';
import { ShieldCheck, ShieldX, UserCheck, UserX } from 'lucide-react';
import { Badge, Select, ConfirmModal } from '@/components/ui';
import { useAdminUsers } from '@/hooks/features/admin/useAdminUsers';
import { UserRoleDisplay, VerificationStatusDisplay } from '@/types/user';
import type { AdminUserListResponse, UserRole, VerificationStatus } from '@/types/user';
import { safeFormatDate } from '@/utils/dateUtils';
import { ActionIconButton } from '@/components/admin/shared/ActionIconButton/ActionIconButton';
import tableStyles from '@/components/admin/shared/AdminTable/AdminTable.module.css';
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
            <tr>
                <td data-label="User">
                    <div className={styles.userInfo}>
                        <div className={styles.userName}>{user.firstName} {user.lastName}</div>
                        <div className={styles.userEmail}>{user.email}</div>
                    </div>
                </td>

                <td data-label="Role">
                    <Select
                        value={user.userRole}
                        onChange={handleRoleChange}
                        options={roleOptions}
                        disabled={loading}
                        className={styles.roleSelect}
                    />
                </td>

                <td data-label="Verification">
                    <div className={styles.verificationInfo}>
                        <Badge variant={getVerificationColor(user.verificationStatus)} size="small">
                            {VerificationStatusDisplay[user.verificationStatus]}
                        </Badge>
                        <div className={styles.verificationDate}>{formatDateTime(user.verifiedAt)}</div>
                    </div>
                </td>

                <td data-label="Status">
                    <Badge variant={isEnabled ? 'success' : 'error'} size="small">
                        {isEnabled ? 'Active' : 'Blocked'}
                    </Badge>
                </td>

                <td data-label="Tickets">
                    <span className={styles.ticketsCount}>{user.ticketsCount ?? 0}</span>
                </td>

                <td data-label="Last Activity">
                    {safeFormatDate(user.lastActivity?.split('T')[0])}
                </td>

                <td data-label="Actions">
                    <div className={tableStyles.actions}>
                        <ActionIconButton
                            icon={isVerified ? <ShieldX /> : <ShieldCheck />}
                            label={isVerified ? 'Revoke verification' : 'Verify user'}
                            variant={isVerified ? 'error' : 'success'}
                            loading={loading}
                            onClick={() => setShowVerificationModal(true)}
                        />
                        <ActionIconButton
                            icon={isEnabled ? <UserX /> : <UserCheck />}
                            label={isEnabled ? 'Block user' : 'Activate user'}
                            variant={isEnabled ? 'error' : 'success'}
                            loading={loading}
                            onClick={() => setShowStatusModal(true)}
                        />
                    </div>
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
