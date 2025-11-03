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

    useEffect(() => {
        if (person) {
            setFormData({
                name: person.name,
                role: person.role
            });
        } else {
            setFormData({
                name: '',
                role: PersonRole.ACTOR
            });
        }
        setErrors({});
    }, [person]);

    useEffect(() => {
        const handleEscape = (e: KeyboardEvent) => {
            if (e.key === 'Escape') {
                onCancel();
            }
        };

        document.addEventListener('keydown', handleEscape);
        return () => document.removeEventListener('keydown', handleEscape);
    }, [onCancel]);

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

    const getRoleDescription = (role: PersonRole): string => {
        switch (role) {
            case PersonRole.ACTOR: return 'Appears in movies as cast member';
            case PersonRole.DIRECTOR: return 'Directs and oversees movie production';
            case PersonRole.SCREENWRITER: return 'Writes movie scripts and screenplays';
            default: return '';
        }
    };

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modal}>
                <div className={styles.header}>
                    <h3>{person ? 'Edit Person' : 'Add New Person'}</h3>
                    <button
                        className={styles.closeButton}
                        onClick={onCancel}
                        type="button"
                        disabled={isLoading}
                    >
                        ×
                    </button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Full Name *
                        </label>
                        <input
                            type="text"
                            value={formData.name}
                            onChange={(e) => handleInputChange('name', e.target.value)}
                            placeholder="Enter full name (e.g., Tom Hanks, Christopher Nolan)"
                            className={`${styles.input} ${errors.name ? styles.inputError : ''}`}
                            maxLength={100}
                            autoFocus
                        />
                        {errors.name && (
                            <span className={styles.error}>{errors.name}</span>
                        )}
                        <div className={styles.charCount}>
                            {formData.name.length}/50 characters
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Role *
                        </label>
                        <div className={styles.roleOptions}>
                            {Object.values(PersonRole).map(role => (
                                <label key={role} className={styles.roleOption}>
                                    <input
                                        type="radio"
                                        name="role"
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
                                        <span className={styles.roleDescription}>
                                            {getRoleDescription(role)}
                                        </span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    </div>

                    <div className={styles.formActions}>
                        <button
                            type="submit"
                            className={styles.primaryButton}
                            disabled={isLoading}
                        >
                            {isLoading ? 'Saving...' : (person ? 'Update Person' : 'Create Person')}
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