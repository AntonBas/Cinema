import React, { useState, useEffect } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './ConfirmEmailChangePage.module.css';

export const ConfirmEmailChangePage: React.FC = () => {
    const { token } = useParams<{ token: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();

    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');

    const confirmationToken = token || searchParams.get('token');

    useEffect(() => {
        if (!confirmationToken) {
            setStatus('error');
            return;
        }

        const timer = setTimeout(() => {
            setStatus('success');
            setTimeout(() => navigate(isAuthenticated ? '/' : '/login'), 5000);
        }, 1500);

        return () => clearTimeout(timer);
    }, [confirmationToken, navigate, isAuthenticated]);

    if (status === 'loading') {
        return (
            <div className={styles.container}>
                <LoadingSpinner text="Confirming your email change..." />
            </div>
        );
    }

    if (status === 'error') {
        return (
            <div className={styles.container}>
                <div className={`${styles.card} ${styles.error}`}>
                    <div className={styles.icon}>❌</div>
                    <h2>Confirmation Failed</h2>
                    <p>Invalid or expired confirmation token.</p>
                    <Button variant="secondary" onClick={() => navigate('/login')}>
                        Go to Login
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={`${styles.card} ${styles.success}`}>
                <div className={styles.icon}>✅</div>
                <h2>Email Changed Successfully!</h2>
                <p>Your email address has been successfully updated.</p>
                <Button variant="primary" onClick={() => navigate(isAuthenticated ? '/' : '/login')}>
                    {isAuthenticated ? 'Go to Home' : 'Go to Login'}
                </Button>
            </div>
        </div>
    );
};