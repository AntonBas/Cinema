import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Mail } from "lucide-react";
import { useAuthActions } from "@/features/auth/hooks/useAuthActions";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { Modal } from "@/components/ui/Modal/Modal";
import { AuthCard } from "@/features/auth/components/AuthCard/AuthCard";
import { validateEmail } from "@/utils/formValidation";
import styles from "./ForgotPasswordForm.module.css";

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState("");
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [emailError, setEmailError] = useState<string>();

  const { loading, error, forgotPassword } = useAuthActions();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const validationError = validateEmail(email);
    setEmailError(validationError);
    if (validationError) return;
    try {
      await forgotPassword(email);
    } catch {
      return;
    }
    setShowSuccessModal(true);
  };

  return (
    <>
      <AuthCard title="Reset Your Password">
        <div className={styles.forgotPasswordTop}>
          <span>Remember your password?</span>
          <Link to="/login">Login</Link>
        </div>

        <form
          onSubmit={handleSubmit}
          className={styles.forgotPasswordForm}
          noValidate
        >
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
            label="Email"
            placeholder="Email address"
            value={email}
            onChange={(value) => {
              setEmail(value);
              setEmailError(undefined);
            }}
            disabled={loading}
            error={emailError}
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
      </AuthCard>

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
            <h3 className={styles.successTitle}>Check Your Email!</h3>
            <p className={styles.successMessage}>
              We've sent password reset instructions to
            </p>
            <p className={styles.emailHighlight}>{email}</p>
          </div>
          <div className={styles.modalActions}>
            <Button
              variant="primary"
              onClick={() => setShowSuccessModal(false)}
              fullWidth
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
