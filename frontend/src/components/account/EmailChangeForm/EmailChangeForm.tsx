import React, { useState } from 'react';
import { useUser } from '@/hooks/features/user/useUser';
import { Input, Button, Notification, Tooltip } from '@/components/ui';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './EmailChangeForm.module.css';

export const EmailChangeForm: React.FC = () => {
    const { requestEmailChange, isEmailChanging } = useUser();
    const [formData, setFormData] = useState({
        newEmail: '',
        password: ''
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
            await requestEmailChange(formData.newEmail, formData.password);
            setShowSuccess(true);
            setFormData({
                newEmail: '',
                password: ''
            });
            setFormErrors({});
            setErrorMessage('');

            setTimeout(() => {
                setShowSuccess(false);
            }, 5000);
        } catch (err) {
            if (isApiErrorException(err)) {
                setErrorMessage(err.message);
            } else if (err instanceof Error) {
                setErrorMessage(err.message);
            } else {
                setErrorMessage('Failed to request email change. Please try again.');
            }
            setShowError(true);

            setTimeout(() => {
                setShowError(false);
            }, 5000);
        }
    };

    return (
        <div className={styles.emailForm}>
            {showSuccess && (
                <Notification
                    id="success"
                    message="Confirmation email sent to your new address! Please check your inbox."
                    type="success"
                    isVisible={showSuccess}
                    onClose={() => setShowSuccess(false)}
                    duration={5000}
                    isStatic={true}
                />
            )}

            {showError && (
                <Notification
                    id="error"
                    message={errorMessage}
                    type="error"
                    isVisible={showError}
                    onClose={() => setShowError(false)}
                    duration={5000}
                    isStatic={true}
                />
            )}

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
                        disabled={isEmailChanging}
                        error={formErrors.newEmail}
                    />

                    <Input
                        type="password"
                        placeholder="Enter your current password to confirm"
                        value={formData.password}
                        onChange={(value) => handleChange('password', value)}
                        disabled={isEmailChanging}
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
                            loading={isEmailChanging}
                            disabled={isEmailChanging}
                        >
                            {isEmailChanging ? 'Sending Confirmation...' : 'Change Email Address'}
                        </Button>
                    </Tooltip>
                </div>
            </form>
        </div>
    );
};