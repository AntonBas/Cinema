import React, { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';

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
    <div style={{
      maxWidth: '400px',
      margin: '0 auto',
      padding: '2rem',
      backgroundColor: '#1a1f2d',
      borderRadius: '8px',
      boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
    }}>
      <h2 style={{ textAlign: 'center', marginBottom: '1.5rem', color: '#fff' }}>
        Set New Password
      </h2>
      
      {message && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: '#d4edda',
          color: '#155724',
          border: '1px solid #c3e6cb',
          borderRadius: '4px',
          marginBottom: '1rem'
        }}>
          {message}
        </div>
      )}
      
      {error && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: '#f8d7da',
          color: '#721c24',
          border: '1px solid #f5c6cb',
          borderRadius: '4px',
          marginBottom: '1rem'
        }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="newPassword" style={{
            display: 'block',
            marginBottom: '0.5rem',
            fontWeight: '500',
            color: '#fff'
          }}>
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
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: '#2d3748',
              border: '1px solid #4a5568',
              borderRadius: '4px',
              color: '#fff',
              fontSize: '1rem'
            }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="confirmPassword" style={{
            display: 'block',
            marginBottom: '0.5rem',
            fontWeight: '500',
            color: '#fff'
          }}>
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
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: '#2d3748',
              border: '1px solid #4a5568',
              borderRadius: '4px',
              color: '#fff',
              fontSize: '1rem'
            }}
          />
        </div>
        
        <button
          type="submit"
          disabled={isLoading}
          style={{
            width: '100%',
            padding: '0.75rem',
            backgroundColor: '#fd5f00',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            fontSize: '1rem',
            cursor: isLoading ? 'not-allowed' : 'pointer',
            opacity: isLoading ? 0.7 : 1,
            marginBottom: '1rem'
          }}
        >
          {isLoading ? 'Resetting...' : 'Reset Password'}
        </button>
        
        <div style={{ textAlign: 'center' }}>
          <Link to="/login" style={{
            color: '#fd5f00',
            textDecoration: 'none'
          }}>
            Back to Login
          </Link>
        </div>
      </form>
    </div>
  );
};