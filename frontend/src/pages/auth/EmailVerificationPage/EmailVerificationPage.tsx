import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './EmailVerificationPage.module.css';

export const EmailVerificationPage: React.FC = () => {
  const { token } = useParams<{ token: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');

  const verificationToken = token || searchParams.get('token');

  useEffect(() => {
    if (!verificationToken) {
      setStatus('error');
      return;
    }

    const timer = setTimeout(() => {
      setStatus('success');
      setTimeout(() => navigate('/login'), 5000);
    }, 1500);

    return () => clearTimeout(timer);
  }, [verificationToken, navigate]);

  if (status === 'loading') {
    return (
      <div className={styles.verificationContainer}>
        <LoadingSpinner text="Verifying your email..." />
      </div>
    );
  }

  if (status === 'error') {
    return (
      <div className={styles.verificationContainer}>
        <div className={`${styles.verificationCard} ${styles.error}`}>
          <div className={styles.icon}>❌</div>
          <h2>Verification Failed</h2>
          <p>Invalid or expired verification token.</p>
          <Button variant="secondary" onClick={() => navigate('/login')}>
            Go to Login
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.verificationContainer}>
      <div className={`${styles.verificationCard} ${styles.success}`}>
        <div className={styles.icon}>✅</div>
        <h2>Email Verified Successfully!</h2>
        <p>Your email has been verified.</p>
        <p className={styles.redirectText}>Redirecting to login page in 5 seconds...</p>
        <Button variant="primary" onClick={() => navigate('/login')}>
          Go to Login Now
        </Button>
      </div>
    </div>
  );
};