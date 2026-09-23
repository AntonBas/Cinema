import React from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import { AccountSidebar } from "@/components/account/AccountSidebar/AccountSidebar";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./AccountPageLayout.module.css";

export interface AccountPageLayoutProps {
  title?: React.ReactNode;
  subtitle?: React.ReactNode;
  actions?: React.ReactNode;
  children: React.ReactNode;
}

export const AccountPageLayout: React.FC<AccountPageLayoutProps> = ({
  title,
  subtitle,
  actions,
  children,
}) => (
  <Layout>
    <div className={styles.page}>
      <div className={styles.container}>
        <AccountSidebar />
        <div className={styles.content}>
          {title && (
            <PageHeader title={title} subtitle={subtitle} actions={actions} />
          )}
          {children}
        </div>
      </div>
    </div>
  </Layout>
);
