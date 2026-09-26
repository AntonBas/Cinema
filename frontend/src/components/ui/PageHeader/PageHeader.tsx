import React from "react";
import clsx from "clsx";
import styles from "./PageHeader.module.css";

export interface PageHeaderProps {
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  actions?: React.ReactNode;
  align?: "start" | "center";
  divider?: boolean;
  className?: string;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  subtitle,
  actions,
  align = "start",
  divider = false,
  className = "",
}) => (
  <header
    className={clsx(
      styles.header,
      align === "center" && styles.center,
      divider && styles.divider,
      className,
    )}
  >
    <div className={styles.text}>
      <h1 className={styles.title}>{title}</h1>
      {subtitle && <div className={styles.subtitle}>{subtitle}</div>}
    </div>
    {actions && <div className={styles.actions}>{actions}</div>}
  </header>
);
