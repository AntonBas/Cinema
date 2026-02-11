import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth/useAuth';
import { Input, Button, Modal } from '@/components/ui';
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
  return (
    <Modal isOpen={isOpen} onClose={onClose} size="small">
      <div className={styles.successContent}>
        <div className={styles.successAnimation}>
          <div className={styles.successIcon}>📧</div>
        </div>

        <div className={styles.successText}>
          <h3 className={styles.successTitle}>Instructions Sent!</h3>
          <p className={styles.successMessage}>
            We've sent password reset instructions to
          </p>
          <p className={styles.emailHighlight}>{email}</p>
        </div>

        <div className={styles.modalActions}>
          <Button
            variant="primary"
            onClick={onClose}
            style={{ minWidth: '120px' }}
          >
            Got It
          </Button>
        </div>

        <p className={styles.helpText}>
          Check your spam folder if you don't see the email
        </p>
      </div>
    </Modal>
  );
};

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { forgotPassword, loading } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);
    setIsSubmitting(true);

    try {
      await forgotPassword(email);
      setShowSuccessModal(true);
    } catch (err: any) {
      setLocalError(err.message || 'Failed to send reset instructions');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
  };

  const isLoading = isSubmitting || loading;

  return (
    <section className={styles.forgotPassword}>
      <div className={styles.forgotPasswordContainer}>
        <h2 className={styles.forgotPasswordTitle}>
          Password Recovery
        </h2>

        {localError && (
          <div className={styles.notification} data-type="error">
            {localError}
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.forgotPasswordForm}>
          <Input
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={setEmail}
            disabled={isLoading}
            error={localError ? '' : undefined}
          />

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={isLoading}
            disabled={isLoading}
            style={{ width: '100%', marginBottom: '1.5rem' }}
          >
            {isLoading ? 'Sending...' : 'Send Instructions'}
          </Button>

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