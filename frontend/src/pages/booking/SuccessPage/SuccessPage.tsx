import { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { PaymentResponse } from '@/types/payment';
import styles from './SuccessPage.module.css';

const SuccessPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { getById, loading } = usePayment();

    const [payment, setPayment] = useState<PaymentResponse | null>(null);
    const pollingRef = useRef<number | null>(null);

    const paymentId = searchParams.get('paymentId');
    const bookingId = searchParams.get('bookingId');

    useEffect(() => {
        const id = paymentId || bookingId;
        if (!id) {
            navigate('/');
            return;
        }

        const fetchPayment = async () => {
            const result = await getById(parseInt(id));
            setPayment(result);
        };

        fetchPayment();

        pollingRef.current = window.setInterval(fetchPayment, 5000);

        return () => {
            if (pollingRef.current) clearInterval(pollingRef.current);
        };
    }, [paymentId, bookingId, getById, navigate]);

    useEffect(() => {
        if (payment && ['SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED'].includes(payment.status)) {
            if (pollingRef.current) {
                clearInterval(pollingRef.current);
                pollingRef.current = null;
            }
        }
    }, [payment]);

    if (loading && !payment) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading payment information..." />
            </div>
        );
    }

    if (!payment) {
        return null;
    }

    const isSuccess = payment.status === 'SUCCESS';
    const isFailed = ['FAILED', 'CANCELLED', 'EXPIRED'].includes(payment.status);
    const isProcessing = ['PENDING', 'PROCESSING'].includes(payment.status);

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <div className={styles.iconWrapper}>
                    {isSuccess && <span className={styles.successIcon}>✓</span>}
                    {isFailed && <span className={styles.errorIcon}>✗</span>}
                    {isProcessing && <span className={styles.warningIcon}>⏳</span>}
                </div>

                <h1 className={styles.title}>
                    {isSuccess && 'Payment Successful!'}
                    {isFailed && 'Payment Failed'}
                    {isProcessing && 'Payment Processing'}
                </h1>

                <p className={styles.message}>
                    {isSuccess && 'Your tickets have been booked.'}
                    {isFailed && 'Payment failed. Please try again.'}
                    {isProcessing && 'Your payment is being processed. Please wait...'}
                </p>

                <div className={styles.actions}>
                    {isSuccess && (
                        <>
                            <Button variant="primary" onClick={() => navigate('/account/tickets')}>
                                View My Tickets
                            </Button>
                            <Button variant="secondary" onClick={() => navigate('/')}>
                                Back to Home
                            </Button>
                        </>
                    )}
                    {isFailed && (
                        <>
                            {bookingId && (
                                <Button variant="primary" onClick={() => navigate(`/booking/payment/${bookingId}`)}>
                                    Try Again
                                </Button>
                            )}
                            <Button variant="secondary" onClick={() => navigate('/')}>
                                Back to Home
                            </Button>
                        </>
                    )}
                    {isProcessing && (
                        <Button variant="secondary" onClick={() => navigate('/')}>
                            Back to Home
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default SuccessPage;