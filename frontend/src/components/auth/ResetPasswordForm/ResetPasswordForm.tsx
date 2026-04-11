import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Input, Button, Modal } from '@/components/ui';
import styles from './ResetPasswordForm.module.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const PasswordResetSuccessModal: React.FC<SuccessModalProps> = ({ isOpen, onClose }) => (
  <Modal isOpen={isOpen} onClose={onClose} size="small">
    <div className={styles.successContent}>
      <div className={styles.successIcon}>✅</div>
      <h3>Password Reset!</h3>
      <p>Your password has been successfully changed</p>
      <Button variant="primary" onClick={onClose}>
        Continue to Login
      </Button>
    </div>
  </Modal>
);

export const ResetPasswordForm: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { loading, error, resetPassword } = useAuthActions();

  const [formData, setFormData] = useState({ newPassword: '', confirmPassword: '' });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (formErrors[field]) {
      setFormErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

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
    if (!validateForm() || !token) return;

    const result = await resetPassword(token, formData.newPassword);
    if (result) {
      setShowSuccessModal(true);
    }
  };

  return (
    <section className={styles.resetPassword}>
      <div className={styles.resetPasswordContainer}>
        <h1 className={styles.resetPasswordTitle}>Create New Password</h1>

        {error && (
          <div className={styles.error}>{error.message}</div>
        )}

        <form onSubmit={handleSubmit} className={styles.resetPasswordForm}>
          <Input
            type="password"
            placeholder="Enter new password"
            value={formData.newPassword}
            onChange={v => handleChange('newPassword', v)}
            disabled={loading}
            error={formErrors.newPassword}
          />

          <Input
            type="password"
            placeholder="Confirm new password"
            value={formData.confirmPassword}
            onChange={v => handleChange('confirmPassword', v)}
            disabled={loading}
            error={formErrors.confirmPassword}
          />

          <Button type="submit" variant="primary" loading={loading} disabled={loading}>
            Reset Password
          </Button>
        </form>
      </div>

      <PasswordResetSuccessModal
        isOpen={showSuccessModal}
        onClose={() => {
          setShowSuccessModal(false);
          navigate('/login');
        }}
      />
    </section>
  );
};