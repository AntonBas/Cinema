import React from 'react';
import styles from './Badge.module.css';
import clsx from 'clsx';

export type BadgeVariant =
    | 'primary'
    | 'secondary'
    | 'success'
    | 'error'
    | 'warning'
    | 'info'
    | 'outline';

export interface BadgeProps {
    children: React.ReactNode;
    variant?: BadgeVariant;
    size?: 'small' | 'medium' | 'large';
    className?: string;
    onClick?: () => void;
    title?: string;
}

export const Badge: React.FC<BadgeProps> = ({
    children,
    variant = 'primary',
    size = 'medium',
    className = '',
    onClick,
    title
}) => {
    const badgeClass = clsx(
        styles.badge,
        styles[variant],
        styles[size],
        onClick && styles.clickable,
        className
    );

    if (onClick) {
        return (
            <button
                className={badgeClass}
                onClick={onClick}
                type="button"
                title={title}
                aria-label={typeof children === 'string' ? children : 'Badge'}
            >
                {children}
            </button>
        );
    }

    return (
        <span className={badgeClass} title={title}>
            {children}
        </span>
    );
};