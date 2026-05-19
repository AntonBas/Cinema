import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Mail } from "lucide-react";
import { useAuthActions } from "@/hooks/features/auth/useAuthActions";
import { Input, Button, Modal } from "@/components/ui";
import styles from "./ForgotPasswordForm.module.css";

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState("");
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { loading, error, forgotPassword } = useAuthActions();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await forgotPassword(email);
    setShowSuccessModal(true);
  };

  return (
    <>
      <section className={styles.forgotPassword}>
        <div className={styles.forgotPasswordContainer}>
          <h1 className={styles.forgotPasswordTitle}>Reset your password</h1>

          <div className={styles.forgotPasswordTop}>
            <span>Remember your password?</span>
            <Link to="/login">Login</Link>
          </div>

          <form onSubmit={handleSubmit} className={styles.forgotPasswordForm}>
            {error && (
              <div className={styles.notification} data-type="error">
                {error.message}
              </div>
            )}

            <p className={styles.instructionText}>
              Enter your email address and we'll send you instructions to reset
              your password.
            </p>

            <Input
              type="email"
              placeholder="Email address"
              value={email}
              onChange={setEmail}
              disabled={loading}
              required
            />

            <Button
              type="submit"
              variant="primary"
              size="large"
              loading={loading}
              disabled={loading}
              className={styles.submitButton}
            >
              {loading ? "Sending..." : "Send Reset Instructions"}
            </Button>
          </form>

          <div className={styles.forgotPasswordBottom}>
            <Link to="/register">Don't have an account? Sign up</Link>
          </div>
        </div>
      </section>

      <Modal
        isOpen={showSuccessModal}
        onClose={() => setShowSuccessModal(false)}
        size="small"
      >
        <div className={styles.successContent}>
          <div className={styles.successAnimation}>
            <Mail size={64} className={styles.successIcon} />
          </div>
          <div className={styles.successText}>
            <h3 className={styles.successTitle}>Check your email!</h3>
            <p className={styles.successMessage}>
              We've sent password reset instructions to
            </p>
            <p className={styles.emailHighlight}>{email}</p>
          </div>
          <div className={styles.modalActions}>
            <Button
              variant="primary"
              onClick={() => setShowSuccessModal(false)}
              style={{ width: "100%" }}
            >
              Got It
            </Button>
          </div>
          <p className={styles.helpText}>
            Check your spam folder if you don't see the email
          </p>
        </div>
      </Modal>
    </>
  );
};
