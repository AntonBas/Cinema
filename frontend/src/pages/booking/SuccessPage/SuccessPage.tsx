import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { CheckCircle2, Home, Ticket } from 'lucide-react';
import type { PaymentStatus, PaymentResponse } from '@/types/payment';
import styles from './SuccessPage.module.css';

const SuccessPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const {
        getById,
        loading
    } = usePayment();

    const [isPolling, setIsPolling] = useState(false);
    const [pollingCount, setPollingCount] = useState(0);
    const [isVisible, setIsVisible] = useState(false);
    const [paymentData, setPaymentData] = useState<PaymentResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const isInitialMount = useRef(true);

    const bookingId = searchParams.get('bookingId');
    const paymentId = searchParams.get('paymentId');

    const fetchPaymentData = useCallback(async (id: string) => {
        try {
            const response = await getById(parseInt(id));
            const data = response || null;
            setPaymentData(data);
            setError(null);
            return data;
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load payment data');
            return null;
        }
    }, [getById]);

    const getResultType = () => {
        if (!paymentData) return 'info';
        const status = paymentData.status;
        if (status === 'SUCCESS') return 'success';
        if (['FAILED', 'CANCELLED', 'EXPIRED'].includes(status)) return 'error';
        if (['PENDING', 'PROCESSING'].includes(status)) return 'warning';
        return 'info';
    };

    const getResultMessage = () => {
        if (!paymentData) return 'Loading payment information...';
        const status = paymentData.status as PaymentStatus;

        if (status === 'PENDING' || status === 'PROCESSING') {
            return 'Your payment is being processed. Please wait...';
        }
        if (status === 'SUCCESS') {
            return 'Your tickets have been booked.';
        }
        if (status === 'FAILED' || status === 'CANCELLED' || status === 'EXPIRED') {
            return 'Payment failed. Please try again or contact support.';
        }

        return 'Payment status unknown.';
    };

    const startPolling = useCallback(() => {
        if (!paymentId || isPolling || pollingRef.current) return;

        setIsPolling(true);
        pollingRef.current = setInterval(() => {
            setPollingCount(prev => {
                if (prev >= 30) {
                    if (pollingRef.current) {
                        clearInterval(pollingRef.current);
                        pollingRef.current = null;
                    }
                    setIsPolling(false);
                    return prev;
                }
                return prev + 1;
            });
            fetchPaymentData(paymentId);
        }, 5000);
    }, [paymentId, isPolling, fetchPaymentData]);

    const stopPolling = useCallback(() => {
        if (pollingRef.current) {
            clearInterval(pollingRef.current);
            pollingRef.current = null;
        }
        setIsPolling(false);
    }, []);

    useEffect(() => {
        setIsVisible(true);
        const paramsValid = bookingId || paymentId;
        if (!paramsValid) {
            navigate('/booking');
        }
    }, [bookingId, paymentId, navigate]);

    useEffect(() => {
        if (isInitialMount.current) {
            isInitialMount.current = false;
            const idToFetch = paymentId || bookingId;
            if (idToFetch) {
                fetchPaymentData(idToFetch);
            }
        }
    }, [bookingId, paymentId, fetchPaymentData]);

    useEffect(() => {
        if (paymentData) {
            const status = paymentData.status;
            if (status === 'PENDING' || status === 'PROCESSING') {
                startPolling();
            } else if (['SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED'].includes(status)) {
                stopPolling();
            }
        }
    }, [paymentData, startPolling, stopPolling]);

    useEffect(() => {
        return () => {
            stopPolling();
        };
    }, [stopPolling]);

    const handleViewTickets = () => {
        navigate('/account/tickets');
    };

    const handleTryAgain = () => {
        if (bookingId) {
            navigate(`/booking/payment/${bookingId}`);
        }
    };

    const handleGoHome = () => {
        navigate('/');
    };

    const handleContactSupport = () => {
        navigate('/support');
    };

    if ((loading && !paymentData) || !isVisible) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading payment information..." />
            </div>
        );
    }

    const resultType = getResultType();
    const resultMessage = getResultMessage();

    if (resultType === 'success') {
        return (
            <div className={`${styles.container} ${isVisible ? styles.visible : ''}`}>
                <div className={styles.contentWrapper}>
                    <div className={styles.card}>
                        <div className={styles.successContainer}>
                            <div className={styles.iconWrapper}>
                                <CheckCircle2 className={styles.successIcon} size={64} />
                            </div>

                            <h1 className={styles.title}>Payment Successful!</h1>

                            <p className={styles.message}>{resultMessage}</p>

                            <div className={styles.actions}>
                                <Button
                                    variant="primary"
                                    onClick={handleViewTickets}
                                    className={styles.actionButton}
                                >
                                    <Ticket size={18} />
                                    View My Tickets
                                </Button>
                                <Button
                                    variant="secondary"
                                    onClick={handleGoHome}
                                    className={styles.actionButton}
                                >
                                    <Home size={18} />
                                    Back to Home
                                </Button>
                            </div>
                        </div>

                        <div className={styles.footer}>
                            <div className={styles.footerContent}>
                                <span className={styles.footerText}>
                                    Thank you for your purchase! Enjoy the movie!
                                </span>
                            </div>
                        </div>
                    </div>

                    {isPolling && (
                        <div className={styles.statusCheck}>
                            <div className={styles.statusCheckContent}>
                                <LoadingSpinner text="" />
                                <span className={styles.statusCheckText}>
                                    Checking payment status automatically... {pollingCount}/30
                                </span>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    }

    if (resultType === 'error') {
        return (
            <div className={`${styles.container} ${isVisible ? styles.visible : ''}`}>
                <div className={styles.contentWrapper}>
                    <div className={styles.card}>
                        <div className={styles.errorContainer}>
                            <div className={`${styles.iconWrapper} ${styles.errorIconWrapper}`}>
                                <span className={styles.resultIcon}>✗</span>
                            </div>

                            <h1 className={styles.title}>Payment Failed</h1>

                            <p className={styles.errorMessage}>{resultMessage}</p>

                            {error && (
                                <div className={styles.errorAlert}>
                                    <p className={styles.errorText}>{error}</p>
                                </div>
                            )}

                            <div className={styles.actions}>
                                {bookingId && (
                                    <Button
                                        variant="primary"
                                        onClick={handleTryAgain}
                                        className={styles.actionButton}
                                    >
                                        Try Again
                                    </Button>
                                )}
                                <Button
                                    variant="secondary"
                                    onClick={handleContactSupport}
                                    className={styles.actionButton}
                                >
                                    Contact Support
                                </Button>
                                <Button
                                    variant="cancel"
                                    onClick={handleGoHome}
                                    className={styles.actionButton}
                                >
                                    Back to Home
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (resultType === 'warning') {
        return (
            <div className={`${styles.container} ${isVisible ? styles.visible : ''}`}>
                <div className={styles.contentWrapper}>
                    <div className={styles.card}>
                        <div className={styles.warningContainer}>
                            <div className={`${styles.iconWrapper} ${styles.warningIconWrapper}`}>
                                <span className={styles.resultIcon}>⏳</span>
                            </div>

                            <h1 className={styles.title}>Payment Processing</h1>

                            <p className={styles.warningMessage}>{resultMessage}</p>

                            {isPolling && (
                                <div className={styles.pollingInfo}>
                                    <LoadingSpinner text="" />
                                    <span>Checking payment status... {pollingCount}/30</span>
                                </div>
                            )}

                            <div className={styles.actions}>
                                <Button
                                    variant="secondary"
                                    onClick={handleGoHome}
                                    className={styles.actionButton}
                                >
                                    Back to Home
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.loadingContainer}>
            <LoadingSpinner text="Loading payment information..." />
        </div>
    );
};

export default SuccessPage;