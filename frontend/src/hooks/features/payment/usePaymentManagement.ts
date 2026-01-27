import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentResponse } from '@/types/payment';

export const usePaymentManagement = () => {
    const { loading, error, clearError } = usePayment();
    const [activePayments, setActivePayments] = useState<PaymentResponse[]>([]);

    const getPaymentForBooking = useCallback(async (bookingId: number): Promise<PaymentResponse | null> => {
        clearError();
        try {
            const allPayments = activePayments.filter(p => p.bookingId === bookingId);
            if (allPayments.length > 0) {
                return allPayments[allPayments.length - 1];
            }
            return null;
        } catch {
            return null;
        }
    }, [activePayments, clearError]);

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

    return {
        loading,
        error,
        activePayments,
        getPaymentForBooking,
        addPaymentToActive,
        removePaymentFromActive,
        getPendingPayments,
        getFailedPayments,
        getSuccessfulPayments,
        canRetryPayment,
        clearError,
        hasActivePayments: activePayments.length > 0
    };
};