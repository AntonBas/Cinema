import React, { useId, useCallback } from 'react';
import styles from './Input.module.css';
import clsx from 'clsx';

export type InputType = 'text' | 'email' | 'password' | 'number' | 'date' | 'datetime-local';
export type InputSize = 'small' | 'medium' | 'large';

export interface InputProps {
    type?: InputType;
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
    onBlur?: () => void;
    onFocus?: () => void;
    onClick?: () => void;
    error?: string;
    required?: boolean;
    maxLength?: number;
    min?: string | number;
    max?: string | number;
    step?: string | number;
    className?: string;
    label?: string;
    size?: InputSize;
    id?: string;
    autoFocus?: boolean;
    'aria-label'?: string;
    'aria-describedby'?: string;
}

export const Input: React.FC<InputProps> = ({
    type = 'text',
    value,
    onChange,
    placeholder,
    disabled = false,
    onBlur,
    onFocus,
    onClick,
    error,
    required = false,
    maxLength,
    min,
    max,
    step,
    className = '',
    label,
    size = 'medium',
    id: externalId,
    autoFocus = false,
    'aria-label': ariaLabel,
    'aria-describedby': ariaDescribedby,
}) => {
    const generatedId = useId();
    const inputId = externalId || generatedId;
    const errorId = error ? `${inputId}-error` : undefined;

    const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        onChange(e.target.value);
    }, [onChange]);

    const handleBlur = useCallback(() => {
        onBlur?.();
    }, [onBlur]);

    const handleFocus = useCallback(() => {
        onFocus?.();
    }, [onFocus]);

    const inputClass = clsx(
        styles.input,
        error && styles.error,
        disabled && styles.disabled,
        size === 'small' && styles.small,
        size === 'large' && styles.large,
        className
    );

    return (
        <div className={styles.container}>
            {label && (
                <label
                    htmlFor={inputId}
                    className={clsx(styles.label, required && styles.required)}
                >
                    {label}
                    {required && <span className={styles.requiredStar} aria-hidden="true">*</span>}
                </label>
            )}

            <input
                id={inputId}
                type={type}
                value={value}
                onChange={handleChange}
                onBlur={handleBlur}
                onFocus={handleFocus}
                onClick={onClick}
                placeholder={placeholder}
                disabled={disabled}
                required={required}
                maxLength={maxLength}
                min={min}
                max={max}
                step={step}
                autoFocus={autoFocus}
                className={inputClass}
                aria-label={ariaLabel}
                aria-describedby={errorId || ariaDescribedby}
                aria-invalid={!!error}
                aria-required={required}
            />

            {error && (
                <div
                    id={errorId}
                    className={styles.errorMessage}
                    role="alert"
                >
                    {error}
                </div>
            )}
        </div>
    );
};