import React, { useState, useEffect } from 'react';
import type { PersonDto, PersonFormData } from '@/types/Person';
import { PersonRole } from '@/types/Person';
import styles from './PersonForm.module.css';

export interface PersonFormProps {
    person?: PersonDto | null;
    onSubmit: (data: PersonFormData) => void;
    onCancel: () => void;
}

export const PersonForm: React.FC<PersonFormProps> = ({
    person,
    onSubmit,
    onCancel
}) => {
    const [formData, setFormData] = useState<PersonFormData>({
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

    const validateForm = (): boolean => {
        const newErrors: { name?: string } = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Name is required';
        } else if (formData.name.trim().length < 2) {
            newErrors.name = 'Name must be at least 2 characters long';
        } else if (formData.name.trim().length > 100) {
            newErrors.name = 'Name must be less than 100 characters';
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

    const handleInputChange = (field: keyof PersonFormData, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
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
                        />
                        {errors.name && (
                            <span className={styles.error}>{errors.name}</span>
                        )}
                        <div className={styles.charCount}>
                            {formData.name.length}/100 characters
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
                        >
                            {person ? 'Update Person' : 'Create Person'}
                        </button>
                        <button
                            type="button"
                            className={styles.cancelButton}
                            onClick={onCancel}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};