import React, { useState, useEffect, useCallback } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { UserProfileCard } from '@/components/account/UserProfileCard/UserProfileCard';
import { ProfileEditForm } from '@/components/account/ProfileEditForm/ProfileEditForm';
import { useUser } from '@/hooks/features/user/useUser';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './ProfilePage.module.css';

export const ProfilePage: React.FC = () => {
    const { user, loading, error, getProfile } = useUser();
    const [isEditing, setIsEditing] = useState(false);
    const [localError, setLocalError] = useState<string | null>(null);
    const [initialLoadDone, setInitialLoadDone] = useState(false);

    const showLoading = useDelayedLoading(loading);

    useEffect(() => {
        if (!initialLoadDone) {
            setInitialLoadDone(true);
            getProfile().catch((err) => {
                setLocalError(err instanceof Error ? err.message : 'Failed to load profile');
            });
        }
    }, [getProfile, initialLoadDone]);

    const handleEditStart = useCallback(() => {
        setIsEditing(true);
        setLocalError(null);
    }, []);

    const handleEditCancel = useCallback(() => {
        setIsEditing(false);
        setLocalError(null);
    }, []);

    const handleProfileUpdated = useCallback(() => {
        getProfile()
            .then(() => {
                setIsEditing(false);
                setLocalError(null);
            })
            .catch((err) => {
                setLocalError(err instanceof Error ? err.message : 'Failed to update profile');
            });
    }, [getProfile]);

    if (showLoading) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading your account..." />
                    </div>
                </div>
            </Layout>
        );
    }

    if (error && !user) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.container}>
                        <AccountSidebar activePage="profile" />
                        <div className={styles.content}>
                            <div className={styles.notification} data-type="error">
                                Failed to load user data. Please try again later.
                            </div>
                        </div>
                    </div>
                </div>
            </Layout>
        );
    }

    if (!user) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading your account..." />
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