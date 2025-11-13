import React from 'react';
import styles from './Input.module.css';
import clsx from 'clsx';

export interface InputProps {
    type?: 'text' | 'email' | 'password' | 'number' | 'date';
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
    error?: string;
    required?: boolean;
    maxLength?: number;
    className?: string;
}

export const Input: React.FC<InputProps> = ({
    type = 'text',
    value,
    onChange,
    placeholder,
    disabled = false,
    error,
    required = false,
    maxLength,
    className = ''
}) => {
    const inputClass = clsx(
        styles.input,
        error && styles.error,
        disabled && styles.disabled,
        className
    );

    return (
        <div className={styles.container}>
            <input
                type={type}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                disabled={disabled}
                required={required}
                maxLength={maxLength}
                className={inputClass}
            />
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
};