import React from 'react';
import styles from './Badge.module.css';

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
}

export const Badge: React.FC<BadgeProps> = ({
    children,
    variant = 'primary',
    size = 'medium',
    className = '',
    onClick
}) => {
    const badgeClass = `${styles.badge} ${styles[variant]} ${styles[size]} ${onClick ? styles.clickable : ''
        } ${className}`;

    if (onClick) {
        return (
            <button
                className={badgeClass}
                onClick={onClick}
                type="button"
            >
                {children}
            </button>
        );
    }

    return (
        <span className={badgeClass}>
            {children}
        </span>
    );
};