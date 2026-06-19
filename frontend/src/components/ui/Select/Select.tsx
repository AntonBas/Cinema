import React from 'react';
import styles from './Select.module.css';

export interface SelectOption {
    value: string | number;
    label: string;
    disabled?: boolean;
}

export interface SelectProps {
    options: SelectOption[];
    value?: string | number;
    onChange: (value: string | number) => void;
    placeholder?: string;
    disabled?: boolean;
    className?: string;
    required?: boolean;
    label?: string;
    error?: string;
}

export const Select: React.FC<SelectProps> = ({
    options,
    value,
    onChange,
    placeholder = 'Select an option',
    disabled = false,
    className = '',
    required = false,
    label,
    error,
}) => {
    const id = `select-${Math.random().toString(36).substr(2, 9)}`;

    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newValue = e.target.value;
        onChange(newValue);
    };

    return (
        <div className={`${styles.wrapper} ${className}`}>
            {label && (
                <label htmlFor={id} className={styles.label}>
                    {label}
                    {required && <span className={styles.required}>*</span>}
                </label>
            )}

            <div className={styles.selectWrapper}>
                <select
                    id={id}
                    className={`
                        ${styles.select}
                        ${error ? styles.error : ''}
                        ${disabled ? styles.disabled : ''}
                        ${!value ? styles.placeholder : ''}
                    `}
                    value={value ?? ''}
                    onChange={handleChange}
                    disabled={disabled}
                    required={required}
                    aria-invalid={!!error}
                    aria-describedby={error ? `${id}-error` : undefined}
                >
                    <option value="" disabled hidden>
                        {placeholder}
                    </option>

                    {options.map(option => (
                        <option
                            key={option.value}
                            value={option.value}
                            disabled={option.disabled}
                        >
                            {option.label}
                        </option>
                    ))}
                </select>

                <div className={styles.arrow} aria-hidden="true">
                    <svg
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            d="M6 9L12 15L18 9"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />
                    </svg>
                </div>
            </div>

            {error && (
                <div className={styles.errorMessage} id={`${id}-error`}>
                    {error}
                </div>
            )}
        </div>
    );
};