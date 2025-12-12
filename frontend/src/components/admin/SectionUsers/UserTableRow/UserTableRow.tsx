import React, { useState, useEffect } from 'react';
import { Badge, Button, Select, ConfirmModal } from '@/components/ui';
import { useAdminUserMutations } from '@/hooks/features';
import { UserRoleDisplay, UserStatusDisplay, VerificationStatusDisplay, VerificationStatusColors } from '@/types/user';
import type { AdminUser, UserRole, VerificationStatus } from '@/types/user';
import { toDisplayFormat } from '@/utils/dateUtils';
import styles from './UserTableRow.module.css';

interface UserTableRowProps {
    user: AdminUser;
    onUpdate: () => void;
    onError: (error: string) => void;
    onSuccess: (message: string) => void;
}

export const UserTableRow: React.FC<UserTableRowProps> = ({ user, onUpdate, onError, onSuccess }) => {
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [showVerificationModal, setShowVerificationModal] = useState(false);
    const [selectedRole, setSelectedRole] = useState<UserRole>(user.userRole);
    const [currentUser, setCurrentUser] = useState<AdminUser>(user);

    useEffect(() => {
        setCurrentUser(user);
        setSelectedRole(user.userRole);
    }, [user]);

    const { updateUserRole, updateUserStatus, updateBirthDateVerification, isLoading } = useAdminUserMutations();

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

    const handleVerificationChange = async () => {
        try {
            const newVerificationStatus: VerificationStatus = currentUser.verificationStatus === 'VERIFIED' ? 'NOT_VERIFIED' : 'VERIFIED';
            await updateBirthDateVerification(currentUser.id, newVerificationStatus);
            const now = new Date().toISOString();
            setCurrentUser(prev => ({
                ...prev,
                verificationStatus: newVerificationStatus,
                verifiedAt: newVerificationStatus === 'VERIFIED' ? now : null
            }));
            setShowVerificationModal(false);
            onUpdate();
            onSuccess(`User verification ${newVerificationStatus === 'VERIFIED' ? 'granted' : 'revoked'} successfully`);
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

                <td className={styles.verificationCell}>
                    <div className={styles.verificationInfo}>
                        <Badge
                            variant={VerificationStatusColors[currentUser.verificationStatus] as any}
                            size="small"
                            className={styles.verificationBadge}
                        >
                            {VerificationStatusDisplay[currentUser.verificationStatus]}
                        </Badge>
                        <div className={styles.verificationDate}>
                            {formatDateTime(currentUser.verifiedAt)}
                        </div>
                        <Button
                            variant="secondary"
                            size="small"
                            loading={isLoading}
                            onClick={() => setShowVerificationModal(true)}
                            className={styles.verificationButton}
                        >
                            {currentUser.verificationStatus === 'VERIFIED' ? 'Revoke' : 'Verify'}
                        </Button>
                    </div>
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
                    {toDisplayFormat(currentUser.createdAt.split('T')[0])}
                </td>

                <td className={styles.ticketsCell}>
                    <span className={styles.ticketsCount}>{currentUser.ticketsCount}</span>
                </td>

                <td className={styles.activityCell}>
                    <div className={styles.activityInfo}>
                        {toDisplayFormat(currentUser.lastActivity.split('T')[0])}
                    </div>
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

            <ConfirmModal
                isOpen={showVerificationModal}
                onConfirm={handleVerificationChange}
                onCancel={() => setShowVerificationModal(false)}
                title={currentUser.verificationStatus === 'VERIFIED' ? 'Revoke Verification' : 'Verify User'}
                message={`Are you sure you want to ${currentUser.verificationStatus === 'VERIFIED' ? 'revoke verification from' : 'verify'} ${currentUser.firstName} ${currentUser.lastName}?`}
                confirmText={currentUser.verificationStatus === 'VERIFIED' ? 'Revoke' : 'Verify'}
                variant={currentUser.verificationStatus === 'VERIFIED' ? 'error' : 'success'}
                isLoading={isLoading}
            />
        </>
    );
};