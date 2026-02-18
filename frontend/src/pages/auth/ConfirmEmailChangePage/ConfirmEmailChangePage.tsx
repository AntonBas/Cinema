import React, { useState, useEffect, useRef } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { authApi } from '@/api/authApi';
import { Button, LoadingSpinner } from '@/components/ui';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './ConfirmEmailChangePage.module.css';

export const ConfirmEmailChangePage: React.FC = () => {
    const [isLoading, setIsLoading] = useState(true);
    const [isSuccess, setIsSuccess] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const { token } = useParams<{ token: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const hasConfirmed = useRef(false);

    const confirmationToken = token || searchParams.get('token');

    useEffect(() => {
        if (hasConfirmed.current || !confirmationToken) {
            return;
        }

        const confirmEmailChange = async () => {
            hasConfirmed.current = true;

            try {
                await authApi.confirmEmailChange(confirmationToken);
                setIsSuccess(true);

                setTimeout(() => {
                    navigate('/login');
                }, 5000);
            } catch (error: any) {
                if (isApiErrorException(error)) {
                    setErrorMessage(error.message);
                } else {
                    setErrorMessage('Email change confirmation failed');
                }
                setIsSuccess(false);
            } finally {
                setIsLoading(false);
            }
        };

        confirmEmailChange();
    }, [confirmationToken, navigate]);

    if (isLoading) {
        return (
            <div className={styles.container}>
                <LoadingSpinner text="Confirming your email change..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={`${styles.card} ${isSuccess ? styles.success : styles.error}`}>
                <div className={styles.icon}>
                    {isSuccess ? '✅' : '❌'}
                </div>
                <h2>{isSuccess ? 'Email Changed Successfully!' : 'Confirmation Failed'}</h2>
                <p className={styles.message}>
                    {isSuccess
                        ? 'Your email address has been successfully updated.'
                        : errorMessage}
                </p>

                {isSuccess ? (
                    <div className={styles.successActions}>
                        <p className={styles.redirectText}>Redirecting to login page in 5 seconds...</p>
                        <Button
                            variant="primary"
                            onClick={() => navigate('/login')}
                        >
                            Go to Login Now
                        </Button>
                    </div>
                ) : (
                    <div className={styles.errorActions}>
                        <Button
                            variant="secondary"
                            onClick={() => navigate('/login')}
                        >
                            Go to Login
                        </Button>
                    </div>
                )}
            </div>
        </div>
    );
};