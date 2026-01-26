import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { usePaymentResult } from '@/hooks/features/payment/usePaymentResult';
import { Button } from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SuccessPage.module.css';

const SuccessPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const {
        loading,
        error,
        paymentData,
        fetchPaymentData,
        getResultMessage,
        getResultType,
        getResultIcon,
        getPaymentDetails,
        hasPaymentData } = usePaymentResult();

    const [isPolling, setIsPolling] = useState(false);
    const [pollingCount, setPollingCount] = useState(0);
    const pollingRef = useRef<NodeJS.Timeout | null>(null);
    const isInitialMount = useRef(true);

    const bookingId = searchParams.get('bookingId');
    const paymentId = searchParams.get('paymentId');

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
            fetchPaymentData(bookingId || undefined, paymentId || undefined);
        }, 5000);

        return () => {
            if (pollingRef.current) {
                clearInterval(pollingRef.current);
                pollingRef.current = null;
            }
            setIsPolling(false);
        };
    }, [paymentId, isPolling, bookingId, fetchPaymentData]);

    const stopPolling = useCallback(() => {
        if (pollingRef.current) {
            clearInterval(pollingRef.current);
            pollingRef.current = null;
        }
        setIsPolling(false);
    }, []);

    useEffect(() => {
        if (!bookingId && !paymentId) {
            navigate('/booking');
        }
    }, [bookingId, paymentId, navigate]);

    useEffect(() => {
        if (isInitialMount.current) {
            isInitialMount.current = false;
            fetchPaymentData(bookingId || undefined, paymentId || undefined);
        }
    }, [bookingId, paymentId, fetchPaymentData]);

    useEffect(() => {
        if (paymentData) {
            const status = paymentData.status;
            if (status === 'PENDING' || status === 'PROCESSING') {
                startPolling();
            } else if (status === 'SUCCESS' || status === 'FAILED' || status === 'CANCELLED') {
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
        if (paymentData?.bookingId) {
            navigate(`/booking/payment/${paymentData.bookingId}`);
        }
    };

    const handleGoHome = () => {
        navigate('/');
    };

    const handleContactSupport = () => {
        navigate('/support');
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('uk-UA', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const formatTime = (dateString: string) => {
        return new Date(dateString).toLocaleTimeString('uk-UA', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    if (loading && !hasPaymentData && !paymentData) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading payment information..." />
            </div>
        );
    }

    const resultType = getResultType();
    const resultIcon = getResultIcon();
    const resultMessage = getResultMessage();
    const paymentDetails = getPaymentDetails();

    const getResultStyles = () => {
        switch (resultType) {
            case 'success':
                return {
                    title: 'Payment Successful!',
                    iconClass: styles.successIcon,
                    messageClass: styles.successMessage
                };
            case 'error':
                return {
                    title: 'Payment Failed',
                    iconClass: styles.errorIcon,
                    messageClass: styles.errorMessage
                };
            case 'warning':
                return {
                    title: 'Payment Processing',
                    iconClass: styles.warningIcon,
                    messageClass: styles.warningMessage
                };
            default:
                return {
                    title: 'Payment Status',
                    iconClass: styles.infoIcon,
                    messageClass: styles.infoMessage
                };
        }
    };

    const resultStyles = getResultStyles();

    return (
        <div className={styles.container}>
            <div className={styles.contentWrapper}>
                <div className={styles.card}>
                    {error && (
                        <div className={styles.errorAlert}>
                            <div className={styles.errorAlertContent}>
                                <svg className={styles.errorIcon} fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 101.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                </svg>
                                <p className={styles.errorText}>{error}</p>
                            </div>
                        </div>
                    )}

                    <div className={styles.resultHeader}>
                        <div className={`${styles.iconWrapper} ${resultStyles.iconClass}`}>
                            <span className={styles.resultIcon}>{resultIcon}</span>
                        </div>

                        <h1 className={styles.title}>{resultStyles.title}</h1>

                        <p className={`${styles.resultMessage} ${resultStyles.messageClass}`}>
                            {resultMessage}
                            {isPolling && ` (Checking... ${pollingCount})`}
                        </p>
                    </div>

                    {paymentDetails && (
                        <div className={styles.detailsCard}>
                            <h3 className={styles.detailsTitle}>Booking Details</h3>
                            <div className={styles.detailsGrid}>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Booking Number</p>
                                    <p className={styles.detailValue}>{paymentDetails.bookingNumber}</p>
                                </div>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Movie</p>
                                    <p className={styles.detailValue}>{paymentDetails.movieTitle}</p>
                                </div>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Hall</p>
                                    <p className={styles.detailValue}>{paymentDetails.hallName}</p>
                                </div>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Session Time</p>
                                    <p className={styles.detailValue}>
                                        {formatDate(paymentDetails.sessionTime)}
                                    </p>
                                </div>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Amount</p>
                                    <p className={styles.detailPrice}>{paymentDetails.finalAmount} UAH</p>
                                </div>
                                {paymentDetails.paymentTime && (
                                    <div className={styles.detailItem}>
                                        <p className={styles.detailLabel}>Payment Time</p>
                                        <p className={styles.detailValue}>
                                            {formatTime(paymentDetails.paymentTime)}
                                        </p>
                                    </div>
                                )}
                                {paymentDetails.senderCardMask && (
                                    <div className={styles.detailItem}>
                                        <p className={styles.detailLabel}>Card</p>
                                        <p className={styles.detailValue}>{paymentDetails.senderCardMask}</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    <div className={styles.actions}>
                        {resultType === 'success' && (
                            <>
                                <Button
                                    variant="primary"
                                    onClick={handleViewTickets}
                                    className={styles.actionButton}
                                >
                                    View My Tickets
                                </Button>
                                <p className={styles.successNote}>
                                    Your tickets have been sent to your email address.
                                    You can also view them in your tickets section.
                                </p>
                            </>
                        )}

                        {resultType === 'error' && paymentData?.bookingId && (
                            <Button
                                variant="primary"
                                onClick={handleTryAgain}
                                className={styles.actionButton}
                            >
                                Try Again
                            </Button>
                        )}

                        {resultType === 'error' && (
                            <Button
                                variant="secondary"
                                onClick={handleContactSupport}
                                className={styles.actionButton}
                            >
                                Contact Support
                            </Button>
                        )}

                        <Button
                            variant="cancel"
                            onClick={handleGoHome}
                            className={styles.actionButton}
                        >
                            Back to Home
                        </Button>
                    </div>

                    {resultType === 'success' && (
                        <div className={styles.footer}>
                            <div className={styles.footerContent}>
                                <svg className={styles.successCheckIcon} fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                </svg>
                                <span className={styles.footerText}>
                                    Thank you for your purchase! Enjoy the movie!
                                </span>
                            </div>
                        </div>
                    )}
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
};

export default SuccessPage;