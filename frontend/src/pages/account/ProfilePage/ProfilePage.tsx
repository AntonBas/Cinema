import React, { useState, useEffect } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { UserProfileCard } from "@/components/account/OverviewSection/UserProfileCard/UserProfileCard";
import { ProfileEditForm } from "@/components/account/OverviewSection/ProfileEditForm/ProfileEditForm";
import { useUser } from "@/hooks/features/user/useUser";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { UserUpdateRequest } from "@/types/user";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./ProfilePage.module.css";

const PROFILE_SUBTITLE = "View and manage your personal information";

export const ProfilePage: React.FC = () => {
  const { profile, loading, profileError, getProfile, updateProfile } =
    useUser();
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    getProfile();
  }, [getProfile]);

  const handleProfileUpdated = async (formData: UserUpdateRequest) => {
    await updateProfile(formData);
    setIsEditing(false);
  };

  if (loading && !profile) {
    return (
      <AccountPageLayout title="My Profile" subtitle={PROFILE_SUBTITLE}>
        <div className={styles.loading}>
          <LoadingSpinner text="Loading your account..." />
        </div>
      </AccountPageLayout>
    );
  }

  if (profileError || !profile) {
    return (
      <AccountPageLayout title="My Profile" subtitle={PROFILE_SUBTITLE}>
        <EmptyState
          variant="error"
          title="Failed to Load Profile"
          message="Please try again."
        />
      </AccountPageLayout>
    );
  }

  return (
    <AccountPageLayout
      title={isEditing ? "Edit Profile" : "My Profile"}
      subtitle={
        isEditing ? "Update your personal information" : PROFILE_SUBTITLE
      }
    >
      {profile.verificationStatus === "NOT_VERIFIED" && (
        <div className={styles.verificationBanner}>
          <span>📅</span>
          <p>
            Verify your date of birth at the cinema cash desk to access birthday
            bonuses.
          </p>
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
    </AccountPageLayout>
  );
};
