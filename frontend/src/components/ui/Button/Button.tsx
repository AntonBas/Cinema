import React from "react";
import styles from "./Button.module.css";
import clsx from "clsx";

export type ButtonVariant =
  | "primary"
  | "secondary"
  | "error"
  | "success"
  | "cancel"
  | "outline";
export type ButtonSize = "small" | "medium" | "large";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  disabled?: boolean;
  icon?: React.ReactNode;
  sortIcon?: "asc" | "desc" | "none";
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = "primary",
  size = "medium",
  type = "button",
  loading = false,
  disabled = false,
  icon,
  sortIcon = "none",
  className = "",
  ...props
}) => {
  const buttonClass = clsx(
    styles.button,
    styles[variant],
    styles[size],
    loading && styles.loading,
    disabled && styles.disabled,
    sortIcon !== "none" && styles.hasSortIcon,
    className,
  );

  const getSortIcon = () => {
    switch (sortIcon) {
      case "asc":
        return "↑";
      case "desc":
        return "↓";
      default:
        return null;
    }
  };

  const sortIconElement = getSortIcon();

  return (
    <button
      type={type}
      className={buttonClass}
      disabled={disabled || loading}
      {...props}
      aria-busy={loading}
    >
      {loading && <span className={styles.spinner} aria-hidden="true" />}
      {icon && <span className={styles.icon}>{icon}</span>}
      {children}
      {sortIconElement && (
        <span className={styles.sortIcon}>{sortIconElement}</span>
      )}
    </button>
  );
};
