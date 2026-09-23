import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuthActions } from "@/hooks/features/auth/useAuthActions";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { Modal } from "@/components/ui/Modal/Modal";
import type { RegisterRequest } from "@/types/auth";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import {
  validateName,
  validatePhoneNumber,
  validatePassword,
} from "@/utils/formValidation";
import { AuthCard } from "@/components/auth/AuthCard/AuthCard";
import styles from "./RegisterForm.module.css";

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

const RegistrationSuccessModal: React.FC<SuccessModalProps> = ({
  isOpen,
  onClose,
  email,
}) => (
  <Modal isOpen={isOpen} onClose={onClose} size="small">
    <div className={styles.successContent}>
      <div className={styles.successText}>
        <h3 className={styles.successTitle}>Registration Successful!</h3>
        <p className={styles.successMessage}>
          We've sent a confirmation email to
        </p>
        <p className={styles.emailHighlight}>{email}</p>
      </div>
      <div className={styles.modalActions}>
        <Button variant="primary" onClick={onClose} fullWidth>
          Continue to Login
        </Button>
      </div>
      <p className={styles.helpText}>
        Check your spam folder if you don't see the email
      </p>
    </div>
  </Modal>
);

export const RegisterForm: React.FC = () => {
  const navigate = useNavigate();
  const { loading, error, register } = useAuthActions();

  const [formData, setFormData] = useState<RegisterRequest>({
    firstName: "",
    lastName: "",
    dateOfBirth: "",
    city: "",
    email: "",
    phoneNumber: "",
    password: "",
    passwordConfirm: "",
  });
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  const handleChange = (field: keyof RegisterRequest, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (formErrors[field]) {
      setFormErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    const firstNameError = validateName(formData.firstName, "First name");
    if (firstNameError) errors.firstName = firstNameError;
    const lastNameError = validateName(formData.lastName, "Last name");
    if (lastNameError) errors.lastName = lastNameError;
    if (!formData.email) errors.email = "Email is required";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
      errors.email = "Invalid email";
    const phoneError = validatePhoneNumber(formData.phoneNumber);
    if (phoneError) errors.phoneNumber = phoneError;
    const passwordError = validatePassword(formData.password);
    if (passwordError) errors.password = passwordError;
    if (formData.password !== formData.passwordConfirm)
      errors.passwordConfirm = "Passwords do not match";

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      const result = await register(formData);
      if (result) {
        setShowSuccessModal(true);
      }
    } catch (err) {
      if (isApiErrorException(err) && err.isValidationError()) {
        setFormErrors(err.getValidationErrors());
      }
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
    navigate("/login");
  };

  return (
    <>
      <AuthCard title="Create an Account" wide>
        <div className={styles.registrationTop}>
          <span>Already have an account?</span>
          <Link to="/login">Login</Link>
        </div>

        <form className={styles.registrationForm} onSubmit={handleSubmit}>
          {error && (
            <div className={styles.notification} data-type="error">
              {error.message}
            </div>
          )}

          <div className={styles.formSection}>
            <h2 className={styles.sectionTitle}>Personal Information</h2>
            <div className={styles.inputGroup}>
              <Input
                placeholder="First Name"
                value={formData.firstName}
                onChange={(v) => handleChange("firstName", v)}
                disabled={loading}
                error={formErrors.firstName}
              />
              <Input
                placeholder="Last Name"
                value={formData.lastName}
                onChange={(v) => handleChange("lastName", v)}
                disabled={loading}
                error={formErrors.lastName}
              />
            </div>
            <div className={styles.inputGroup}>
              <Input
                type="date"
                placeholder="Date of Birth"
                value={formData.dateOfBirth}
                onChange={(v) => handleChange("dateOfBirth", v)}
                disabled={loading}
                error={formErrors.dateOfBirth}
              />
              <Input
                placeholder="Your City"
                value={formData.city}
                onChange={(v) => handleChange("city", v)}
                disabled={loading}
                error={formErrors.city}
              />
            </div>
          </div>

          <div className={styles.formSection}>
            <h2 className={styles.sectionTitle}>Contact Information</h2>
            <Input
              type="email"
              placeholder="E-mail"
              value={formData.email}
              onChange={(v) => handleChange("email", v)}
              disabled={loading}
              error={formErrors.email}
            />
            <Input
              placeholder="Phone number"
              value={formData.phoneNumber}
              onChange={(v) => handleChange("phoneNumber", v)}
              disabled={loading}
              error={formErrors.phoneNumber}
            />
          </div>

          <div className={styles.formSection}>
            <h2 className={styles.sectionTitle}>Create a Password</h2>
            <Input
              type="password"
              placeholder="Enter Password"
              value={formData.password}
              onChange={(v) => handleChange("password", v)}
              disabled={loading}
              error={formErrors.password}
            />
            <Input
              type="password"
              placeholder="Confirm Password"
              value={formData.passwordConfirm}
              onChange={(v) => handleChange("passwordConfirm", v)}
              disabled={loading}
              error={formErrors.passwordConfirm}
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={loading}
            disabled={loading}
            fullWidth
            style={{ marginTop: "1rem" }}
          >
            {loading ? "Creating account..." : "Sign Up"}
          </Button>
        </form>
      </AuthCard>

      <RegistrationSuccessModal
        isOpen={showSuccessModal}
        onClose={handleModalClose}
        email={formData.email}
      />
    </>
  );
};
