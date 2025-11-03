import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/authApi';
import styles from './EmailVerificationPage.module.css';

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
        const response = await authApi.verifyEmail(token);
        setMessage(response.message);
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
    return <div className={styles.verificationContainer}>Verifying email...</div>;
  }

  return (
    <div className={styles.verificationContainer}>
      <div className={`${styles.verificationMessage} ${isSuccess ? styles.success : styles.error}`}>
        <h2>{isSuccess ? 'Email Verified!' : 'Verification Failed'}</h2>
        <p>{message}</p>
        {isSuccess && (
          <p>Redirecting to login page in 3 seconds...</p>
        )}
        {!isSuccess && (
          <button onClick={() => navigate('/login')} className={styles.loginButton}>
            Go to Login
          </button>
        )}
      </div>
    </div>
  );
};