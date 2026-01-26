import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentResponse } from '@/types/payment';

export const usePaymentResult = () => {
    const { getById, getByBooking, loading, error, clearError } = usePayment();

    const [paymentData, setPaymentData] = useState<PaymentResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const fetchPaymentData = useCallback(async (bookingId?: string, paymentId?: string) => {
        if (!bookingId && !paymentId) {
            setIsLoading(false);
            return;
        }

        clearError();
        setIsLoading(true);

        try {
            let payment: PaymentResponse;

            if (bookingId) {
                payment = await getByBooking(parseInt(bookingId));
            } else if (paymentId) {
                payment = await getById(parseInt(paymentId));
            } else {
                return;
            }

            setPaymentData(payment);
        } catch (err) {
            console.error('Failed to fetch payment:', err);
        } finally {
            setIsLoading(false);
        }
    }, [getById, getByBooking, clearError]);

    const getResultMessage = useCallback(() => {
        if (!paymentData) return null;

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

    const getResultIcon = useCallback(() => {
        const type = getResultType();

        switch (type) {
            case 'success':
                return '✓';
            case 'error':
                return '✗';
            case 'warning':
                return '⏳';
            default:
                return 'ℹ';
        }
    }, [getResultType]);

    const getPaymentDetails = useCallback(() => {
        if (!paymentData) return null;

        return {
            bookingNumber: paymentData.bookingNumber,
            movieTitle: paymentData.movieTitle,
            hallName: paymentData.hallName,
            sessionTime: paymentData.sessionTime,
            finalAmount: paymentData.finalAmount,
            paymentTime: paymentData.paymentTime,
            senderCardMask: paymentData.senderCardMask,
            status: paymentData.status
        };
    }, [paymentData]);

    return {
        loading: loading || isLoading,
        error,
        paymentData,
        fetchPaymentData,
        getResultMessage,
        getResultType,
        getResultIcon,
        getPaymentDetails,
        hasPaymentData: !!paymentData,
        clearError
    };
};