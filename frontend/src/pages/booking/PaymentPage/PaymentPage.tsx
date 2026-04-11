import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { ProgressStepper } from '@/components/booking/ProgressStepper/ProgressStepper';
import { BOOKING_STEPS } from '@/components/booking/ProgressStepper/bookingSteps';
import { Badge } from '@/components/ui/Badge/Badge';
import { Button } from '@/components/ui/Button/Button';
import { Layout } from '@/components/layout/Layout/Layout';
import type { PaymentResponse } from '@/types/payment';
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

type PaymentStep = 'init' | 'processing' | 'ready' | 'paying' | 'success' | 'failed';

export const PaymentPage: React.FC = () => {
    const { bookingId } = useParams<{ bookingId: string }>();
    const navigate = useNavigate();
    const location = useLocation();
    const bookingData = location.state?.booking as BookingData;

    const { create, getById, getLiqPayData, loading } = usePayment();

    const [step, setStep] = useState<PaymentStep>('init');
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [payment, setPayment] = useState<PaymentResponse | null>(null);
    const [pollingInterval, setPollingInterval] = useState<number | null>(null);

    const stopPolling = useCallback(() => {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            setPollingInterval(null);
        }
    }, [pollingInterval]);

    const startPolling = useCallback((paymentId: number) => {
        stopPolling();

        const interval = window.setInterval(async () => {
            const result = await getById(paymentId);
            if (result) {
                setPayment(result);

                if (['SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED'].includes(result.status)) {
                    stopPolling();
                    setStep(result.status === 'SUCCESS' ? 'success' : 'failed');
                }
            }
        }, 3000);

        setPollingInterval(interval);
    }, [getById, stopPolling]);

    const initPayment = useCallback(async () => {
        if (!bookingId) return;

        setStep('processing');

        const result = await create({ bookingId: parseInt(bookingId) });
        if (!result) {
            setStep('failed');
            setErrorMessage('Failed to create payment');
            return;
        }

        setPayment(result);
        startPolling(result.id);

        const liqPayData = await getLiqPayData(result.id);
        if (liqPayData?.paymentUrl) {
            setStep('ready');
        } else {
            setStep('failed');
            setErrorMessage('Failed to initialize payment gateway');
        }
    }, [bookingId, create, getLiqPayData, startPolling]);

    useEffect(() => {
        if (step === 'init') {
            initPayment();
        }
        return () => stopPolling();
    }, [step, initPayment, stopPolling]);

    const handlePay = async () => {
        if (!payment) return;

        setStep('paying');
        const liqPayData = await getLiqPayData(payment.id);

        if (liqPayData?.paymentUrl) {
            window.location.href = liqPayData.paymentUrl;
        } else {
            setStep('failed');
            setErrorMessage('Payment data not available');
        }
    };

    const handleRetry = () => {
        stopPolling();
        setPayment(null);
        setErrorMessage(null);
        setStep('init');
    };

    const formatTime = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    if (!bookingData) {
        return (
            <Layout>
                <div className={styles.error}>Booking data not found</div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.container}>
                <ProgressStepper steps={BOOKING_STEPS} currentStep={3} className={styles.stepper} />

                <div className={styles.header}>
                    <h1 className={styles.title}>Payment</h1>
                    <p className={styles.subtitle}>Complete your booking by making a secure payment</p>
                </div>

                <div className={styles.content}>
                    <div className={styles.paymentCard}>
                        <div className={styles.paymentHeader}>
                            <h3>Payment Details</h3>
                        </div>
                        <div className={styles.paymentBody}>
                            {step === 'processing' && (
                                <div className={styles.loadingContainer}>
                                    <div className={styles.loadingSpinner} />
                                    <h3>Preparing Payment</h3>
                                    <p>Please wait while we set up your payment</p>
                                </div>
                            )}

                            {step === 'ready' && (
                                <div className={styles.statusContainer}>
                                    <h3>Ready to Pay</h3>
                                    <Button onClick={handlePay} variant="primary" disabled={loading}>
                                        Pay with Card
                                    </Button>
                                </div>
                            )}

                            {step === 'paying' && (
                                <div className={styles.statusContainer}>
                                    <div className={styles.loadingSpinner} />
                                    <h3>Redirecting to Payment</h3>
                                    <p>You will be redirected to the payment page</p>
                                </div>
                            )}

                            {step === 'success' && (
                                <div className={styles.statusContainer}>
                                    <h3>Payment Successful!</h3>
                                    {payment?.status && (
                                        <Badge variant="success">{PaymentStatusDisplay[payment.status]}</Badge>
                                    )}
                                    <Button onClick={() => navigate('/account/tickets')} variant="primary">
                                        View My Tickets
                                    </Button>
                                </div>
                            )}

                            {step === 'failed' && (
                                <div className={styles.statusContainer}>
                                    <h3>Payment Failed</h3>
                                    <p>{errorMessage || 'There was an issue processing your payment'}</p>
                                    <Button onClick={handleRetry} variant="primary">
                                        Try Again
                                    </Button>
                                    <Button onClick={() => navigate(`/booking/summary/${bookingId}`)} variant="secondary">
                                        Back to Summary
                                    </Button>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className={styles.summaryCard}>
                        <h3>Booking Summary</h3>
                        <div className={styles.movieInfo}>
                            <h4>{bookingData.movieTitle}</h4>
                            <p>{bookingData.hallName} • {formatTime(bookingData.sessionTime)}</p>
                            <Badge variant="primary">{bookingData.bookingNumber}</Badge>
                        </div>

                        <div className={styles.seatsList}>
                            <h4>Selected Seats</h4>
                            {bookingData.bookedSeats.map((seat, i) => (
                                <div key={i} className={styles.seatItem}>
                                    <span>Row {seat.seatRow}, Seat {seat.seatNumber}</span>
                                    <span>{seat.ticketType} • {seat.seatPrice} UAH</span>
                                </div>
                            ))}
                        </div>

                        <div className={styles.totalRow}>
                            <span>Final amount:</span>
                            <span>{bookingData.finalPrice} UAH</span>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};