import React from "react";
import { Tooltip } from "@/components/ui";
import styles from "./ActionIconButton.module.css";
import clsx from "clsx";

export type ActionIconVariant =
  | "primary"
  | "success"
  | "error"
  | "warning"
  | "secondary";

export interface ActionIconButtonProps
  extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "children"> {
  icon: React.ReactNode;
  label: string;
  variant?: ActionIconVariant;
  loading?: boolean;
}

export const ActionIconButton: React.FC<ActionIconButtonProps> = ({
  icon,
  label,
  variant = "secondary",
  loading = false,
  disabled = false,
  className,
  type = "button",
  ...props
}) => {
  return (
    <Tooltip content={label} align="end">
      <button
        type={type}
        aria-label={label}
        aria-busy={loading}
        disabled={disabled || loading}
        className={clsx(
          styles.iconButton,
          styles[variant],
          loading && styles.loading,
          className,
        )}
        {...props}
      >
        {loading ? <span className={styles.spinner} aria-hidden="true" /> : icon}
      </button>
    </Tooltip>
  );
};
