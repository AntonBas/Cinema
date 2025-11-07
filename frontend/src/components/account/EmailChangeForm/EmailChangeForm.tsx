import React, { useState } from 'react';
import { useUserMutation } from '@/hooks/features/user';
import styles from './EmailChangeForm.module.css';

export const EmailChangeForm: React.FC = () => {
    const { requestEmailChange, isLoading, error, clearError } = useUserMutation();
    const [formData, setFormData] = useState({
        newEmail: '',
        password: ''
    });
    const [successMessage, setSuccessMessage] = useState('');

    const isValidEmail = (email: string) => {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    };

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
            await requestEmailChange(formData.newEmail);
            setSuccessMessage('Confirmation email sent to your new address! Please check your inbox.');
            setFormData({
                newEmail: '',
                password: ''
            });
        } catch (err) {
        }
    };

    const isFormValid = formData.newEmail &&
        formData.password &&
        isValidEmail(formData.newEmail);

    return (
        <div className={styles.emailForm}>
            <h2 className={styles.title}>Change Email Address</h2>
            <p className={styles.description}>
                Update your email address. We'll send a confirmation link to your new email.
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

                <div className={styles.formGroup}>
                    <label htmlFor="newEmail" className={styles.label}>
                        New Email Address
                    </label>
                    <input
                        type="email"
                        id="newEmail"
                        name="newEmail"
                        value={formData.newEmail}
                        onChange={handleChange}
                        className={`${styles.input} ${formData.newEmail && !isValidEmail(formData.newEmail) ? styles.inputError : ''
                            }`}
                        placeholder="Enter your new email address"
                        required
                        disabled={isLoading}
                    />
                    {formData.newEmail && !isValidEmail(formData.newEmail) && (
                        <div className={styles.validationError}>
                            Please enter a valid email address
                        </div>
                    )}
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="password" className={styles.label}>
                        Current Password
                    </label>
                    <input
                        type="password"
                        id="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        className={styles.input}
                        placeholder="Enter your current password to confirm"
                        required
                        disabled={isLoading}
                    />
                    <div className={styles.passwordHint}>
                        For security reasons, please enter your current password to change your email.
                    </div>
                </div>

                <button
                    type="submit"
                    className={styles.submitButton}
                    disabled={!isFormValid || isLoading}
                >
                    {isLoading ? 'Sending Confirmation...' : 'Change Email Address'}
                </button>
            </form>

            <div className={styles.note}>
                <h4>Important:</h4>
                <ul>
                    <li>You will receive a confirmation email at your new address</li>
                    <li>You must click the confirmation link to complete the change</li>
                    <li>Your login email will be updated after confirmation</li>
                </ul>
            </div>
        </div>
    );
};