import React, { useState, useEffect } from 'react';
import { Layout } from '@/components/layout/Layout/Layout';
import { AccountSidebar } from '@/components/account/AccountSidebar/AccountSidebar';
import { UserProfileCard } from '@/components/account/OverviewSection/UserProfileCard/UserProfileCard';
import { ProfileEditForm } from '@/components/account/OverviewSection/ProfileEditForm/ProfileEditForm';
import { useUser } from '@/hooks/features/user/useUser';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { UserUpdateRequest } from '@/types/user';
import styles from './ProfilePage.module.css';

export const ProfilePage: React.FC = () => {
    const { profile, loading, profileError, getProfile, updateProfile } = useUser();
    const [isEditing, setIsEditing] = useState(false);

    const showLoading = useDelayedLoading(loading);

    useEffect(() => {
        getProfile();
    }, [getProfile]);

    const handleProfileUpdated = async (formData: UserUpdateRequest) => {
        await updateProfile(formData);
        setIsEditing(false);
    };

    if (showLoading && !profile) {
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

    if (profileError || !profile) {
        return (
            <Layout>
                <div className={styles.profilePage}>
                    <div className={styles.container}>
                        <AccountSidebar />
                        <div className={styles.content}>
                            <div className={styles.error}>Failed to load profile. Please try again.</div>
                        </div>
                    </div>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.profilePage}>
                <div className={styles.container}>
                    <AccountSidebar />

                    <div className={styles.content}>
                        <div className={styles.header}>
                            <h1 className={styles.title}>{isEditing ? 'Edit Profile' : 'My Profile'}</h1>
                        </div>

                        {profile.verificationStatus === 'NOT_VERIFIED' && (
                            <div className={styles.verificationBanner}>
                                <span>📅</span>
                                <p>Verify your date of birth at the cinema cash desk to access birthday bonuses.</p>
                            </div>
                        )}

                        <div className={styles.profileSection}>
                            {isEditing ? (
                                <ProfileEditForm
                                    user={profile}
                                    onCancel={() => setIsEditing(false)}
                                    onSuccess={handleProfileUpdated}
                                    loading={loading}
                                />
                            ) : (
                                <UserProfileCard user={profile} onEdit={() => setIsEditing(true)} />
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};