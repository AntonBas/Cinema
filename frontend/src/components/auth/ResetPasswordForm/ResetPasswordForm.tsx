import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthMutation } from '@/hooks/features/auth';
import { Input, Button, Modal } from '@/components/ui';
import styles from './ResetPasswordForm.module.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const PasswordResetSuccessModal: React.FC<SuccessModalProps> = ({
  isOpen,
  onClose
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} size="small">
      <div className={styles.successContent}>
        <div className={styles.successAnimation}>
          <div className={styles.successIcon}>✅</div>
        </div>

        <div className={styles.successText}>
          <h3 className={styles.successTitle}>Password Reset!</h3>
          <p className={styles.successMessage}>
            Your password has been successfully changed
          </p>
        </div>

        <div className={styles.modalActions}>
          <Button
            variant="primary"
            onClick={onClose}
            style={{ minWidth: '120px' }}
          >
            Continue to Login
          </Button>
        </div>
      </div>
    </Modal>
  );
};

export const ResetPasswordForm: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const { resetPassword, isLoading, error } = useAuthMutation();

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    if (formErrors[field]) {
      setFormErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (formData.newPassword.length < 8) {
      errors.newPassword = 'Password must be at least 8 characters';
    }

    if (formData.newPassword !== formData.confirmPassword) {
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

    if (!token) {
      return;
    }

    try {
      await resetPassword(token, formData.newPassword);
      setShowSuccessModal(true);
    } catch (err) { }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
    navigate('/login');
  };

  const getErrorMessage = () => {
    if (!error) return null;

    if (error.includes('cannot be the same')) {
      return 'New password cannot be the same as your current password';
    }

    return error;
  };

  const errorMessage = getErrorMessage();

  return (
    <section className={styles.resetPassword}>
      <div className={styles.resetPasswordContainer}>
        <h1 className={styles.resetPasswordTitle}>
          Set New Password
        </h1>

        {errorMessage && (
          <div className={styles.notification} data-type="error">
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.resetPasswordForm}>
          <div className={styles.formSection}>
            <h2 className={styles.sectionTitle}>Create New Password</h2>

            <Input
              type="password"
              placeholder="Enter new password"
              value={formData.newPassword}
              onChange={(value) => handleChange('newPassword', value)}
              disabled={isLoading}
              error={formErrors.newPassword}
            />

            <Input
              type="password"
              placeholder="Confirm new password"
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
            {isLoading ? 'Resetting...' : 'Reset Password'}
          </Button>
        </form>
      </div>

      <PasswordResetSuccessModal
        isOpen={showSuccessModal}
        onClose={handleModalClose}
      />
    </section>
  );
};