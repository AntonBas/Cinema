import React, { useState } from 'react';
import { Input, Button } from '@/components/ui';
import type { UserProfileResponse, UserUpdateRequest } from '@/types/user';
import styles from './ProfileEditForm.module.css';

interface ProfileEditFormProps {
    user: UserProfileResponse;
    onCancel: () => void;
    onSuccess: (data: UserUpdateRequest) => Promise<void>;
    loading?: boolean;
}

export const ProfileEditForm: React.FC<ProfileEditFormProps> = ({
    user,
    onCancel,
    onSuccess,
    loading = false
}) => {
    const [formData, setFormData] = useState<UserUpdateRequest>({
        firstName: user.firstName,
        lastName: user.lastName,
        dateOfBirth: user.dateOfBirth,
        city: user.city || '',
        phoneNumber: user.phoneNumber || ''
    });
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});

    const handleChange = (field: keyof UserUpdateRequest, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }));
        if (formErrors[field]) {
            setFormErrors(prev => ({ ...prev, [field]: '' }));
        }
    };

    const validateForm = (): boolean => {
        const errors: Record<string, string> = {};

        if (!formData.firstName?.trim()) errors.firstName = 'First name is required';
        if (!formData.lastName?.trim()) errors.lastName = 'Last name is required';
        if (!formData.dateOfBirth) errors.dateOfBirth = 'Date of birth is required';

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;
        await onSuccess(formData);
    };

    return (
        <div className={styles.profileForm}>
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Personal Information</h2>

                    <div className={styles.formRow}>
                        <Input
                            type="text"
                            placeholder="Enter your first name"
                            value={formData.firstName || ''}
                            onChange={value => handleChange('firstName', value)}
                            disabled={loading}
                            error={formErrors.firstName}
                            label="First Name"
                        />
                        <Input
                            type="text"
                            placeholder="Enter your last name"
                            value={formData.lastName || ''}
                            onChange={value => handleChange('lastName', value)}
                            disabled={loading}
                            error={formErrors.lastName}
                            label="Last Name"
                        />
                    </div>

                    <Input
                        type="date"
                        value={formData.dateOfBirth || ''}
                        onChange={value => handleChange('dateOfBirth', value)}
                        disabled={loading}
                        error={formErrors.dateOfBirth}
                        label="Date of Birth"
                    />
                </div>

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Contact Information</h2>

                    <Input
                        type="text"
                        placeholder="Enter your phone number"
                        value={formData.phoneNumber || ''}
                        onChange={value => handleChange('phoneNumber', value)}
                        disabled={loading}
                        label="Phone Number"
                    />

                    <Input
                        type="text"
                        placeholder="Enter your city"
                        value={formData.city || ''}
                        onChange={value => handleChange('city', value)}
                        disabled={loading}
                        label="City"
                    />
                </div>

                <div className={styles.formActions}>
                    <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary" loading={loading} disabled={loading}>
                        Save Changes
                    </Button>
                </div>
            </form>
        </div>
    );
};