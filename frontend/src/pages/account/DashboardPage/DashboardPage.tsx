import React, { useState } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { UserProfileCard } from '@/components/account/UserProfileCard/UserProfileCard';
import { ProfileEditForm } from '@/components/account/ProfileEditForm/ProfileEditForm';
import { useUser } from '@/hooks/features/user';
import styles from './DashboardPage.module.css';

export const DashboardPage: React.FC = () => {
    const { user, isLoading, error, refreshUser } = useUser();
    const [isEditing, setIsEditing] = useState(false);

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
                <div className={styles.dashboardPage}>
                    <div className={styles.loading}>Loading your account...</div>
                </div>
            </Layout>
        );
    }

    if (error || !user) {
        return (
            <Layout>
                <div className={styles.dashboardPage}>
                    <div className={styles.notification} data-type="error">
                        {error || 'Failed to load user data'}
                    </div>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.dashboardPage}>
                <div className={styles.container}>
                    <AccountSidebar activePage="dashboard" />

                    <div className={styles.content}>
                        <div className={styles.header}>
                            <h1 className={styles.title}>
                                {isEditing ? 'Edit Profile' : 'Welcome back, ' + user.firstName + '! 👋'}
                            </h1>
                            <p className={styles.subtitle}>
                                {isEditing ? 'Update your personal information' : 'Here\'s your account overview'}
                            </p>
                        </div>

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

                        {!isEditing && (
                            <div className={styles.quickStats}>
                                <div className={styles.statCard}>
                                    <h3>Recent Tickets</h3>
                                    <p>No tickets yet</p>
                                </div>
                                <div className={styles.statCard}>
                                    <h3>Loyalty Points</h3>
                                    <p>0 points</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </Layout>
    );
};