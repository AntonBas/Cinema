import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../../services/api';
import './EmailVerificationPage.css';

export const EmailVerificationPage: React.FC = () => {
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    const verifyEmail = async () => {
      if (!token) {
        setMessage('No verification token provided');
        setIsLoading(false);
        setIsSuccess(false);
        return;
      }

      try {
        const response = await api.get(`/auth/verify-email?token=${token}`);
        setMessage(response.data);
        setIsSuccess(true);

        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } catch (error: any) {
        setMessage(error.response?.data || 'Verification failed');
        setIsSuccess(false);
      } finally {
        setIsLoading(false);
      }
    };

    verifyEmail();
  }, [token, navigate]);

  if (isLoading) {
    return <div className="verification-container">Verifying email...</div>;
  }

  return (
    <div className="verification-container">
      <div className={`verification-message ${isSuccess ? 'success' : 'error'}`}>
        <h2>{isSuccess ? 'Email Verified!' : 'Verification Failed'}</h2>
        <p>{message}</p>
        {isSuccess && (
          <p>Redirecting to login page in 3 seconds...</p>
        )}
        {!isSuccess && (
          <button onClick={() => navigate('/login')} className="login-button">
            Go to Login
          </button>
        )}
      </div>
    </div>
  );
};