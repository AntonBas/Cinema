import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentResponse } from '@/types/payment';

export const usePaymentResult = () => {
    const { getById, loading, error, clearError } = usePayment();

    const [paymentData, setPaymentData] = useState<PaymentResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const fetchPaymentData = useCallback(async (paymentId?: string | number) => {
        if (!paymentId) {
            setIsLoading(false);
            return;
        }

        clearError();
        setIsLoading(true);

        try {
            const id = typeof paymentId === 'string' ? parseInt(paymentId, 10) : paymentId;

            if (isNaN(id)) {
                throw new Error('Invalid payment ID');
            }

            const payment = await getById(id);
            setPaymentData(payment);
        } catch (err) {
            console.error('Failed to fetch payment:', err);
        } finally {
            setIsLoading(false);
        }
    }, [getById, clearError]);

    const getResultMessage = useCallback(() => {
        if (!paymentData) return 'Loading payment information...';

        switch (paymentData.status) {
            case 'SUCCESS':
                return 'Payment completed successfully! Your tickets have been sent to your email.';
            case 'PENDING':
                return 'Payment is being processed. Please wait...';
            case 'PROCESSING':
                return 'Payment is being processed. This may take a few moments.';
            case 'FAILED':
                return `Payment failed: ${paymentData.errorDescription || paymentData.errorCode || 'Unknown error'}`;
            case 'CANCELLED':
                return 'Payment was cancelled.';
            case 'EXPIRED':
                return 'Payment expired. Please try again.';
            case 'REFUNDED':
                return 'Payment has been refunded.';
            case 'PARTIALLY_REFUNDED':
                return 'Payment has been partially refunded.';
            default:
                return 'Payment status unknown.';
        }
    }, [paymentData]);

    const getResultType = useCallback(() => {
        if (!paymentData) return 'info';

        switch (paymentData.status) {
            case 'SUCCESS':
            case 'REFUNDED':
            case 'PARTIALLY_REFUNDED':
                return 'success';
            case 'PENDING':
            case 'PROCESSING':
                return 'warning';
            case 'FAILED':
            case 'CANCELLED':
            case 'EXPIRED':
                return 'error';
            default:
                return 'info';
        }
    }, [paymentData]);

    const getPaymentDetails = useCallback(() => {
        if (!paymentData) return null;

        return {
            bookingId: paymentData.bookingId,
            amount: paymentData.amount,
            status: paymentData.status,
            liqpayOrderId: paymentData.liqpayOrderId,
            paymentId: paymentData.id
        };
    }, [paymentData]);

    const reset = useCallback(() => {
        setPaymentData(null);
        setIsLoading(true);
        clearError();
    }, [clearError]);

    return {
        loading: loading || isLoading,
        error,
        paymentData,
        fetchPaymentData,
        getResultMessage,
        getResultType,
        getPaymentDetails,
        hasPaymentData: !!paymentData,
        clearError,
        reset
    };
};