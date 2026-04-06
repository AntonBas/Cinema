import React, { useState, useEffect, useRef } from 'react';
import { useParams, useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useAuthActions } from '@/hooks/features/auth/useAuthActions';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
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
    const { isAuthenticated } = useAuth();
    const { confirmEmailChange } = useAuthActions();

    const confirmationToken = token || searchParams.get('token');
    const redirectPath = isAuthenticated ? '/' : '/login';
    const redirectText = isAuthenticated
        ? 'Redirecting to home page in 5 seconds...'
        : 'Redirecting to login page in 5 seconds...';
    const buttonText = isAuthenticated ? 'Go to Home Page' : 'Go to Login';

    useEffect(() => {
        if (hasConfirmed.current || !confirmationToken) {
            return;
        }

        const confirmEmailChangeHandler = async () => {
            hasConfirmed.current = true;

            try {
                await confirmEmailChange(confirmationToken);
                setIsSuccess(true);

                setTimeout(() => {
                    navigate(redirectPath);
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

        confirmEmailChangeHandler();
    }, [confirmationToken, confirmEmailChange, navigate, redirectPath]);

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
                        <p className={styles.redirectText}>{redirectText}</p>
                        <Button
                            variant="primary"
                            onClick={() => navigate(redirectPath)}
                        >
                            {buttonText} Now
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