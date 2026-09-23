import React from "react";
import { Layout } from "@/components/layout/Layout/Layout";
import styles from "./AuthPageLayout.module.css";

export interface AuthPageLayoutProps {
  children: React.ReactNode;
}

export const AuthPageLayout: React.FC<AuthPageLayoutProps> = ({ children }) => (
  <Layout>
    <div className={styles.container}>{children}</div>
  </Layout>
);
