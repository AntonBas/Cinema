import React, { useState, useEffect, useRef } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/authApi';
import { Button } from '@/components/ui';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './EmailVerificationPage.module.css';

export const EmailVerificationPage: React.FC = () => {
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const hasVerified = useRef(false);

  const verificationToken = token || searchParams.get('token');

  useEffect(() => {
    if (hasVerified.current || !verificationToken) {
      return;
    }

    const verifyEmail = async () => {
      hasVerified.current = true;

      try {
        const response = await authApi.verifyEmail(verificationToken);
        setMessage(response.message);
        setIsSuccess(true);

        setTimeout(() => {
          navigate('/login');
        }, 5000);
      } catch (error: any) {
        if (isApiErrorException(error)) {
          setMessage(error.message);
        } else {
          setMessage('Email verification failed');
        }
        setIsSuccess(false);
      } finally {
        setIsLoading(false);
      }
    };

    verifyEmail();
  }, [verificationToken, navigate]);

  if (isLoading) {
    return (
      <div className={styles.verificationContainer}>
        <LoadingSpinner text="Verifying your email..." />
      </div>
    );
  }

  return (
    <div className={styles.verificationContainer}>
      <div className={`${styles.verificationCard} ${isSuccess ? styles.success : styles.error}`}>
        <div className={styles.icon}>
          {isSuccess ? '✅' : '❌'}
        </div>
        <h2>{isSuccess ? 'Email Verified Successfully!' : 'Verification Failed'}</h2>
        <p className={styles.message}>{message}</p>

        {isSuccess ? (
          <div className={styles.successActions}>
            <p className={styles.redirectText}>Redirecting to login page in 5 seconds...</p>
            <Button
              variant="primary"
              onClick={() => navigate('/login')}
              style={{ marginTop: '1rem' }}
            >
              Go to Login Now
            </Button>
          </div>
        ) : (
          <div className={styles.errorActions}>
            <Button
              variant="secondary"
              onClick={() => navigate('/login')}
              style={{ marginTop: '1rem' }}
            >
              Go to Login
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};