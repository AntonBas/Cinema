import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Input, Button, Modal } from '@/components/ui';
import type { RegisterRequest } from '@/types/auth';
import styles from './RegisterForm.module.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

const RegistrationSuccessModal: React.FC<SuccessModalProps> = ({ isOpen, onClose, email }) => (
  <Modal isOpen={isOpen} onClose={onClose} size="small">
    <div className={styles.successContent}>
      <div className={styles.successIcon}>🎉</div>
      <h3>Registration Successful!</h3>
      <p>We've sent a confirmation email to</p>
      <p className={styles.emailHighlight}>{email}</p>
      <Button variant="primary" onClick={onClose}>
        Continue to Login
      </Button>
      <p className={styles.helpText}>Check your spam folder if you don't see the email</p>
    </div>
  </Modal>
);

export const RegisterForm: React.FC = () => {
  const navigate = useNavigate();
  const { loading, error, register } = useAuthActions();

  const [formData, setFormData] = useState<RegisterRequest>({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    city: '',
    email: '',
    phoneNumber: '',
    password: '',
    passwordConfirm: '',
  });
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  const handleChange = (field: keyof RegisterRequest, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (formErrors[field]) {
      setFormErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!formData.firstName) errors.firstName = 'First name is required';
    if (!formData.lastName) errors.lastName = 'Last name is required';
    if (!formData.email) errors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) errors.email = 'Invalid email';
    if (!formData.password) errors.password = 'Password is required';
    else if (formData.password.length < 8) errors.password = 'Password must be at least 8 characters';
    if (formData.password !== formData.passwordConfirm) errors.passwordConfirm = 'Passwords do not match';

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    const result = await register(formData);
    if (result) {
      setShowSuccessModal(true);
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
    navigate('/login');
  };

  return (
    <section className={styles.registration}>
      <div className={styles.registrationContainer}>
        <h1 className={styles.registrationTitle}>Registration</h1>

        <form className={styles.registrationForm} onSubmit={handleSubmit}>
          {error && (
            <div className={styles.error}>{error.message}</div>
          )}

          <h2>Personal Information</h2>
          <div className={styles.registrationGroup}>
            <Input
              placeholder="First Name"
              value={formData.firstName}
              onChange={v => handleChange('firstName', v)}
              disabled={loading}
              error={formErrors.firstName}
            />
            <Input
              placeholder="Last Name"
              value={formData.lastName}
              onChange={v => handleChange('lastName', v)}
              disabled={loading}
              error={formErrors.lastName}
            />
          </div>
          <div className={styles.registrationGroup}>
            <Input
              type="date"
              placeholder="Date of Birth"
              value={formData.dateOfBirth}
              onChange={v => handleChange('dateOfBirth', v)}
              disabled={loading}
            />
            <Input
              placeholder="Your City"
              value={formData.city}
              onChange={v => handleChange('city', v)}
              disabled={loading}
            />
          </div>

          <h2>Contact Information</h2>
          <Input
            type="email"
            placeholder="E-mail"
            value={formData.email}
            onChange={v => handleChange('email', v)}
            disabled={loading}
            error={formErrors.email}
          />
          <Input
            placeholder="Phone number"
            value={formData.phoneNumber}
            onChange={v => handleChange('phoneNumber', v)}
            disabled={loading}
          />

          <h2>Create a Password</h2>
          <Input
            type="password"
            placeholder="Enter Password"
            value={formData.password}
            onChange={v => handleChange('password', v)}
            disabled={loading}
            error={formErrors.password}
          />
          <Input
            type="password"
            placeholder="Confirm Password"
            value={formData.passwordConfirm}
            onChange={v => handleChange('passwordConfirm', v)}
            disabled={loading}
            error={formErrors.passwordConfirm}
          />

          <Button type="submit" variant="primary" loading={loading} disabled={loading}>
            Sign Up
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