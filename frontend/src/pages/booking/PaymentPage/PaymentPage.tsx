import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { usePaymentForm } from '@/hooks/features/payment/usePaymentForm';
import { useLiqPayPayment } from '@/hooks/features/payment/useLiqPayPayment';
import { Badge } from '@/components/ui/Badge/Badge';
import { Button } from '@/components/ui/Button/Button';
import { Layout } from '@/components/layout/Layout/Layout';
import { Loader2, CreditCard, AlertCircle, CheckCircle2, ArrowLeft, Ticket, Home, RefreshCw } from 'lucide-react';
import { PaymentStatusDisplay } from '@/types/payment';
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

export const PaymentPage: React.FC = () => {
    const { bookingId } = useParams<{ bookingId: string }>();
    const navigate = useNavigate();
    const location = useLocation();

    const bookingData = location.state?.booking as BookingData;

    const { handleCreate, paymentResult, error: createError } = usePaymentForm();
    const {
        initializePayment,
        openLiqPayPopup,
        paymentStatus,
        error: liqPayError,
        getPaymentTimeLeft,
        startStatusPolling,
        stopStatusPolling,
        reset
    } = useLiqPayPayment();

    const [step, setStep] = useState<'init' | 'processing' | 'ready' | 'paying' | 'success' | 'failed'>('init');
    const [selectedMethod, setSelectedMethod] = useState<'liqpay' | null>('liqpay');
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    useEffect(() => {
        if (bookingId && step === 'init') {
            initPayment();
        }
    }, [bookingId]);

    useEffect(() => {
        if (paymentStatus) {
            if (paymentStatus.status === 'SUCCESS') {
                setStep('success');
                stopStatusPolling();
            } else if (['FAILED', 'CANCELLED', 'EXPIRED'].includes(paymentStatus.status)) {
                setStep('failed');
                setErrorMessage(paymentStatus.errorDescription || 'Payment failed');
                stopStatusPolling();
            } else if (paymentStatus.status === 'PROCESSING' && step !== 'paying') {
                setStep('paying');
            }
        }
    }, [paymentStatus]);

    useEffect(() => {
        if (createError) {
            setErrorMessage(createError);
            setStep('failed');
        }
        if (liqPayError) {
            setErrorMessage(liqPayError);
            setStep('failed');
        }
    }, [createError, liqPayError]);

    const initPayment = async () => {
        if (!bookingId) return;

        setStep('processing');
        setErrorMessage(null);
        const result = await handleCreate({ bookingId: parseInt(bookingId) });

        if (result) {
            const liqPayData = await initializePayment(result.id);
            if (liqPayData) {
                setStep('ready');
            } else {
                setStep('failed');
                setErrorMessage('Failed to initialize payment gateway');
            }
        } else {
            setStep('failed');
        }
    };

    const handlePayWithLiqPay = async () => {
        if (!paymentResult?.id) return;

        setStep('paying');
        openLiqPayPopup();
        startStatusPolling(paymentResult.id, 3000);
    };

    const handleRetry = () => {
        reset();
        setErrorMessage(null);
        setStep('init');
        initPayment();
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
                        <h3 className={styles.statusTitle}>Awaiting Payment</h3>
                        <p className={styles.statusMessage}>Please complete the payment in the opened window</p>
                        <p className={styles.statusMessage}>Time left: {getPaymentTimeLeft() || '30 minutes'}</p>
                        <div className={styles.alertContainer}>
                            <AlertCircle size={16} />
                            <p>Do not close this page until payment is complete</p>
                        </div>
                    </div>
                );

            case 'success':
                return (
                    <div className={styles.statusContainer}>
                        <CheckCircle2 className={styles.successIcon} size={64} />
                        <h3 className={styles.statusTitle}>Payment Successful!</h3>
                        <p className={styles.statusMessage}>Your tickets have been successfully paid and booked</p>

                        {paymentStatus?.liqpayPaymentId && (
                            <div className={styles.paymentNumber}>
                                Payment ID: {paymentStatus.liqpayPaymentId}
                            </div>
                        )}

                        <Badge variant="success" className={styles.badge}>
                            <CheckCircle2 size={12} />
                            {paymentStatus ? PaymentStatusDisplay[paymentStatus.status] : 'SUCCESS'}
                        </Badge>

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

                        <Badge variant="error" className={styles.badge}>
                            <AlertCircle size={12} />
                            {paymentStatus ? PaymentStatusDisplay[paymentStatus.status] : 'FAILED'}
                        </Badge>

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
                                {step === 'paying' && 'Processing your payment'}
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