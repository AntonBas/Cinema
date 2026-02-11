import React, { useState, useEffect } from 'react';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import { PersonRoleDisplay } from '@/types/person';
import { Modal, Input, Button, Select } from '@/components/ui';
import styles from './PersonForm.module.css';

export interface PersonFormProps {
    person?: PersonResponse | null;
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
        role: 'ACTOR'
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

    const roleOptions = [
        { value: 'ACTOR', label: `🎭 ${PersonRoleDisplay.ACTOR}` },
        { value: 'DIRECTOR', label: `🎬 ${PersonRoleDisplay.DIRECTOR}` },
        { value: 'SCREENWRITER', label: `✍️ ${PersonRoleDisplay.SCREENWRITER}` }
    ];

    return (
        <Modal
            isOpen={true}
            onClose={onCancel}
            title={person ? 'Edit Person' : 'Add New Person'}
            size="small"
        >
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Full Name *
                    </label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => handleInputChange('name', value)}
                        onBlur={() => handleBlur('name')}
                        placeholder="Enter full name (e.g., Tom Hanks, Christopher Nolan)"
                        error={isNameInvalid ? errors.name : undefined}
                        maxLength={50}
                        disabled={isLoading}
                    />
                    <div className={styles.charCount}>
                        {formData.name.length}/50 characters
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Role *
                    </label>
                    <Select
                        value={formData.role}
                        onChange={(value) => handleInputChange('role', value as string)}
                        options={roleOptions}
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formActions}>
                    <Button
                        type="button"
                        variant="cancel"
                        onClick={onCancel}
                        disabled={isLoading}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        loading={isLoading}
                        disabled={isLoading}
                    >
                        {person ? 'Update Person' : 'Create Person'}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};