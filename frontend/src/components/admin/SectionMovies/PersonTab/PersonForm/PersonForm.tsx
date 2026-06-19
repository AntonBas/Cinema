import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { PersonResponse, PersonRequest, PersonRole } from '@/types/person';
import { PersonRoleDisplay } from '@/types/person';
import { Modal, Input, Button, Select } from '@/components/ui';
import styles from './PersonForm.module.css';

interface PersonFormProps {
    person?: PersonResponse | null;
    onSubmit: (data: PersonRequest) => void;
    onCancel: () => void;
    isLoading?: boolean;
}

interface FormErrors {
    name?: string;
}

const NAME_MIN_LENGTH = 2;
const NAME_MAX_LENGTH = 50;

export const PersonForm: React.FC<PersonFormProps> = React.memo(({
    person,
    onSubmit,
    onCancel,
    isLoading = false
}) => {
    const [formData, setFormData] = useState<PersonRequest>(() => ({
        name: '',
        role: 'ACTOR'
    }));

    const [errors, setErrors] = useState<FormErrors>({});
    const [touched, setTouched] = useState({ name: false });

    useEffect(() => {
        if (person) {
            setFormData({
                name: person.name,
                role: person.role
            });
            setErrors({});
            setTouched({ name: false });
        }
    }, [person]);

    const validateName = useCallback((name: string): string | undefined => {
        const trimmedName = name.trim();

        if (!trimmedName) {
            return 'Name is required';
        }
        if (trimmedName.length < NAME_MIN_LENGTH) {
            return `Name must be at least ${NAME_MIN_LENGTH} characters long`;
        }
        if (trimmedName.length > NAME_MAX_LENGTH) {
            return `Name must be less than ${NAME_MAX_LENGTH} characters`;
        }

        return undefined;
    }, []);

    const validateForm = useCallback((): boolean => {
        const nameError = validateName(formData.name);
        setErrors({ name: nameError });
        return !nameError;
    }, [formData.name, validateName]);

    const handleSubmit = useCallback((e: React.FormEvent) => {
        e.preventDefault();
        setTouched({ name: true });

        if (validateForm()) {
            onSubmit({
                name: formData.name.trim(),
                role: formData.role
            });
        }
    }, [formData, validateForm, onSubmit]);

    const handleInputChange = useCallback((field: keyof PersonRequest, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: field === 'role' ? value as PersonRole : value
        }));

        if (field === 'name' && errors.name) {
            setErrors(prev => ({ ...prev, name: undefined }));
        }
    }, [errors.name]);

    const handleBlur = useCallback((field: string) => {
        setTouched(prev => ({ ...prev, [field]: true }));
        if (field === 'name') {
            const nameError = validateName(formData.name);
            setErrors(prev => ({ ...prev, name: nameError }));
        }
    }, [formData.name, validateName]);

    const handleClose = useCallback(() => {
        if (!isLoading) {
            onCancel();
        }
    }, [isLoading, onCancel]);

    const roleOptions = useMemo(() => [
        { value: 'ACTOR', label: `🎭 ${PersonRoleDisplay.ACTOR}` },
        { value: 'DIRECTOR', label: `🎬 ${PersonRoleDisplay.DIRECTOR}` },
        { value: 'SCREENWRITER', label: `✍️ ${PersonRoleDisplay.SCREENWRITER}` }
    ], []);

    const isNameInvalid = touched.name && !!errors.name;
    const charCount = formData.name.length;
    const isCharCountValid = charCount <= NAME_MAX_LENGTH;

    return (
        <Modal
            isOpen={true}
            onClose={handleClose}
            title={person ? 'Edit Person' : 'Add New Person'}
            size="small"
        >
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Full Name <span className={styles.required}>*</span>
                    </label>
                    <Input
                        type="text"
                        value={formData.name}
                        onChange={(value) => handleInputChange('name', value)}
                        onBlur={() => handleBlur('name')}
                        placeholder="Enter full name (e.g., Tom Hanks, Christopher Nolan)"
                        error={isNameInvalid ? errors.name : undefined}
                        maxLength={NAME_MAX_LENGTH}
                        disabled={isLoading}
                        autoFocus={!person}
                    />
                    <div className={`${styles.charCount} ${!isCharCountValid ? styles.error : ''}`}>
                        {charCount}/{NAME_MAX_LENGTH} characters
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Role <span className={styles.required}>*</span>
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
                        onClick={handleClose}
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
});

PersonForm.displayName = 'PersonForm';