import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import './RegisterForm.css';

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
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="success-animation">
          <div className="checkmark">✓</div>
        </div>
        <h3>Registration Successful! 🎉</h3>
        <p>We've sent a confirmation email to <strong>{email}</strong></p>
        <div className="modal-actions">
          <button className="btn-primary" onClick={onClose}>
            Continue to Login
          </button>
        </div>
        <p className="help-text">Didn't receive the email? Check your spam folder</p>
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
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const { register } = useAuth();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    if (formData.password !== formData.passwordConfirm) {
      setError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    try {
      await register(formData);
      setShowSuccessModal(true);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
    window.location.href = '/login';
  };

  return (
    <section className="registration">
      <div className="registration-container">
        <h1 className="registration-title">Registration</h1>
        <form className="registration-form" onSubmit={handleSubmit}>
          {error && <div className="error-message">{error}</div>}

          <div className="registration-top">
            <h2 className="registration-text">Personal Information</h2>
            <div className="registration-group-first">
              <input
                placeholder="First Name"
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                className="form-control form-left"
                required
              />
              <input
                placeholder="Last Name"
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                className="form-control form-right"
                required
              />
            </div>
            <div className="registration-group-second">
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className="form-control form-left"
                required
              />
              <input
                placeholder="Your City"
                type="text"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className="form-control form right"
                required
              />
            </div>
          </div>

          <div className="registration-middle">
            <h2 className="registration-text">Contact Information</h2>
            <input
              placeholder="E-mail"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              className="form-control"
              required
            />
            <input
              placeholder="Phone number"
              type="text"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>

          <div className="registration-bottom">
            <h2 className="registration-text">Create a Password</h2>
            <input
              placeholder="Enter Password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="form-control"
              required
            />
            <input
              placeholder="Confirm Password"
              type="password"
              name="passwordConfirm"
              value={formData.passwordConfirm}
              onChange={handleChange}
              className="form-control"
              required
            />
          </div>

          <button
            type="submit"
            className="registration-confirm registration-text"
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