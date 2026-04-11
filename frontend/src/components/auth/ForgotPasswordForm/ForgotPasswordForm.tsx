import React, { useState } from 'react';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Input, Button, Modal } from '@/components/ui';
import styles from './ForgotPasswordForm.module.css';

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { loading, forgotPassword } = useAuthActions();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await forgotPassword(email);
    setShowSuccessModal(true);
  };

  return (
    <>
      <section className={styles.forgotPassword}>
        <div className={styles.forgotPasswordContainer}>
          <h2 className={styles.forgotPasswordTitle}>Password Recovery</h2>

          <form onSubmit={handleSubmit} className={styles.forgotPasswordForm}>
            <Input
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={setEmail}
              disabled={loading}
              required
            />

            <Button type="submit" variant="primary" loading={loading} disabled={loading}>
              Send Instructions
            </Button>
          </form>
        </div>
      </section>

      <Modal isOpen={showSuccessModal} onClose={() => setShowSuccessModal(false)} size="small">
        <div className={styles.successContent}>
          <h3>Instructions Sent!</h3>
          <p>We've sent password reset instructions to</p>
          <p className={styles.emailHighlight}>{email}</p>
          <Button variant="primary" onClick={() => setShowSuccessModal(false)}>
            Got It
          </Button>
          <p className={styles.helpText}>Check your spam folder if you don't see the email</p>
        </div>
      </Modal>
    </>
  );
};