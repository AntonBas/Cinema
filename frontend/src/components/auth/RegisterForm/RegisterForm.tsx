import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/hooks/features/auth/useAuth';
import { Input, Button, Modal } from '@/components/ui';
import type { RegisterRequest } from '@/types/auth';
import styles from './RegisterForm.module.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

const RegistrationSuccessModal: React.FC<SuccessModalProps> = ({
  isOpen,
  onClose,
  email
}) => {
  return (
    <Modal isOpen={isOpen} onClose={onClose} size="small">
      <div className={styles.successContent}>
        <div className={styles.successAnimation}>
          <div className={styles.successIcon}>🎉</div>
        </div>

        <div className={styles.successText}>
          <h3 className={styles.successTitle}>Registration Successful!</h3>
          <p className={styles.successMessage}>
            We've sent a confirmation email to
          </p>
          <p className={styles.emailHighlight}>{email}</p>
        </div>

        <div className={styles.modalActions}>
          <Button
            variant="primary"
            onClick={onClose}
            style={{ minWidth: '200px' }}
          >
            Continue to Login
          </Button>
        </div>

        <p className={styles.helpText}>
          Didn't receive the email? Check your spam folder
        </p>
      </div>
    </Modal>
  );
};

export const RegisterForm: React.FC = () => {
  const [formData, setFormData] = useState<RegisterRequest>({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    city: '',
    email: '',
    phoneNumber: '',
    password: '',
    passwordConfirm: ''
  });
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [localError, setLocalError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register, loading } = useAuth();
  const navigate = useNavigate();

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    if (formErrors[field]) {
      setFormErrors(prev => ({ ...prev, [field]: '' }));
    }
    if (localError) setLocalError(null);
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!formData.firstName) {
      errors.firstName = 'First name is required';
    }

    if (!formData.lastName) {
      errors.lastName = 'Last name is required';
    }

    if (!formData.email) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Please enter a valid email address';
    }

    if (!formData.password) {
      errors.password = 'Password is required';
    } else if (formData.password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }

    if (formData.password !== formData.passwordConfirm) {
      errors.passwordConfirm = 'Passwords do not match';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      await register(formData);
      setShowSuccessModal(true);
    } catch (err: any) {
      setLocalError(err.message || 'Registration failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
    navigate('/login');
  };

  const isLoading = isSubmitting || loading;

  return (
    <section className={styles.registration}>
      <div className={styles.registrationContainer}>
        <h1 className={styles.registrationTitle}>Registration</h1>
        <form className={styles.registrationForm} onSubmit={handleSubmit}>
          {localError && (
            <div className={styles.notification} data-type="error">
              {localError}
            </div>
          )}

          <div className={styles.registrationTop}>
            <h2 className={styles.registrationText}>Personal Information</h2>
            <div className={styles.registrationGroup}>
              <Input
                placeholder="First Name"
                value={formData.firstName}
                onChange={(value) => handleChange('firstName', value)}
                disabled={isLoading}
                error={formErrors.firstName}
              />
              <Input
                placeholder="Last Name"
                value={formData.lastName}
                onChange={(value) => handleChange('lastName', value)}
                disabled={isLoading}
                error={formErrors.lastName}
              />
            </div>
            <div className={styles.registrationGroup}>
              <Input
                type="date"
                placeholder="Date of Birth"
                value={formData.dateOfBirth}
                onChange={(value) => handleChange('dateOfBirth', value)}
                disabled={isLoading}
                error={formErrors.dateOfBirth}
              />
              <Input
                placeholder="Your City"
                value={formData.city}
                onChange={(value) => handleChange('city', value)}
                disabled={isLoading}
                error={formErrors.city}
              />
            </div>
          </div>

          <div className={styles.registrationMiddle}>
            <h2 className={styles.registrationText}>Contact Information</h2>
            <Input
              type="email"
              placeholder="E-mail"
              value={formData.email}
              onChange={(value) => handleChange('email', value)}
              disabled={isLoading}
              error={formErrors.email}
            />
            <Input
              placeholder="Phone number"
              value={formData.phoneNumber}
              onChange={(value) => handleChange('phoneNumber', value)}
              disabled={isLoading}
              error={formErrors.phoneNumber}
            />
          </div>

          <div className={styles.registrationBottom}>
            <h2 className={styles.registrationText}>Create a Password</h2>
            <Input
              type="password"
              placeholder="Enter Password"
              value={formData.password}
              onChange={(value) => handleChange('password', value)}
              disabled={isLoading}
              error={formErrors.password}
            />
            <Input
              type="password"
              placeholder="Confirm Password"
              value={formData.passwordConfirm}
              onChange={(value) => handleChange('passwordConfirm', value)}
              disabled={isLoading}
              error={formErrors.passwordConfirm}
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            size="large"
            loading={isLoading}
            disabled={isLoading}
            style={{ width: '100%', marginTop: '1rem' }}
          >
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </Button>
        </form>
      </div>

      <RegistrationSuccessModal
        isOpen={showSuccessModal}
        onClose={handleModalClose}
        email={formData.email}
      />
    </section>
  );
};