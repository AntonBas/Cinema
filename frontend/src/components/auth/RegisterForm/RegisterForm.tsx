import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthMutation } from '@/hooks/features/auth';
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
  if (!isOpen) return null;

  return (
    <div className={styles.modalOverlay}>
      <div className={styles.modalContent}>
        <div className={styles.successAnimation}>
          <div className={styles.checkmark}>✓</div>
        </div>
        <h3>Registration Successful! 🎉</h3>
        <p>We've sent a confirmation email to <strong>{email}</strong></p>
        <div className={styles.modalActions}>
          <button className={styles.btnPrimary} onClick={onClose}>
            Continue to Login
          </button>
        </div>
        <p className={styles.helpText}>Didn't receive the email? Check your spam folder</p>
      </div>
    </div>
  );
};

export const RegisterForm: React.FC = () => {
  const [formData, setFormData] = useState({
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
  const { register, isLoading, error } = useAuthMutation();
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.password !== formData.passwordConfirm) {
      return;
    }

    try {
      await register(formData);
      setShowSuccessModal(true);
    } catch (err) {
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
          {error && <div className={styles.errorMessage}>{error}</div>}

          <div className={styles.registrationTop}>
            <h2 className={styles.registrationText}>Personal Information</h2>
            <div className={styles.registrationGroupFirst}>
              <input
                placeholder="First Name"
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className={`${styles.formControl} ${styles.formLeft}`}
                required
                disabled={isLoading}
              />
              <input
                placeholder="Last Name"
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className={`${styles.formControl} ${styles.formRight}`}
                required
                disabled={isLoading}
              />
            </div>
            <div className={styles.registrationGroupSecond}>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className={`${styles.formControl} ${styles.formLeft}`}
                required
                disabled={isLoading}
              />
              <input
                placeholder="Your City"
                type="text"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className={`${styles.formControl} ${styles.formRight}`}
                required
                disabled={isLoading}
              />
            </div>
          </div>

          <div className={styles.registrationMiddle}>
            <h2 className={styles.registrationText}>Contact Information</h2>
            <input
              placeholder="E-mail"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className={styles.formControl}
              required
              disabled={isLoading}
            />
            <input
              placeholder="Phone number"
              type="text"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className={styles.formControl}
              required
              disabled={isLoading}
            />
          </div>

          <div className={styles.registrationBottom}>
            <h2 className={styles.registrationText}>Create a Password</h2>
            <input
              placeholder="Enter Password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className={styles.formControl}
              required
              disabled={isLoading}
            />
            <input
              placeholder="Confirm Password"
              type="password"
              name="passwordConfirm"
              value={formData.passwordConfirm}
              onChange={handleChange}
              className={styles.formControl}
              required
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            className={`${styles.registrationConfirm} ${styles.registrationText}`}
            disabled={isLoading}
          >
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </button>
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