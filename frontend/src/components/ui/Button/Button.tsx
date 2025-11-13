import React from 'react';
import styles from './Button.module.css';
import clsx from 'clsx';

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'error';
export type ButtonSize = 'small' | 'medium' | 'large';

export interface ButtonProps {
    children: React.ReactNode;
    variant?: ButtonVariant;
    size?: ButtonSize;
    type?: 'button' | 'submit' | 'reset';
    loading?: boolean;
    disabled?: boolean;
    onClick?: () => void;
    className?: string;
    style?: React.CSSProperties;
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
    style
}) => {
    const buttonClass = clsx(
        styles.button,
        styles[variant],
        styles[size],
        loading && styles.loading,
        className
    );

    return (
        <button
            type={type}
            className={buttonClass}
            disabled={disabled || loading}
            onClick={onClick}
            style={style}
        >
            {loading && <span className={styles.spinner}>⏳</span>}
            {children}
        </button>
    );
};