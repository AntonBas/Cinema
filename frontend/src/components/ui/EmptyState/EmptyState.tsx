import React from "react";
import clsx from "clsx";
import styles from "./EmptyState.module.css";

export interface EmptyStateProps {
  title: React.ReactNode;
  message?: React.ReactNode;
  action?: React.ReactNode;
  variant?: "empty" | "error";
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  message,
  action,
  variant = "empty",
  className = "",
}) => (
  <div
    className={clsx(styles.state, styles[variant], className)}
    role={variant === "error" ? "alert" : undefined}
  >
    <h3 className={styles.title}>{title}</h3>
    {message && <p className={styles.message}>{message}</p>}
    {action && <div className={styles.action}>{action}</div>}
  </div>
);
