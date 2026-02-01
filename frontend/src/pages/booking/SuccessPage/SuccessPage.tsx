import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { ProgressStepper } from '@/components/booking/ProgressStepper';
import { Button } from '@/components/ui/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { PaymentStatus, PaymentResponse } from '@/types/payment';
import styles from './SuccessPage.module.css';

const PaymentStatusDisplay: Record<PaymentStatus, string> = {
    PENDING: 'Pending',
    PROCESSING: 'Processing',
    SUCCESS: 'Success',
    FAILED: 'Failed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    REFUNDED: 'Refunded',
    PARTIALLY_REFUNDED: 'Partially Refunded'
};

const BOOKING_STEPS = [
    {
        id: 1,
        title: 'Select Seats',
        description: 'Choose your seats',
        isClickable: true
    },
    {
        id: 2,
        title: 'Booking Summary',
        description: 'Review your booking',
        isClickable: true
    },
    {
        id: 3,
        title: 'Payment',
        description: 'Secure payment',
        isClickable: true
    },
    {
        id: 4,
        title: 'Confirmation',
        description: 'Booking confirmed',
        isClickable: false
    }
];

const SuccessPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const {
        getById,
        loading,
        isPaymentComplete,
        isPaymentFailed,
        isPaymentInProgress,
        getPaymentErrorMessage
    } = usePayment();

    const [isPolling, setIsPolling] = useState(false);
    const [pollingCount, setPollingCount] = useState(0);
    const [isVisible, setIsVisible] = useState(false);
    const [paymentData, setPaymentData] = useState<PaymentResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const pollingRef = useRef<NodeJS.Timeout | null>(null);
    const isInitialMount = useRef(true);

    const bookingId = searchParams.get('bookingId');
    const paymentId = searchParams.get('paymentId');

    const fetchPaymentData = useCallback(async (id: string) => {
        try {
            const data = await getById(parseInt(id));
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
        if (isPaymentComplete(paymentData)) return 'success';
        if (isPaymentFailed(paymentData)) return 'error';
        if (isPaymentInProgress(paymentData)) return 'warning';
        return 'info';
    };

    const getResultMessage = () => {
        if (!paymentData) return 'Loading payment information...';

        const status = paymentData.status as PaymentStatus;
        const messages: Record<PaymentStatus, string> = {
            PENDING: 'Your payment is being processed. Please wait...',
            PROCESSING: 'Payment is currently being processed.',
            SUCCESS: 'Payment was successful! Your tickets have been booked.',
            FAILED: 'Payment failed. Please try again or contact support.',
            CANCELLED: 'Payment was cancelled.',
            EXPIRED: 'Payment session has expired.',
            REFUNDED: 'Payment has been refunded.',
            PARTIALLY_REFUNDED: 'Payment has been partially refunded.'
        };

        return messages[status] || 'Payment status unknown.';
    };

    const getPaymentDetails = () => {
        if (!paymentData) return null;

        return {
            bookingId: paymentData.bookingId,
            paymentId: paymentData.id,
            amount: paymentData.amount,
            status: paymentData.status,
            statusDisplay: PaymentStatusDisplay[paymentData.status as PaymentStatus],
            liqpayOrderId: paymentData.liqpayOrderId,
            error: getPaymentErrorMessage(paymentData)
        };
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

        return () => {
            if (pollingRef.current) {
                clearInterval(pollingRef.current);
                pollingRef.current = null;
            }
            setIsPolling(false);
        };
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
            } else if (status === 'SUCCESS' || status === 'FAILED' || status === 'CANCELLED' || status === 'EXPIRED') {
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

    const handleStepClick = (step: any) => {
        if (step.id === 1 && paymentData?.bookingId) {
            navigate(`/booking/summary/${paymentData.bookingId}`);
        }
        if (step.id === 2 && paymentData?.bookingId) {
            navigate(`/booking/summary/${paymentData.bookingId}`);
        }
        if (step.id === 3 && paymentData?.bookingId) {
            navigate(`/booking/payment/${paymentData.bookingId}`);
        }
        if (step.id === 4) {
            return;
        }
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

    const getResultIcon = () => {
        switch (resultType) {
            case 'success': return '✓';
            case 'error': return '✗';
            case 'warning': return '⏳';
            default: return 'ℹ️';
        }
    };

    const resultStyles = getResultStyles();
    const resultIcon = getResultIcon();

    return (
        <div className={`${styles.container} ${isVisible ? styles.visible : ''}`}>
            <div className={styles.contentWrapper}>
                <ProgressStepper
                    steps={BOOKING_STEPS}
                    currentStep={4}
                    className={styles.stepper}
                    onStepClick={handleStepClick}
                />

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
                                    <p className={styles.detailValue}>{paymentDetails.bookingId}</p>
                                </div>
                                {paymentDetails.paymentId && (
                                    <div className={styles.detailItem}>
                                        <p className={styles.detailLabel}>Payment ID</p>
                                        <p className={styles.detailValue}>{paymentDetails.paymentId}</p>
                                    </div>
                                )}
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Amount</p>
                                    <p className={styles.detailPrice}>{paymentDetails.amount} UAH</p>
                                </div>
                                <div className={styles.detailItem}>
                                    <p className={styles.detailLabel}>Status</p>
                                    <p className={styles.detailValue}>{paymentDetails.statusDisplay}</p>
                                </div>
                                {paymentDetails.liqpayOrderId && (
                                    <div className={styles.detailItem}>
                                        <p className={styles.detailLabel}>LiqPay Order ID</p>
                                        <p className={styles.detailValue}>{paymentDetails.liqpayOrderId}</p>
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