import { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { usePayment } from '@/hooks/features/payment/usePayment';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { CheckCircle2, Home, Ticket, AlertCircle, RefreshCw } from 'lucide-react';
import type { PaymentResponse } from '@/types/payment';
import styles from './SuccessPage.module.css';

const SuccessPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { getById, loading } = usePayment();

    const [payment, setPayment] = useState<PaymentResponse | null>(null);
    const [pollingCount, setPollingCount] = useState(0);
    const [isVisible, setIsVisible] = useState(false);
    const pollingRef = useRef<number | null>(null);

    const paymentId = searchParams.get('paymentId');
    const bookingId = searchParams.get('bookingId');

    useEffect(() => {
        setIsVisible(true);
        const id = paymentId || bookingId;
        if (!id) {
            navigate('/');
            return;
        }

        const fetchPayment = async () => {
            const result = await getById(parseInt(id));
            setPayment(result);
            setPollingCount(prev => prev + 1);
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
        if (pollingCount >= 30) {
            if (pollingRef.current) {
                clearInterval(pollingRef.current);
                pollingRef.current = null;
            }
        }
    }, [payment, pollingCount]);

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
        <div className={`${styles.container} ${isVisible ? styles.visible : ''}`}>
            <div className={styles.card}>
                <div className={isSuccess ? styles.successContainer : isFailed ? styles.errorContainer : styles.warningContainer}>
                    <div className={`${styles.iconWrapper} ${isSuccess ? styles.successIconWrapper : isFailed ? styles.errorIconWrapper : styles.warningIconWrapper}`}>
                        {isSuccess && <CheckCircle2 className={styles.successIcon} size={64} />}
                        {isFailed && <AlertCircle className={styles.errorIcon} size={64} />}
                        {isProcessing && <RefreshCw className={styles.warningIcon} size={64} />}
                    </div>

                    <h1 className={styles.title}>
                        {isSuccess && 'Payment Successful!'}
                        {isFailed && 'Payment Failed'}
                        {isProcessing && 'Payment Processing'}
                    </h1>

                    <p className={styles.message}>
                        {isSuccess && 'Your tickets have been successfully paid and booked.'}
                        {isFailed && 'Payment failed. Please try again or contact support.'}
                        {isProcessing && 'Your payment is being processed. Please wait...'}
                    </p>

                    {isProcessing && pollingRef.current && (
                        <div className={styles.pollingInfo}>
                            <LoadingSpinner text="" />
                            <span>Checking payment status... {pollingCount}/30</span>
                        </div>
                    )}

                    <div className={styles.actions}>
                        {isSuccess && (
                            <>
                                <Button variant="primary" onClick={() => navigate('/account/tickets')}>
                                    <Ticket size={18} /> View My Tickets
                                </Button>
                                <Button variant="secondary" onClick={() => navigate('/')}>
                                    <Home size={18} /> Back to Home
                                </Button>
                            </>
                        )}
                        {isFailed && (
                            <>
                                {bookingId && (
                                    <Button variant="primary" onClick={() => navigate(`/booking/payment/${bookingId}`)}>
                                        <RefreshCw size={18} /> Try Again
                                    </Button>
                                )}
                                <Button variant="secondary" onClick={() => navigate('/')}>
                                    <Home size={18} /> Back to Home
                                </Button>
                            </>
                        )}
                        {isProcessing && (
                            <Button variant="secondary" onClick={() => navigate('/')}>
                                <Home size={18} /> Back to Home
                            </Button>
                        )}
                    </div>
                </div>

                {isSuccess && (
                    <div className={styles.footer}>
                        <span className={styles.footerText}>Thank you for your purchase! Enjoy the movie!</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default SuccessPage;