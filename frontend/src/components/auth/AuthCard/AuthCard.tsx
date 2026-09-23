import React from 'react';
import clsx from 'clsx';
import styles from './AuthCard.module.css';

export interface AuthCardProps {
    title: React.ReactNode;
    children: React.ReactNode;
    wide?: boolean;
    className?: string;
}

export const AuthCard: React.FC<AuthCardProps> = ({ title, children, wide = false, className = '' }) => (
    <section className={clsx(styles.card, wide && styles.wide, className)}>
        <h1 className={styles.title}>{title}</h1>
        {children}
    </section>
);
