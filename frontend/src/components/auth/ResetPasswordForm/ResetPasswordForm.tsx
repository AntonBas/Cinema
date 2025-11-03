import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuthMutation } from '@/hooks/features/auth';
import styles from './ResetPasswordForm.module.css';

export const ResetPasswordForm: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });
  const [message, setMessage] = useState('');
  const { resetPassword, isLoading, error } = useAuthMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.newPassword !== formData.confirmPassword) {
      return;
    }

    if (!token) {
      return;
    }

    try {
      await resetPassword(token, formData.newPassword);
      setMessage('Password has been successfully changed');
      setTimeout(() => navigate('/login'), 3000);
    } catch (err) {
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  return (
    <div className={styles.resetPasswordContainer}>
      <h2 className={styles.resetPasswordTitle}>
        Set New Password
      </h2>

      {message && (
        <div className={styles.resetPasswordMessage}>
          {message}
        </div>
      )}

      {error && (
        <div className={styles.resetPasswordError}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className={styles.resetPasswordForm}>
        <div className={styles.formGroup}>
          <label htmlFor="newPassword" className={styles.formLabel}>
            New Password
          </label>
          <input
            type="password"
            id="newPassword"
            name="newPassword"
            value={formData.newPassword}
            onChange={handleChange}
            required
            minLength={6}
            placeholder="Enter new password"
            className={styles.formInput}
            disabled={isLoading}
          />
        </div>

        <div className={styles.formGroup}>
          <label htmlFor="confirmPassword" className={styles.formLabel}>
            Confirm Password
          </label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            minLength={6}
            placeholder="Confirm new password"
            className={styles.formInput}
            disabled={isLoading}
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className={styles.resetPasswordButton}
        >
          {isLoading ? 'Resetting...' : 'Reset Password'}
        </button>

        <div className={styles.resetPasswordLinks}>
          <Link to="/login" className={styles.backLink}>
            Back to Login
          </Link>
        </div>
      </form>
    </div>
  );
};