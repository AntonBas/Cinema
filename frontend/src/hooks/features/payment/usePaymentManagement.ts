import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentResponse } from '@/types/payment';

export const usePaymentManagement = () => {
    const { getByBooking, getStatus, retry, loading, error, clearError } = usePayment();
    const [activePayments, setActivePayments] = useState<PaymentResponse[]>([]);
    const [retryingId, setRetryingId] = useState<number | null>(null);

    const getPaymentForBooking = useCallback(async (bookingId: number): Promise<PaymentResponse | null> => {
        clearError();
        try {
            return await getByBooking(bookingId);
        } catch {
            return null;
        }
    }, [getByBooking, clearError]);

    const handleRetryPayment = useCallback(async (paymentId: number): Promise<PaymentResponse | null> => {
        setRetryingId(paymentId);
        clearError();
        try {
            const result = await retry(paymentId);
            return result;
        } catch {
            return null;
        } finally {
            setRetryingId(null);
        }
    }, [retry, clearError]);

    const refreshPaymentStatus = useCallback(async (paymentId: number): Promise<PaymentResponse | null> => {
        clearError();
        try {
            return await getStatus(paymentId);
        } catch {
            return null;
        }
    }, [getStatus, clearError]);

    const isPaymentRetrying = useCallback((paymentId: number): boolean => {
        return retryingId === paymentId;
    }, [retryingId]);

    const getPaymentByBookingId = useCallback((bookingId: number): PaymentResponse | undefined => {
        return activePayments.find(payment => payment.bookingId === bookingId);
    }, [activePayments]);

    const addPaymentToActive = useCallback((payment: PaymentResponse) => {
        setActivePayments(prev => {
            const existingIndex = prev.findIndex(p => p.id === payment.id);
            if (existingIndex >= 0) {
                const updated = [...prev];
                updated[existingIndex] = payment;
                return updated;
            }
            return [...prev, payment];
        });
    }, []);

    const removePaymentFromActive = useCallback((paymentId: number) => {
        setActivePayments(prev => prev.filter(payment => payment.id !== paymentId));
    }, []);

    const getPendingPayments = useCallback((): PaymentResponse[] => {
        return activePayments.filter(payment =>
            payment.status === 'PENDING' || payment.status === 'PROCESSING'
        );
    }, [activePayments]);

    const getFailedPayments = useCallback((): PaymentResponse[] => {
        return activePayments.filter(payment =>
            payment.status === 'FAILED' || payment.status === 'CANCELLED'
        );
    }, [activePayments]);

    const getSuccessfulPayments = useCallback((): PaymentResponse[] => {
        return activePayments.filter(payment => payment.status === 'SUCCESS');
    }, [activePayments]);

    const canRetryPayment = useCallback((payment: PaymentResponse): boolean => {
        return payment.status === 'FAILED' || payment.status === 'CANCELLED';
    }, []);

    const isPaymentExpired = useCallback((payment: PaymentResponse): boolean => {
        if (payment.status === 'EXPIRED') return true;

        const createdAt = new Date(payment.createdAt);
        const now = new Date();
        const diffMs = now.getTime() - createdAt.getTime();
        const diffMinutes = Math.floor(diffMs / (1000 * 60));

        return diffMinutes > 30 && payment.status === 'PENDING';
    }, []);

    const getPaymentTimeInfo = useCallback((payment: PaymentResponse): {
        createdAt: string;
        paymentTime: string | null;
        isExpired: boolean;
        timeSinceCreation: string
    } => {
        const createdAt = new Date(payment.createdAt);
        const paymentTime = payment.paymentTime ? new Date(payment.paymentTime) : null;
        const now = new Date();

        const diffMs = now.getTime() - createdAt.getTime();
        const diffMinutes = Math.floor(diffMs / (1000 * 60));

        let timeSinceCreation = '';
        if (diffMinutes < 60) {
            timeSinceCreation = `${diffMinutes} minutes ago`;
        } else if (diffMinutes < 1440) {
            const hours = Math.floor(diffMinutes / 60);
            timeSinceCreation = `${hours} hours ago`;
        } else {
            const days = Math.floor(diffMinutes / 1440);
            timeSinceCreation = `${days} days ago`;
        }

        return {
            createdAt: createdAt.toLocaleString(),
            paymentTime: paymentTime ? paymentTime.toLocaleString() : null,
            isExpired: isPaymentExpired(payment),
            timeSinceCreation
        };
    }, [isPaymentExpired]);

    return {
        loading,
        error,
        activePayments,
        retryingId,
        getPaymentForBooking,
        handleRetryPayment,
        refreshPaymentStatus,
        isPaymentRetrying,
        getPaymentByBookingId,
        addPaymentToActive,
        removePaymentFromActive,
        getPendingPayments,
        getFailedPayments,
        getSuccessfulPayments,
        canRetryPayment,
        isPaymentExpired,
        getPaymentTimeInfo,
        clearError,
        hasActivePayments: activePayments.length > 0
    };
};