import React, { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { authApi } from "@/features/auth/api/authApi";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { CheckCircle2 } from "lucide-react";
import { useAuthActions } from "@/features/auth/hooks/useAuthActions";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { Modal } from "@/components/ui/Modal/Modal";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import { validatePassword } from "@/utils/formValidation";
import { AuthCard } from "@/features/auth/components/AuthCard/AuthCard";
import styles from "./ResetPasswordForm.module.css";

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const PasswordResetSuccessModal: React.FC<SuccessModalProps> = ({
  isOpen,
  onClose,
}) => (
  <Modal isOpen={isOpen} onClose={onClose} size="small">
    <div className={styles.successContent}>
      <div className={styles.successAnimation}>
        <CheckCircle2 size={64} className={styles.successIcon} />
      </div>
      <div className={styles.successText}>
        <h3 className={styles.successTitle}>Password Reset!</h3>
        <p className={styles.successMessage}>
          Your password has been successfully changed.
        </p>
      </div>
      <div className={styles.modalActions}>
        <Button variant="primary" onClick={onClose} fullWidth>
          Continue to Login
        </Button>
      </div>
    </div>
  </Modal>
);

export const ResetPasswordForm: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { loading, error, resetPassword } = useAuthActions();

  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [tokenError, setTokenError] = useState<string | null>(null);
  const [tokenChecked, setTokenChecked] = useState(false);

  useEffect(() => {
    let active = true;
    authApi
      .validateResetToken(token ?? "")
      .then(() => active && setTokenError(null))
      .catch((err) => {
        if (!active) return;
        setTokenError(
          isApiErrorException(err)
            ? err.message
            : "This password reset link is invalid or has expired.",
        );
      })
      .finally(() => active && setTokenChecked(true));
    return () => {
      active = false;
    };
  }, [token]);

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (formErrors[field]) {
      setFormErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    const newPasswordError = validatePassword(formData.newPassword);
    if (newPasswordError) {
      errors.newPassword = newPasswordError;
    }

    if (!formData.confirmPassword) {
      errors.confirmPassword = "Please confirm your new password";
    } else if (formData.newPassword !== formData.confirmPassword) {
      errors.confirmPassword = "Passwords do not match";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm() || !token) return;

    try {
      await resetPassword(token, formData.newPassword);
      setShowSuccessModal(true);
    } catch (err) {
      if (isApiErrorException(err) && err.isValidationError()) {
        setFormErrors(err.getValidationErrors());
      }
    }
  };

  if (!tokenChecked) {
    return <LoadingSpinner text="Checking your reset link..." />;
  }

  if (tokenError) {
    return (
      <AuthCard title="Reset Link Not Valid">
        <div className={styles.notification} data-type="error">
          {tokenError}
        </div>
        <p className={styles.instructionText}>
          The link may have expired or already been used. Request a new one to
          reset your password.
        </p>
        <Link to="/forgot-password">
          <Button variant="primary" size="large" fullWidth>
            Request New Link
          </Button>
        </Link>
      </AuthCard>
    );
  }

  return (
    <>
      <AuthCard title="Create New Password">
        <p className={styles.instructionText}>
          Enter and confirm your new password below.
        </p>

        <form onSubmit={handleSubmit} className={styles.resetPasswordForm}>
          {error && (
            <div className={styles.notification} data-type="error">
              {error.message}
            </div>
          )}

          <Input
            type="password"
            placeholder="New password"
            value={formData.newPassword}
            onChange={(v) => handleChange("newPassword", v)}
            disabled={loading}
            error={formErrors.newPassword}
          />

          <Input
            type="password"
            placeholder="Confirm new password"
            value={formData.confirmPassword}
            onChange={(v) => handleChange("confirmPassword", v)}
            disabled={loading}
            error={formErrors.confirmPassword}
          />

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={loading}
            disabled={loading}
            className={styles.submitButton}
          >
            {loading ? "Resetting..." : "Reset Password"}
          </Button>
        </form>
      </AuthCard>

      <PasswordResetSuccessModal
        isOpen={showSuccessModal}
        onClose={() => {
          setShowSuccessModal(false);
          navigate("/login");
        }}
      />
    </>
  );
};
