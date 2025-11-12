import React, { useState } from 'react';
import { useUserMutation } from '@/hooks/features/user';
import { Input, Button } from '@/components/ui';
import type { UserProfile, UserUpdateRequest } from '@/types/user';
import styles from './ProfileEditForm.module.css';

interface ProfileEditFormProps {
    user: UserProfile;
    onCancel: () => void;
    onSuccess: () => void;
}

export const ProfileEditForm: React.FC<ProfileEditFormProps> = ({
    user,
    onCancel,
    onSuccess
}) => {
    const { updateProfile, isLoading, error, clearError } = useUserMutation();
    const [formData, setFormData] = useState<UserUpdateRequest>({
        firstName: user.firstName,
        lastName: user.lastName,
        dateOfBirth: user.dateOfBirth,
        city: user.city || '',
        phoneNumber: user.phoneNumber || ''
    });
    const [successMessage, setSuccessMessage] = useState('');
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        if (formErrors[field]) {
            setFormErrors(prev => ({ ...prev, [field]: '' }));
        }
        if (error) clearError();
        if (successMessage) setSuccessMessage('');
    };

    const validateForm = () => {
        const errors: Record<string, string> = {};

        if (!formData.firstName.trim()) {
            errors.firstName = 'First name is required';
        }

        if (!formData.lastName.trim()) {
            errors.lastName = 'Last name is required';
        }

        if (!formData.dateOfBirth) {
            errors.dateOfBirth = 'Date of birth is required';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            await updateProfile(formData);
            setSuccessMessage('Profile updated successfully!');
            setTimeout(() => {
                onSuccess();
            }, 1500);
        } catch (err) {
            // Error handling is done through the hook
        }
    };

    return (
        <div className={styles.profileForm}>
            <form onSubmit={handleSubmit} className={styles.form}>
                {error && (
                    <div className={styles.notification} data-type="error">
                        {error}
                    </div>
                )}

                {successMessage && (
                    <div className={styles.notification} data-type="success">
                        {successMessage}
                    </div>
                )}

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Personal Information</h2>

                    <div className={styles.formRow}>
                        <Input
                            type="text"
                            placeholder="Enter your first name"
                            value={formData.firstName}
                            onChange={(value) => handleChange('firstName', value)}
                            disabled={isLoading}
                            error={formErrors.firstName}
                        />
                        <Input
                            type="text"
                            placeholder="Enter your last name"
                            value={formData.lastName}
                            onChange={(value) => handleChange('lastName', value)}
                            disabled={isLoading}
                            error={formErrors.lastName}
                        />
                    </div>

                    <Input
                        type="date"
                        value={formData.dateOfBirth}
                        onChange={(value) => handleChange('dateOfBirth', value)}
                        disabled={isLoading}
                        error={formErrors.dateOfBirth}
                    />
                </div>

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Contact Information</h2>

                    <Input
                        type="text"
                        placeholder="Enter your phone number"
                        value={formData.phoneNumber}
                        onChange={(value) => handleChange('phoneNumber', value)}
                        disabled={isLoading}
                    />

                    <Input
                        type="text"
                        placeholder="Enter your city"
                        value={formData.city}
                        onChange={(value) => handleChange('city', value)}
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formActions}>
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={onCancel}
                        disabled={isLoading}
                        style={{ minWidth: '100px' }}
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        variant="primary"
                        loading={isLoading}
                        disabled={isLoading}
                        style={{ minWidth: '120px' }}
                    >
                        {isLoading ? 'Updating...' : 'Save Changes'}
                    </Button>
                </div>
            </form>
        </div>
    );
};