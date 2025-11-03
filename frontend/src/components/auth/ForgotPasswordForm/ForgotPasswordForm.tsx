import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuthMutation } from '@/hooks/features/auth';
import styles from './ForgotPasswordForm.module.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

const PasswordResetModal: React.FC<SuccessModalProps> = ({
  isOpen,
  onClose,
  email
}) => {
  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.successAnimation}>
          <div className={styles.checkmark}>✓</div>
        </div>
        <h3>Instructions Sent! 📧</h3>
        <p>We've sent password reset instructions to <strong>{email}</strong></p>
        <div className={styles.modalActions}>
          <button className={styles.btnPrimary} onClick={onClose}>
            Got It
          </button>
        </div>
        <p className={styles.helpText}>Check your spam folder if you don't see the email</p>
      </div>
    </div>
  );
};

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const { forgotPassword, isLoading, error } = useAuthMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await forgotPassword(email);
      setShowSuccessModal(true);
    } catch (err) {
      // Помилка вже оброблена в хуку
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
  };

  return (
    <section className={styles.forgotPassword}>
      <div className={styles.forgotPasswordContainer}>
        <h2 className={styles.forgotPasswordTitle}>
          Password Recovery
        </h2>

        {error && (
          <div className={styles.forgotPasswordError}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.forgotPasswordForm}>
          <div className={styles.formGroup}>
            <label htmlFor="email" className={styles.formLabel}>
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="Enter your email"
              className={styles.formInput}
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className={styles.forgotPasswordButton}
          >
            {isLoading ? 'Sending...' : 'Send Instructions'}
          </button>

          <div className={styles.forgotPasswordLinks}>
            <Link to="/login" className={styles.backLink}>
              Back to Login
            </Link>
          </div>
        </form>
      </div>

      <PasswordResetModal
        isOpen={showSuccessModal}
        onClose={handleModalClose}
        email={email}
      />
    </section>
  );
};