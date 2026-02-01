import React, { useState } from 'react';
import { useUser } from '@/hooks/features/user';
import { Input, Button, Notification } from '@/components/ui';
import styles from './PasswordChangeForm.module.css';

export const PasswordChangeForm: React.FC = () => {
    const { updatePassword, isLoading } = useUser();
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [showSuccess, setShowSuccess] = useState(false);
    const [showError, setShowError] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        if (formErrors[field]) {
            setFormErrors(prev => ({ ...prev, [field]: '' }));
        }
        if (errorMessage) {
            setErrorMessage('');
        }
        if (showSuccess) setShowSuccess(false);
        if (showError) setShowError(false);
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
            setShowSuccess(true);
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setFormErrors({});
            setErrorMessage('');
        } catch (err) {
            setErrorMessage('Failed to update password. Please try again.');
            setShowError(true);
        }
    };

    const handleCloseNotification = (id: string) => {
        if (id === 'success') setShowSuccess(false);
        if (id === 'error') setShowError(false);
    };

    return (
        <div className={styles.passwordForm}>
            <Notification
                id="success"
                message="Password updated successfully!"
                type="success"
                isVisible={showSuccess}
                onClose={handleCloseNotification}
                duration={5000}
            />

            <Notification
                id="error"
                message={errorMessage}
                type="error"
                isVisible={showError}
                onClose={handleCloseNotification}
                duration={5000}
            />

            <h1 className={styles.title}>Change Password</h1>
            <p className={styles.description}>
                Update your password to keep your account secure.
            </p>

            <form onSubmit={handleSubmit} className={styles.form}>
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