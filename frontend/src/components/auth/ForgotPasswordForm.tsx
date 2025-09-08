import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './ForgotPasswordForm.css';

interface SuccessModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
}

const PasswordResetModal: React.FC<SuccessModalProps> = ({
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
        <h3>Instructions Sent! 📧</h3>
        <p>We've sent password reset instructions to <strong>{email}</strong></p>
        <div className="modal-actions">
          <button className="btn-primary" onClick={onClose}>
            Got It
          </button>
        </div>
        <p className="help-text">Check your spam folder if you don't see the email</p>
      </div>
    </div>
  );
};

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [error, setError] = useState(''); // Видалили message
  const [isLoading, setIsLoading] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `email=${encodeURIComponent(email)}`
      });

      const data = await response.text();

      if (response.ok) {
        setShowSuccessModal(true);
      } else {
        setError(data);
      }
    } catch (err) {
      setError('An error occurred while sending the request');
    } finally {
      setIsLoading(false);
    }
  };

  const handleModalClose = () => {
    setShowSuccessModal(false);
  };

  return (
    <section className='forgot-password'>
      <div className="forgot-password-container">
        <h2 className="forgot-password-title">
          Password Recovery
        </h2>

        {error && (
          <div className="forgot-password-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="forgot-password-form">
          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="Enter your email"
              className="form-input"
              disabled={isLoading}
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="forgot-password-button"
          >
            {isLoading ? 'Sending...' : 'Send Instructions'}
          </button>

          <div className="forgot-password-links">
            <Link to="/login" className="back-link">
              Back to Login
            </Link>
          </div>
        </form>
      </div>

      <PasswordResetModal
        isOpen={showSuccessModal}
        onClose={handleModalClose}
        email={email}
      />
    </section>
  );
};