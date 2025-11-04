import React, { useState, useEffect } from 'react';
import { PersonRole, type PersonDto, type PersonRequest } from '@/types/person';
import styles from './PersonForm.module.css';

export interface PersonFormProps {
    person?: PersonDto | null;
    onSubmit: (data: PersonRequest) => void;
    onCancel: () => void;
    isLoading?: boolean;
}

export const PersonForm: React.FC<PersonFormProps> = ({
    person,
    onSubmit,
    onCancel,
    isLoading = false
}) => {
    const [formData, setFormData] = useState<PersonRequest>({
        name: '',
        role: PersonRole.ACTOR
    });

    const [errors, setErrors] = useState<{ name?: string }>({});
    const [touched, setTouched] = useState({ name: false });

    useEffect(() => {
        if (person) {
            setFormData({
                name: person.name,
                role: person.role
            });
        }
        setErrors({});
        setTouched({ name: false });
    }, [person]);

    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape' && !isLoading) {
                onCancel();
            }
        };

        document.addEventListener('keydown', handleEscape);
        return () => document.removeEventListener('keydown', handleEscape);
    }, [onCancel, isLoading]);

    const validateForm = (): boolean => {
        const newErrors: { name?: string } = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Name is required';
        } else if (formData.name.trim().length < 2) {
            newErrors.name = 'Name must be at least 2 characters long';
        } else if (formData.name.trim().length > 50) {
            newErrors.name = 'Name must be less than 50 characters';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setTouched({ name: true });

        if (validateForm()) {
            onSubmit({
                name: formData.name.trim(),
                role: formData.role
            });
        }
    };

    const handleInputChange = (field: keyof PersonRequest, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: field === 'role' ? value as PersonRole : value
        }));

        if (errors.name && field === 'name') {
            setErrors(prev => ({ ...prev, name: undefined }));
        }
    };

    const handleBlur = (field: string) => {
        setTouched(prev => ({ ...prev, [field]: true }));
        if (field === 'name') {
            validateForm();
        }
    };

    const shouldShowError = (field: 'name') => {
        return touched[field] && errors[field];
    };

    const isNameInvalid = shouldShowError('name');

    return (
        <div className={styles.modalOverlay} onClick={onCancel}>
            <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
                <div className={styles.header}>
                    <h3>{person ? 'Edit Person' : 'Add New Person'}</h3>
                    <button
                        className={styles.closeButton}
                        onClick={onCancel}
                        type="button"
                        disabled={isLoading}
                        aria-label="Close dialog"
                    >
                        ×
                    </button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form} noValidate>
                    <div className={styles.formGroup}>
                        <label htmlFor="person-name" className={styles.label}>
                            Full Name *
                        </label>
                        <input
                            id="person-name"
                            type="text"
                            value={formData.name}
                            onChange={(e) => handleInputChange('name', e.target.value)}
                            onBlur={() => handleBlur('name')}
                            placeholder="Enter full name (e.g., Tom Hanks, Christopher Nolan)"
                            className={`${styles.input} ${isNameInvalid ? styles.inputError : ''}`}
                            maxLength={50}
                            autoFocus
                            disabled={isLoading}
                            aria-describedby={isNameInvalid ? 'name-error' : 'name-help'}
                            aria-invalid={isNameInvalid ? "true" : "false"} required
                        />
                        {isNameInvalid && (
                            <span id="name-error" className={styles.error} role="alert">
                                {errors.name}
                            </span>
                        )}
                        <div id="name-help" className={styles.charCount}>
                            {formData.name.length}/50 characters
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <fieldset className={styles.roleFieldset}>
                            <legend className={styles.label}>
                                Role *
                            </legend>
                            <div className={styles.roleOptions}>
                                {Object.values(PersonRole).map(role => (
                                    <label key={role} className={styles.roleOption}>
                                        <input
                                            type="radio"
                                            name="person-role"
                                            value={role}
                                            checked={formData.role === role}
                                            onChange={(e) => handleInputChange('role', e.target.value)}
                                            className={styles.radioInput}
                                            disabled={isLoading}
                                        />
                                        <span className={styles.radioCustom}></span>
                                        <div className={styles.roleContent}>
                                            <span className={styles.roleLabel}>
                                                {role === PersonRole.ACTOR && '🎭 Actor'}
                                                {role === PersonRole.DIRECTOR && '🎬 Director'}
                                                {role === PersonRole.SCREENWRITER && '✍️ Screenwriter'}
                                            </span>
                                        </div>
                                    </label>
                                ))}
                            </div>
                        </fieldset>
                    </div>

                    <div className={styles.formActions}>
                        <button
                            type="submit"
                            className={styles.primaryButton}
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <>
                                    <span className={styles.loadingSpinner}></span>
                                    Saving...
                                </>
                            ) : (
                                person ? 'Update Person' : 'Create Person'
                            )}
                        </button>
                        <button
                            type="button"
                            className={styles.cancelButton}
                            onClick={onCancel}
                            disabled={isLoading}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};