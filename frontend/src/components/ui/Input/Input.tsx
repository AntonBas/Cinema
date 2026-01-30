import React from 'react';
import styles from './Input.module.css';
import clsx from 'clsx';

export interface InputProps {
    type?: 'text' | 'email' | 'password' | 'number' | 'date' | 'datetime-local';
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
    onBlur?: () => void;
    onClick?: () => void;
    error?: string;
    required?: boolean;
    maxLength?: number;
    min?: string | number;
    max?: string | number;
    step?: string | number;
    className?: string;
    label?: string;
    size?: 'small' | 'medium' | 'large';
    id?: string;
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
    min,
    max,
    step,
    onClick,
    className = '',
    label,
    size = 'medium',
    id
}) => {
    const inputClass = clsx(
        styles.input,
        error && styles.error,
        disabled && styles.disabled,
        size === 'small' && styles.small,
        size === 'large' && styles.large,
        className
    );

    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

    return (
        <div className={styles.container}>
            {label && (
                <label htmlFor={inputId} className={styles.label}>
                    {label}
                    {required && <span className={styles.required}>*</span>}
                </label>
            )}
            <input
                id={inputId}
                type={type}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                onClick={onClick}
                placeholder={placeholder}
                disabled={disabled}
                required={required}
                maxLength={maxLength}
                min={min}
                max={max}
                step={step}
                className={inputClass}
            />
            {error && <div className={styles.errorMessage}>{error}</div>}
        </div>
    );
};