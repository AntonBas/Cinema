import React, { useState } from "react";
import { AccountPageLayout } from "@/components/account/AccountPageLayout/AccountPageLayout";
import { PasswordChangeForm } from "@/components/account/SecuritySection/PasswordChangeForm/PasswordChangeForm";
import { EmailChangeForm } from "@/components/account/SecuritySection/EmailChangeForm/EmailChangeForm";
import { Tabs, type TabItem } from "@/components/ui/Tabs/Tabs";
import styles from "./SecurityPage.module.css";

type SecuritySection = "password" | "email";

const SECTIONS: ReadonlyArray<TabItem<SecuritySection>> = [
  { id: "password", label: "Change Password" },
  { id: "email", label: "Change Email" },
];

export const SecurityPage: React.FC = () => {
  const [activeSection, setActiveSection] =
    useState<SecuritySection>("password");

  return (
    <AccountPageLayout
      title="Account Security"
      subtitle="Manage your password and email settings"
    >
      <Tabs
        items={SECTIONS}
        activeId={activeSection}
        onChange={setActiveSection}
        ariaLabel="Security sections"
      />

      <div className={styles.mainContent}>
        {activeSection === "password" ? (
          <PasswordChangeForm />
        ) : (
          <EmailChangeForm />
        )}
      </div>
    </AccountPageLayout>
  );
};
