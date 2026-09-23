import React, { useState } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { PasswordChangeForm } from "@/components/account/SecuritySection/PasswordChangeForm/PasswordChangeForm";
import { EmailChangeForm } from "@/components/account/SecuritySection/EmailChangeForm/EmailChangeForm";
import styles from "./SecurityPage.module.css";

type SecuritySection = "password" | "email";

const SECTIONS = [
  { id: "password" as SecuritySection, label: "Change Password" },
  { id: "email" as SecuritySection, label: "Change Email" },
];

export const SecurityPage: React.FC = () => {
  const [activeSection, setActiveSection] =
    useState<SecuritySection>("password");

  return (
    <AccountPageLayout title="Account Security" subtitle="Manage your password and email settings">
      <div className={styles.mobileTabs}>
        {SECTIONS.map((section) => (
          <button
            key={section.id}
            className={`${styles.mobileTab} ${activeSection === section.id ? styles.mobileTabActive : ""}`}
            onClick={() => setActiveSection(section.id)}
          >
            <span className={styles.mobileTabLabel}>{section.label}</span>
          </button>
        ))}
      </div>

      <div className={styles.securityLayout}>
        <div className={styles.sidebar}>
          <nav className={styles.securityNav}>
            {SECTIONS.map((section) => (
              <button
                key={section.id}
                className={`${styles.navButton} ${activeSection === section.id ? styles.active : ""}`}
                onClick={() => setActiveSection(section.id)}
              >
                <span className={styles.navLabel}>{section.label}</span>
              </button>
            ))}
          </nav>
        </div>

        <div className={styles.mainContent}>
          {activeSection === "password" ? (
            <PasswordChangeForm />
          ) : (
            <EmailChangeForm />
          )}
        </div>
      </div>
    </AccountPageLayout>
  );
};
