import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { UserProfileCard } from '@/components/account/UserProfileCard/UserProfileCard';
import { ProfileEditForm } from '@/components/account/ProfileEditForm/ProfileEditForm';
import { useUser } from '@/hooks/features/user';
import styles from './ProfilePage.module.css';

export const ProfilePage: React.FC = () => {
    const { user, isLoading, refreshUser } = useUser();
    const [isEditing, setIsEditing] = useState(false);
    const [localError] = useState<string | null>(null);

    const handleEditStart = () => {
        setIsEditing(true);
    };

    const handleEditCancel = () => {
        setIsEditing(false);
    };

    const handleProfileUpdated = () => {
        refreshUser();
        setIsEditing(false);
    };

    if (isLoading) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.loading}>Loading your account...</div>
                </div>
            </Layout>
        );
    }

    if (!user) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.notification} data-type="error">
                        Failed to load user data
                    </div>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.profilePage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="profile" />

                    <div className={styles.content}>
                        {localError && (
                            <div className={styles.notification} data-type="error">
                                {localError}
                            </div>
                        )}

                        <div className={styles.header}>
                            <h1 className={styles.title}>
                                {isEditing ? 'Edit Profile' : 'My Profile'}
                            </h1>
                            <p className={styles.subtitle}>
                                {isEditing
                                    ? 'Update your personal information'
                                    : 'Manage your account information and preferences'}
                            </p>
                        </div>

                        {!isEditing && user.verificationStatus === 'NOT_VERIFIED' && (
                            <div className={styles.verificationBanner}>
                                <div className={styles.bannerContent}>
                                    <span className={styles.bannerIcon}>📅</span>
                                    <div>
                                        <p className={styles.bannerTitle}>Verify Your Date of Birth</p>
                                        <p className={styles.bannerText}>
                                            To change your date of birth ({new Date(user.dateOfBirth).toLocaleDateString()})
                                            you will need to verify it at the cinema cash desk.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )}

                        <div className={styles.profileSection}>
                            {isEditing ? (
                                <ProfileEditForm
                                    user={user}
                                    onCancel={handleEditCancel}
                                    onSuccess={handleProfileUpdated}
                                />
                            ) : (
                                <UserProfileCard
                                    user={user}
                                    onEdit={handleEditStart}
                                />
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};