import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { ProgressStepper } from '@/components/booking/ProgressStepper';
import { Badge } from '@/components/ui/Badge/Badge';
import { Button } from '@/components/ui/Button/Button';
import { Layout } from '@/components/layout/Layout/Layout';
import { Loader2, CreditCard, AlertCircle, CheckCircle2, ArrowLeft, Ticket, Home, RefreshCw } from 'lucide-react';
import type { PaymentStatus, PaymentResponse } from '@/types/payment';
import styles from './PaymentPage.module.css';

interface BookingData {
    id: number;
    bookingNumber: string;
    movieTitle: string;
    hallName: string;
    sessionTime: string;
    totalPrice: string;
    finalPrice: string;
    bonusPointsUsed: number;
    bookedSeats: Array<{
        seatNumber: string;
        seatRow: number;
        ticketType: string;
        seatPrice: string;
    }>;
}

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

export const PaymentPage: React.FC = () => {
    const { bookingId } = useParams<{ bookingId: string }>();
    const navigate = useNavigate();
    const location = useLocation();

    const bookingData = location.state?.booking as BookingData;

    const {
        create,
        getById,
        getLiqPayData,
        redirectToLiqPay,
        getLiqPayFormData,
        getPaymentErrorMessage,
        reset
    } = usePayment();

    const [step, setStep] = useState<'init' | 'processing' | 'ready' | 'paying' | 'success' | 'failed'>('init');
    const [selectedMethod, setSelectedMethod] = useState<'liqpay' | null>('liqpay');
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [currentPayment, setCurrentPayment] = useState<PaymentResponse | null>(null);
    const [paymentTimeLeft, setPaymentTimeLeft] = useState<string>('30 minutes');

    const isMountedRef = useRef(true);
    const isCreatingRef = useRef(false);
    const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

    const startPolling = useCallback((paymentId: number) => {
        stopPolling();

        const interval = setInterval(async () => {
            try {
                const payment = await getById(paymentId);
                setCurrentPayment(payment);

                if (payment && (payment.status === 'SUCCESS' || payment.status === 'FAILED' || payment.status === 'CANCELLED' || payment.status === 'EXPIRED')) {
                    stopPolling();
                }

                if (payment?.createdAt) {
                    const now = new Date();
                    const createdAt = new Date(payment.createdAt);
                    const expiresAt = new Date(createdAt.getTime() + 30 * 60000);
                    const diffMs = expiresAt.getTime() - now.getTime();
                    const diffMinutes = Math.floor(diffMs / (1000 * 60));

                    if (diffMs <= 0) {
                        setPaymentTimeLeft('Expired');
                    } else if (diffMinutes > 60) {
                        setPaymentTimeLeft(`${Math.floor(diffMinutes / 60)}h ${diffMinutes % 60}m`);
                    } else {
                        setPaymentTimeLeft(`${diffMinutes}m`);
                    }
                }
            } catch (error) {
                console.error('Polling error:', error);
            }
        }, 3000);

        pollingIntervalRef.current = interval;
    }, [getById]);

    const stopPolling = useCallback(() => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    }, []);

    const initPayment = useCallback(async () => {
        if (!bookingId || isCreatingRef.current) {
            return;
        }

        isCreatingRef.current = true;
        setStep('processing');
        setErrorMessage(null);

        const paymentKey = `payment_attempt_${bookingId}`;
        const attemptTime = localStorage.getItem(paymentKey);
        const now = Date.now();

        if (attemptTime && (now - parseInt(attemptTime)) < 10000) {
            setErrorMessage('Payment is already being processed. Please wait.');
            setStep('failed');
            isCreatingRef.current = false;
            return;
        }

        localStorage.setItem(paymentKey, now.toString());

        try {
            const result = await create({ bookingId: parseInt(bookingId) });

            if (result) {
                setCurrentPayment(result);
                const liqPayData = await getLiqPayData(result.id);
                if (liqPayData) {
                    setStep('ready');
                } else {
                    setStep('failed');
                    setErrorMessage('Failed to initialize payment gateway');
                }
            } else {
                setStep('failed');
                setErrorMessage('Failed to create payment');
            }
        } catch (error) {
            setStep('failed');
            setErrorMessage(error instanceof Error ? error.message : 'Failed to create payment');
        } finally {
            isCreatingRef.current = false;
            setTimeout(() => {
                localStorage.removeItem(paymentKey);
            }, 10000);
        }
    }, [bookingId, create, getLiqPayData]);

    useEffect(() => {
        isMountedRef.current = true;

        if (bookingId && step === 'init') {
            initPayment();
        }

        return () => {
            isMountedRef.current = false;
            stopPolling();
        };
    }, [bookingId, step, initPayment, stopPolling]);

    useEffect(() => {
        if (currentPayment) {
            if (currentPayment.status === 'SUCCESS') {
                setStep('success');
                stopPolling();
            } else if (['FAILED', 'CANCELLED', 'EXPIRED'].includes(currentPayment.status)) {
                setStep('failed');
                setErrorMessage(currentPayment.errorDescription || getPaymentErrorMessage(currentPayment) || 'Payment failed');
                stopPolling();
            } else if (currentPayment.status === 'PROCESSING' && step !== 'paying') {
                setStep('paying');
            }
        }
    }, [currentPayment, step, stopPolling, getPaymentErrorMessage]);

    const handlePayWithLiqPay = async () => {
        if (!currentPayment?.id) return;

        setStep('paying');

        const liqPayData = getLiqPayFormData();
        if (liqPayData) {
            redirectToLiqPay();
            startPolling(currentPayment.id);
        } else {
            setStep('failed');
            setErrorMessage('Payment data not available');
        }
    };

    const handleRetry = () => {
        stopPolling();
        reset();
        setCurrentPayment(null);
        setErrorMessage(null);
        setStep('init');
    };

    const handleBack = () => {
        navigate(`/booking/summary/${bookingId}`, { state: { booking: bookingData } });
    };

    const handleViewTickets = () => {
        navigate('/account/tickets');
    };

    const handleGoHome = () => {
        navigate('/');
    };

    const handleStepClick = (step: any) => {
        if (step.id === 1 && bookingData?.id) {
            navigate(`/booking/summary/${bookingData.id}`);
        }
        if (step.id === 2 && bookingData?.id) {
            navigate(`/booking/summary/${bookingData.id}`);
        }
        if (step.id === 3) {
            return;
        }
    };

    const formatTime = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const renderStepContent = () => {
        switch (step) {
            case 'init':
            case 'processing':
                return (
                    <div className={styles.loadingContainer}>
                        <Loader2 className={styles.loadingSpinner} size={48} />
                        <h3 className={styles.statusTitle}>Preparing Payment</h3>
                        <p className={styles.statusMessage}>Please wait while we set up your payment</p>
                    </div>
                );

            case 'ready':
                return (
                    <div className={styles.statusContainer}>
                        <CreditCard className={styles.infoIcon} size={64} />
                        <h3 className={styles.statusTitle}>Ready to Pay</h3>
                        <p className={styles.statusMessage}>Choose your payment method and continue</p>

                        <div className={styles.paymentMethods}>
                            <h4 className={styles.methodsTitle}>Payment Method</h4>
                            <div className={styles.methodsList}>
                                <div
                                    className={`${styles.methodItem} ${selectedMethod === 'liqpay' ? styles.active : ''}`}
                                    onClick={() => setSelectedMethod('liqpay')}
                                >
                                    <CreditCard className={styles.methodIcon} size={20} />
                                    <span className={styles.methodName}>Card Payment</span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.actions}>
                            <Button
                                onClick={handleBack}
                                variant="secondary"
                                className={styles.secondaryButton}
                            >
                                <ArrowLeft size={20} />
                                Back
                            </Button>
                            <Button
                                onClick={handlePayWithLiqPay}
                                variant="primary"
                                className={styles.primaryButton}
                                disabled={!selectedMethod}
                            >
                                <CreditCard size={20} />
                                Pay with Card
                            </Button>
                        </div>
                    </div>
                );

            case 'paying':
                return (
                    <div className={styles.statusContainer}>
                        <Loader2 className={styles.loadingSpinner} size={64} />
                        <h3 className={styles.statusTitle}>Redirecting to Payment</h3>
                        <p className={styles.statusMessage}>You will be redirected to the payment page</p>
                        <p className={styles.statusMessage}>Time left: {paymentTimeLeft}</p>
                        <div className={styles.alertContainer}>
                            <AlertCircle size={16} />
                            <p>Please complete the payment on the next page</p>
                        </div>
                    </div>
                );

            case 'success':
                return (
                    <div className={styles.statusContainer}>
                        <CheckCircle2 className={styles.successIcon} size={64} />
                        <h3 className={styles.statusTitle}>Payment Successful!</h3>
                        <p className={styles.statusMessage}>Your tickets have been successfully paid and booked</p>

                        {currentPayment?.liqpayPaymentId && (
                            <div className={styles.paymentNumber}>
                                Payment ID: {currentPayment.liqpayPaymentId}
                            </div>
                        )}

                        {currentPayment?.status && (
                            <Badge variant="success" className={styles.badge}>
                                <CheckCircle2 size={12} />
                                {PaymentStatusDisplay[currentPayment.status]}
                            </Badge>
                        )}

                        <div className={styles.actions}>
                            <Button
                                onClick={handleViewTickets}
                                variant="primary"
                                className={styles.primaryButton}
                            >
                                <Ticket size={20} />
                                View My Tickets
                            </Button>
                            <Button
                                onClick={handleGoHome}
                                variant="secondary"
                                className={styles.secondaryButton}
                            >
                                <Home size={20} />
                                Home Page
                            </Button>
                        </div>
                    </div>
                );

            case 'failed':
                return (
                    <div className={styles.statusContainer}>
                        <AlertCircle className={styles.errorIcon} size={64} />
                        <h3 className={styles.statusTitle}>Payment Failed</h3>
                        <p className={styles.statusMessage}>
                            {errorMessage || 'There was an issue processing your payment'}
                        </p>

                        {currentPayment?.status && (
                            <Badge variant="error" className={styles.badge}>
                                <AlertCircle size={12} />
                                {PaymentStatusDisplay[currentPayment.status]}
                            </Badge>
                        )}

                        <div className={styles.actions}>
                            <Button
                                onClick={handleRetry}
                                variant="primary"
                                className={styles.primaryButton}
                            >
                                <RefreshCw size={20} />
                                Try Again
                            </Button>
                            <Button
                                onClick={handleBack}
                                variant="secondary"
                                className={styles.secondaryButton}
                            >
                                <ArrowLeft size={20} />
                                Back to Summary
                            </Button>
                        </div>
                    </div>
                );
        }
    };

    const renderSummaryCard = () => {
        if (!bookingData) return null;

        return (
            <div className={styles.summaryCard}>
                <div className={styles.summaryHeader}>
                    <h3 className={styles.summaryTitle}>Booking Summary</h3>
                </div>
                <div className={styles.summaryBody}>
                    <div className={styles.movieInfo}>
                        <h3 className={styles.movieTitle}>{bookingData.movieTitle}</h3>
                        <p className={styles.movieDetails}>
                            {bookingData.hallName} • {formatTime(bookingData.sessionTime)}
                        </p>
                        <Badge variant="primary" className={styles.badge}>
                            {bookingData.bookingNumber}
                        </Badge>
                    </div>

                    <div className={styles.divider}></div>

                    <div className={styles.seatsList}>
                        <h4 className={styles.seatsTitle}>Selected Seats</h4>
                        {bookingData.bookedSeats.map((seat, index) => (
                            <div key={index} className={styles.seatItem}>
                                <span className={styles.seatLabel}>
                                    Row {seat.seatRow}, Seat {seat.seatNumber}
                                </span>
                                <div>
                                    <span className={styles.seatType}>{seat.ticketType}</span>
                                    <span className={styles.seatLabel}> • {seat.seatPrice} UAH</span>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className={styles.divider}></div>

                    <div className={styles.detailRow}>
                        <span className={styles.detailLabel}>Total price:</span>
                        <span className={styles.detailValue}>{bookingData.totalPrice} UAH</span>
                    </div>
                    {bookingData.bonusPointsUsed > 0 && (
                        <div className={styles.detailRow}>
                            <span className={styles.detailLabel}>Bonus points used:</span>
                            <span className={styles.detailValue}>{bookingData.bonusPointsUsed} points</span>
                        </div>
                    )}
                    <div className={styles.totalRow}>
                        <span>Final amount:</span>
                        <span>{bookingData.finalPrice} UAH</span>
                    </div>
                </div>
            </div>
        );
    };

    return (
        <Layout>
            <div className={styles.container}>
                <ProgressStepper
                    steps={BOOKING_STEPS}
                    currentStep={3}
                    className={styles.stepper}
                    onStepClick={handleStepClick}
                />

                <div className={styles.header}>
                    <h1 className={styles.title}>Payment</h1>
                    <p className={styles.subtitle}>
                        Complete your booking by making a secure payment
                    </p>
                </div>

                <div className={styles.content}>
                    <div className={styles.paymentCard}>
                        <div className={styles.paymentHeader}>
                            <h3 className={styles.paymentTitle}>Payment Details</h3>
                            <p className={styles.paymentDescription}>
                                {step === 'ready' && 'Select payment method and proceed'}
                                {step === 'paying' && 'Redirecting to payment page'}
                                {step === 'success' && 'Payment completed successfully'}
                                {step === 'failed' && 'Payment processing failed'}
                                {step === 'processing' && 'Setting up your payment'}
                            </p>
                        </div>
                        <div className={styles.paymentBody}>
                            {renderStepContent()}
                        </div>
                    </div>

                    {renderSummaryCard()}
                </div>
            </div>
        </Layout>
    );
};