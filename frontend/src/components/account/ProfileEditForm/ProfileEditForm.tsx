import React, { useState } from 'react';
import { useUserMutation } from '@/hooks/features/user';
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

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        if (error) clearError();
        if (successMessage) setSuccessMessage('');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            await updateProfile(formData);
            setSuccessMessage('Profile updated successfully!');
            setTimeout(() => {
                onSuccess();
            }, 1500);
        } catch (err) {
        }
    };

    const isFormValid = formData.firstName &&
        formData.lastName &&
        formData.dateOfBirth;

    return (
        <div className={styles.profileForm}>
            <form onSubmit={handleSubmit} className={styles.form}>
                {error && (
                    <div className={styles.errorMessage}>
                        {error}
                    </div>
                )}

                {successMessage && (
                    <div className={styles.successMessage}>
                        {successMessage}
                    </div>
                )}

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label htmlFor="firstName" className={styles.label}>
                            First Name *
                        </label>
                        <input
                            type="text"
                            id="firstName"
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleChange}
                            className={styles.input}
                            placeholder="Enter your first name"
                            required
                            disabled={isLoading}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="lastName" className={styles.label}>
                            Last Name *
                        </label>
                        <input
                            type="text"
                            id="lastName"
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleChange}
                            className={styles.input}
                            placeholder="Enter your last name"
                            required
                            disabled={isLoading}
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="dateOfBirth" className={styles.label}>
                        Date of Birth *
                    </label>
                    <input
                        type="date"
                        id="dateOfBirth"
                        name="dateOfBirth"
                        value={formData.dateOfBirth}
                        onChange={handleChange}
                        className={styles.input}
                        required
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="phoneNumber" className={styles.label}>
                        Phone Number
                    </label>
                    <input
                        type="tel"
                        id="phoneNumber"
                        name="phoneNumber"
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        className={styles.input}
                        placeholder="Enter your phone number"
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="city" className={styles.label}>
                        City
                    </label>
                    <input
                        type="text"
                        id="city"
                        name="city"
                        value={formData.city}
                        onChange={handleChange}
                        className={styles.input}
                        placeholder="Enter your city"
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formActions}>
                    <button
                        type="button"
                        className={styles.cancelButton}
                        onClick={onCancel}
                        disabled={isLoading}
                    >
                        Cancel
                    </button>
                    <button
                        type="submit"
                        className={styles.submitButton}
                        disabled={!isFormValid || isLoading}
                    >
                        {isLoading ? 'Updating...' : 'Save Changes'}
                    </button>
                </div>
            </form>
        </div>
    );
};