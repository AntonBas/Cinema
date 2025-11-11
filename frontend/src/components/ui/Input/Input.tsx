import React from 'react';
import styles from './Input.module.css';

export interface InputProps {
    type?: 'text' | 'email' | 'password' | 'number' | 'date';
    placeholder?: string;
    value?: string;
    size?: 'small' | 'medium' | 'large';
    disabled?: boolean;
    error?: string;
    onChange?: (value: string) => void;
    onBlur?: () => void;
    onFocus?: () => void;
}

export const Input: React.FC<InputProps> = ({
    type = 'text',
    placeholder,
    value,
    size = 'medium',
    disabled = false,
    error,
    onChange,
    onBlur,
    onFocus
}) => {
    const inputClasses = [
        styles.input,
        size !== 'medium' && styles[size],
        error && styles.error,
        disabled && styles.disabled
    ].filter(Boolean).join(' ');

    return (
        <div className={styles.container}>
            <input
                type={type}
                className={inputClasses}
                placeholder={placeholder}
                value={value}
                disabled={disabled}
                onChange={(e) => onChange?.(e.target.value)}
                onBlur={onBlur}
                onFocus={onFocus}
            />
            {error && <span className={styles.errorText}>{error}</span>}
        </div>
    );
};