import React, { useState } from 'react';
import { Link } from 'react-router-dom';

export const ForgotPasswordForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    setMessage('');

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
        setMessage(data);
      } else {
        setError(data);
      }
    } catch (err) {
      setError('An error occurred while sending the request');
    } finally {
      setIsLoading(false);
    }
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
        Password Recovery
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
          <label htmlFor="email" style={{
            display: 'block',
            marginBottom: '0.5rem',
            fontWeight: '500',
            color: '#fff'
          }}>
            Email
          </label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            placeholder="Enter your email"
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
          {isLoading ? 'Sending...' : 'Send Instructions'}
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