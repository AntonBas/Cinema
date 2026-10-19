import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuthActions } from "@/features/auth/hooks/useAuthActions";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import type { RegisterRequest } from "@/features/auth/types/auth";
import { isApiErrorException } from "@/utils/apiErrorHandler";
import {
  validateCity,
  validateDateOfBirth,
  validateEmail,
  validateName,
  validatePasswordConfirm,
  validatePhoneNumber,
  validatePassword,
} from "@/utils/formValidation";
import { useFocusFirstInvalidField } from "@/hooks/common/useFocusFirstInvalidField";
import { AuthCard } from "@/features/auth/components/AuthCard/AuthCard";
import { GoogleIcon } from "@/features/auth/components/GoogleIcon/GoogleIcon";
import styles from "./RegisterForm.module.css";

export const RegisterForm: React.FC = () => {
  const navigate = useNavigate();
  const { loading, error, register, loginWithGoogle } = useAuthActions();

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
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const { formRef, focusFirstInvalidField } = useFocusFirstInvalidField();

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
    const dateOfBirthError = validateDateOfBirth(formData.dateOfBirth);
    if (dateOfBirthError) errors.dateOfBirth = dateOfBirthError;
    const cityError = validateCity(formData.city);
    if (cityError) errors.city = cityError;
    const emailError = validateEmail(formData.email);
    if (emailError) errors.email = emailError;
    const phoneError = validatePhoneNumber(formData.phoneNumber);
    if (phoneError) errors.phoneNumber = phoneError;
    const passwordError = validatePassword(formData.password);
    if (passwordError) errors.password = passwordError;
    const passwordConfirmError = validatePasswordConfirm(
      formData.password,
      formData.passwordConfirm,
    );
    if (passwordConfirmError) errors.passwordConfirm = passwordConfirmError;

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.SyntheticEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      focusFirstInvalidField();
      return;
    }

    try {
      const result = await register(formData);
      if (result) {
        navigate("/check-email", { state: { email: formData.email } });
      }
    } catch (err) {
      if (isApiErrorException(err) && err.isValidationError()) {
        setFormErrors(err.getValidationErrors());
        focusFirstInvalidField();
      }
    }
  };

  const handleGoogleSignUp = (e: React.MouseEvent) => {
    e.preventDefault();
    loginWithGoogle();
  };

  return (
    <AuthCard title="Create an Account" wide>
      <div className={styles.registrationTop}>
        <span>Already have an account?</span>
        <Link to="/login">Login</Link>
      </div>

      <form
        ref={formRef}
        className={styles.registrationForm}
        onSubmit={handleSubmit}
        noValidate
      >
        {error && (
          <div className={styles.notification} data-type="error">
            {error.message}
          </div>
        )}

        <div className={styles.formSection}>
          <h2 className={styles.sectionTitle}>Personal Information</h2>
          <div className={styles.inputGroup}>
            <Input
              label="First name"
              placeholder="First Name"
              value={formData.firstName}
              onChange={(v) => handleChange("firstName", v)}
              disabled={loading}
              error={formErrors.firstName}
            />
            <Input
              label="Last name"
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
              label="Date of birth"
              placeholder="Date of Birth"
              value={formData.dateOfBirth}
              onChange={(v) => handleChange("dateOfBirth", v)}
              disabled={loading}
              error={formErrors.dateOfBirth}
            />
            <Input
              label="City"
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
            label="Email"
            placeholder="E-mail"
            value={formData.email}
            onChange={(v) => handleChange("email", v)}
            disabled={loading}
            error={formErrors.email}
          />
          <Input
            label="Phone number"
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
            label="Password"
            placeholder="Enter Password"
            value={formData.password}
            onChange={(v) => handleChange("password", v)}
            disabled={loading}
            error={formErrors.password}
          />
          <p className={styles.hint}>Use 8 to 32 characters.</p>
          <Input
            type="password"
            label="Confirm password"
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

      <div className={styles.divider}>
        <span className={styles.dividerText}>or</span>
      </div>

      <Button
        type="button"
        variant="outline"
        size="large"
        onClick={handleGoogleSignUp}
        disabled={loading}
        fullWidth
      >
        <GoogleIcon size={20} />
        Continue with Google
      </Button>
    </AuthCard>
  );
};
