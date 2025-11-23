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