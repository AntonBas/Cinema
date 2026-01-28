import React from 'react';
import styles from './Button.module.css';
import clsx from 'clsx';

export type ButtonVariant = 'primary' | 'secondary' | 'error' | 'success' | 'cancel' | 'outline';
export type ButtonSize = 'small' | 'medium' | 'large';

export interface ButtonProps {
    children: React.ReactNode;
    variant?: ButtonVariant;
    size?: ButtonSize;
    type?: 'button' | 'submit' | 'reset';
    loading?: boolean;
    disabled?: boolean;
    onClick?: (e: React.MouseEvent<HTMLButtonElement>) => void;
    className?: string;
    style?: React.CSSProperties;
    'aria-label'?: string;
    icon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
    children,
    variant = 'primary',
    size = 'medium',
    type = 'button',
    loading = false,
    disabled = false,
    onClick,
    className = '',
    style,
    'aria-label': ariaLabel,
    icon
}) => {
    const buttonClass = clsx(
        styles.button,
        styles[variant],
        styles[size],
        loading && styles.loading,
        disabled && styles.disabled,
        className
    );

    return (
        <button
            type={type}
            className={buttonClass}
            disabled={disabled || loading}
            onClick={onClick}
            style={style}
            aria-label={ariaLabel || (loading ? 'Loading' : undefined)}
            aria-busy={loading}
        >
            {loading && <span className={styles.spinner} aria-hidden="true">⏳</span>}
            {icon && <span className={styles.icon}>{icon}</span>}
            {children}
        </button>
    );
};