import React, { useId, useCallback } from 'react';
import styles from './Textarea.module.css';
import clsx from 'clsx';

export interface TextareaProps {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
    onBlur?: () => void;
    onFocus?: () => void;
    error?: string;
    required?: boolean;
    maxLength?: number;
    rows?: number;
    className?: string;
    label?: string;
    id?: string;
    autoFocus?: boolean;
    'aria-label'?: string;
    'aria-describedby'?: string;
}

export const Textarea: React.FC<TextareaProps> = ({
    value,
    onChange,
    placeholder,
    disabled = false,
    onBlur,
    onFocus,
    error,
    required = false,
    maxLength,
    rows = 4,
    className = '',
    label,
    id: externalId,
    autoFocus = false,
    'aria-label': ariaLabel,
    'aria-describedby': ariaDescribedby,
}) => {
    const generatedId = useId();
    const textareaId = externalId || generatedId;
    const errorId = error ? `${textareaId}-error` : undefined;

    const handleChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
        onChange(e.target.value);
    }, [onChange]);

    const handleBlur = useCallback(() => {
        onBlur?.();
    }, [onBlur]);

    const handleFocus = useCallback(() => {
        onFocus?.();
    }, [onFocus]);

    const textareaClass = clsx(
        styles.textarea,
        error && styles.error,
        disabled && styles.disabled,
        className
    );

    return (
        <div className={styles.container}>
            {label && (
                <label
                    htmlFor={textareaId}
                    className={clsx(styles.label, required && styles.required)}
                >
                    {label}
                    {required && <span className={styles.requiredStar} aria-hidden="true">*</span>}
                </label>
            )}

            <textarea
                id={textareaId}
                value={value}
                onChange={handleChange}
                onBlur={handleBlur}
                onFocus={handleFocus}
                placeholder={placeholder}
                disabled={disabled}
                required={required}
                maxLength={maxLength}
                rows={rows}
                autoFocus={autoFocus}
                className={textareaClass}
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
