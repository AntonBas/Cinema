import React, { useState } from 'react';
import { useUserMutation } from '@/hooks/features/user';
import { Input, Button } from '@/components/ui';
import styles from './PasswordChangeForm.module.css';

export const PasswordChangeForm: React.FC = () => {
    const { updatePassword, isLoading, error, clearError } = useUserMutation();
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
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

        if (!formData.currentPassword) {
            errors.currentPassword = 'Current password is required';
        }

        if (!formData.newPassword) {
            errors.newPassword = 'New password is required';
        } else if (formData.newPassword.length < 8) {
            errors.newPassword = 'Password must be at least 8 characters';
        }

        if (!formData.confirmPassword) {
            errors.confirmPassword = 'Please confirm your new password';
        } else if (formData.newPassword !== formData.confirmPassword) {
            errors.confirmPassword = 'Passwords do not match';
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
            await updatePassword(
                formData.currentPassword,
                formData.newPassword,
                formData.confirmPassword
            );
            setSuccessMessage('Password updated successfully!');
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setFormErrors({});
        } catch (err) {
        }
    };

    return (
        <div className={styles.passwordForm}>
            <h1 className={styles.title}>Change Password</h1>
            <p className={styles.description}>
                Update your password to keep your account secure.
            </p>

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

                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>Password Details</h2>

                    <Input
                        type="password"
                        placeholder="Enter your current password"
                        value={formData.currentPassword}
                        onChange={(value) => handleChange('currentPassword', value)}
                        disabled={isLoading}
                        error={formErrors.currentPassword}
                    />

                    <Input
                        type="password"
                        placeholder="Enter new password (min 8 characters)"
                        value={formData.newPassword}
                        onChange={(value) => handleChange('newPassword', value)}
                        disabled={isLoading}
                        error={formErrors.newPassword}
                    />

                    <Input
                        type="password"
                        placeholder="Confirm your new password"
                        value={formData.confirmPassword}
                        onChange={(value) => handleChange('confirmPassword', value)}
                        disabled={isLoading}
                        error={formErrors.confirmPassword}
                    />
                </div>

                <Button
                    type="submit"
                    variant="primary"
                    size="large"
                    loading={isLoading}
                    disabled={isLoading}
                    style={{ width: '100%' }}
                >
                    {isLoading ? 'Updating Password...' : 'Update Password'}
                </Button>
            </form>
        </div>
    );
};