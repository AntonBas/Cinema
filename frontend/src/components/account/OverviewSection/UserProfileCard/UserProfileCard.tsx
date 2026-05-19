import React from "react";
import { Info } from "lucide-react";
import type { UserProfileResponse } from "@/types/user";
import { Button } from "@/components/ui/Button/Button";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import styles from "./UserProfileCard.module.css";

interface UserProfileCardProps {
  user: UserProfileResponse;
  onEdit: () => void;
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

export const UserProfileCard: React.FC<UserProfileCardProps> = ({
  user,
  onEdit,
}) => {
  return (
    <div className={styles.profileCard}>
      <div className={styles.cardHeader}>
        <h2 className={styles.cardTitle}>Profile Information</h2>
        <Button
          variant="primary"
          onClick={onEdit}
          className={styles.editButton}
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
            <span className={styles.detailLabel}>
              Phone Number
              <Tooltip
                content="We'll call you if there are changes or cancellations to your movie sessions"
                position="top"
              >
                <Info size={14} className={styles.tooltipIcon} />
              </Tooltip>
            </span>
            <span className={styles.detailValue}>
              {user.phoneNumber || "Not provided"}
            </span>
          </div>

          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>City</span>
            <span className={styles.detailValue}>
              {user.city || "Not provided"}
            </span>
          </div>

          <div className={styles.detailItem}>
            <span className={styles.detailLabel}>
              Date of Birth
              <Tooltip
                content="Add your birthday to get special discounts! You'll need to verify your ID at the cinema to claim your birthday offer"
                position="top"
              >
                <Info size={14} className={styles.tooltipIcon} />
              </Tooltip>
            </span>
            <span className={styles.detailValue}>
              {user.dateOfBirth ? formatDate(user.dateOfBirth) : "Not provided"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
