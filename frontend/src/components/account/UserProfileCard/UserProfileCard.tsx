import React from 'react';
import type { UserProfile } from '@/types/user';
import { Button } from '@/components/ui';
import styles from './UserProfileCard.module.css';

interface UserProfileCardProps {
    user: UserProfile;
    onEdit: () => void;
}

export const UserProfileCard: React.FC<UserProfileCardProps> = ({ user, onEdit }) => {
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className={styles.profileCard}>
            <div className={styles.cardHeader}>
                <h2 className={styles.cardTitle}>Profile Information</h2>
                <Button
                    variant="primary"
                    onClick={onEdit}
                    style={{ minWidth: '120px' }}
                >
                    Edit Profile
                </Button>
            </div>

            <div className={styles.cardContent}>
                <div className={styles.profileSection}>
                    <div className={styles.userInfo}>
                        <h3 className={styles.userName}>
                            {user.firstName} {user.lastName}
                        </h3>
                        <p className={styles.userEmail}>{user.email}</p>
                    </div>
                </div>

                <div className={styles.detailsGrid}>
                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>First Name</span>
                        <span className={styles.detailValue}>{user.firstName}</span>
                    </div>

                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Last Name</span>
                        <span className={styles.detailValue}>{user.lastName}</span>
                    </div>

                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Email Address</span>
                        <span className={styles.detailValue}>{user.email}</span>
                    </div>

                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Phone Number</span>
                        <span className={styles.detailValue}>
                            {user.phoneNumber || 'Not provided'}
                        </span>
                    </div>

                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>City</span>
                        <span className={styles.detailValue}>
                            {user.city || 'Not provided'}
                        </span>
                    </div>

                    <div className={styles.detailItem}>
                        <span className={styles.detailLabel}>Date of Birth</span>
                        <span className={styles.detailValue}>
                            {user.dateOfBirth ? formatDate(user.dateOfBirth) : 'Not provided'}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};