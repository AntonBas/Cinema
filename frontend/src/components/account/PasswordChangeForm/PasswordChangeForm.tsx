import React, { useState } from 'react';
import { useUserMutation } from '@/hooks/features/user';
import styles from './PasswordChangeForm.module.css';

export const PasswordChangeForm: React.FC = () => {
    const { updatePassword, isLoading, error, clearError } = useUserMutation();
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
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

        if (formData.newPassword !== formData.confirmPassword) {
            return;
        }

        if (formData.newPassword.length < 8) {
            return;
        }

        try {
            await updatePassword(formData.newPassword);
            setSuccessMessage('Password updated successfully!');
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
        } catch (err) {
        }
    };

    const passwordsMatch = formData.newPassword === formData.confirmPassword;
    const isFormValid = formData.currentPassword &&
        formData.newPassword &&
        formData.confirmPassword &&
        passwordsMatch &&
        formData.newPassword.length >= 8;

    return (
        <div className={styles.passwordForm}>
            <h2 className={styles.title}>Change Password</h2>
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

                <div className={styles.formGroup}>
                    <label htmlFor="currentPassword" className={styles.label}>
                        Current Password
                    </label>
                    <input
                        type="password"
                        id="currentPassword"
                        name="currentPassword"
                        value={formData.currentPassword}
                        onChange={handleChange}
                        className={styles.input}
                        placeholder="Enter your current password"
                        required
                        disabled={isLoading}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="newPassword" className={styles.label}>
                        New Password
                    </label>
                    <input
                        type="password"
                        id="newPassword"
                        name="newPassword"
                        value={formData.newPassword}
                        onChange={handleChange}
                        className={styles.input}
                        placeholder="Enter new password (min 8 characters)"
                        required
                        disabled={isLoading}
                    />
                    {formData.newPassword && formData.newPassword.length < 8 && (
                        <div className={styles.validationError}>
                            Password must be at least 8 characters long
                        </div>
                    )}
                </div>

                <div className={styles.formGroup}>
                    <label htmlFor="confirmPassword" className={styles.label}>
                        Confirm New Password
                    </label>
                    <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        className={`${styles.input} ${formData.confirmPassword && !passwordsMatch ? styles.inputError : ''
                            }`}
                        placeholder="Confirm your new password"
                        required
                        disabled={isLoading}
                    />
                    {formData.confirmPassword && !passwordsMatch && (
                        <div className={styles.validationError}>
                            Passwords do not match
                        </div>
                    )}
                </div>

                <button
                    type="submit"
                    className={styles.submitButton}
                    disabled={!isFormValid || isLoading}
                >
                    {isLoading ? 'Updating Password...' : 'Update Password'}
                </button>
            </form>
        </div>
    );
};