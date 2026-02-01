import React, { useState } from 'react';
import { useUser } from '@/hooks/features/user';
import { Input, Button, Notification, Tooltip } from '@/components/ui';
import styles from './EmailChangeForm.module.css';

export const EmailChangeForm: React.FC = () => {
    const { requestEmailChange, isLoading } = useUser();
    const [formData, setFormData] = useState({
        newEmail: '',
        password: ''
    });
    const [showSuccess, setShowSuccess] = useState(false);
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});

    const handleChange = (field: string, value: string) => {
        setFormData(prev => ({
            ...prev,
            [field]: value
        }));

        if (formErrors[field]) {
            setFormErrors(prev => ({ ...prev, [field]: '' }));
        }
        if (showSuccess) setShowSuccess(false);
    };

    const validateForm = () => {
        const errors: Record<string, string> = {};

        if (!formData.newEmail) {
            errors.newEmail = 'Email is required';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.newEmail)) {
            errors.newEmail = 'Please enter a valid email address';
        }

        if (!formData.password) {
            errors.password = 'Password is required';
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
            await requestEmailChange(formData.newEmail);
            setShowSuccess(true);
            setFormData({
                newEmail: '',
                password: ''
            });
            setFormErrors({});
        } catch {
        }
    };

    const handleCloseNotification = (id: string) => {
        if (id === 'success') setShowSuccess(false);
    };

    return (
        <div className={styles.emailForm}>
            <Notification
                id="success"
                message="Confirmation email sent to your new address! Please check your inbox."
                type="success"
                isVisible={showSuccess}
                onClose={handleCloseNotification}
                duration={5000}
            />

            <h1 className={styles.title}>Change Email Address</h1>
            <p className={styles.description}>
                Update your email address. We'll send a confirmation link to your new email.
            </p>

            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formSection}>
                    <h2 className={styles.sectionTitle}>New Email Details</h2>

                    <Input
                        type="email"
                        placeholder="Enter your new email address"
                        value={formData.newEmail}
                        onChange={(value) => handleChange('newEmail', value)}
                        disabled={isLoading}
                        error={formErrors.newEmail}
                    />

                    <Input
                        type="password"
                        placeholder="Enter your current password to confirm"
                        value={formData.password}
                        onChange={(value) => handleChange('password', value)}
                        disabled={isLoading}
                        error={formErrors.password}
                    />

                    <div className={styles.passwordHint}>
                        For security reasons, please enter your current password to change your email.
                    </div>
                </div>

                <div className={styles.buttonWrapper}>
                    <Tooltip
                        content={`Important:\n• You will receive a confirmation email at your new address\n• You must click the confirmation link to complete the change\n• Your login email will be updated after confirmation`}
                        position="top"
                    >
                        <Button
                            type="submit"
                            variant="primary"
                            size="large"
                            loading={isLoading}
                            disabled={isLoading}
                        >
                            {isLoading ? 'Sending Confirmation...' : 'Change Email Address'}
                        </Button>
                    </Tooltip>
                </div>
            </form>
        </div>
    );
};