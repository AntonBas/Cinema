import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import './ResetPasswordForm.css';

export const ResetPasswordForm: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.newPassword !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.newPassword.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/reset-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `token=${token}&newPassword=${encodeURIComponent(formData.newPassword)}`
      });

      const data = await response.text();
      
      if (response.ok) {
        setMessage(data);
        setTimeout(() => navigate('/login'), 3000);
      } else {
        setError(data);
      }
    } catch (err) {
      setError('An error occurred while resetting password');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  return (
    <div className="reset-password-container">
      <h2 className="reset-password-title">
        Set New Password
      </h2>
      
      {message && (
        <div className="reset-password-message">
          {message}
        </div>
      )}
      
      {error && (
        <div className="reset-password-error">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="reset-password-form">
        <div className="form-group">
          <label htmlFor="newPassword" className="form-label">
            New Password
          </label>
          <input
            type="password"
            id="newPassword"
            name="newPassword"
            value={formData.newPassword}
            onChange={handleChange}
            required
            minLength={6}
            placeholder="Enter new password"
            className="form-input"
            disabled={isLoading}
          />
        </div>

        <div className="form-group">
          <label htmlFor="confirmPassword" className="form-label">
            Confirm Password
          </label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
            minLength={6}
            placeholder="Confirm new password"
            className="form-input"
            disabled={isLoading}
          />
        </div>
        
        <button
          type="submit"
          disabled={isLoading}
          className="reset-password-button"
        >
          {isLoading ? 'Resetting...' : 'Reset Password'}
        </button>
        
        <div className="reset-password-links">
          <Link to="/login" className="back-link">
            Back to Login
          </Link>
        </div>
      </form>
    </div>
  );
};