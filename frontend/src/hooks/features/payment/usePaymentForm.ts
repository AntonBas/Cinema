import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentCreateRequest, PaymentResponse } from '@/types/payment';

export const usePaymentForm = () => {
    const { create, loading, error, clearError } = usePayment();
    const [success, setSuccess] = useState(false);
    const [paymentResult, setPaymentResult] = useState<PaymentResponse | null>(null);

    const handleCreate = useCallback(async (data: PaymentCreateRequest): Promise<PaymentResponse | null> => {
        clearError();
        setSuccess(false);
        setPaymentResult(null);

        try {
            const result = await create(data);
            setSuccess(true);
            setPaymentResult(result);
            return result;
        } catch {
            return null;
        }
    }, [create, clearError]);

    const reset = useCallback(() => {
        setSuccess(false);
        setPaymentResult(null);
        clearError();
    }, [clearError]);

    const getDefaultValues = useCallback((): PaymentCreateRequest => ({
        bookingId: 0
    }), []);

    const isPaymentPending = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'PENDING' || payment.status === 'PROCESSING';
    }, []);

    const isPaymentSuccessful = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'SUCCESS';
    }, []);

    const isPaymentFailed = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'FAILED' || payment.status === 'CANCELLED' || payment.status === 'EXPIRED';
    }, []);

    const canRetryPayment = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'FAILED' || payment.status === 'CANCELLED';
    }, []);

    const getPaymentErrorMessage = useCallback((payment: PaymentResponse | null): string | null => {
        if (!payment) return null;
        return payment.errorDescription || payment.errorCode || null;
    }, []);

    return {
        loading,
        error,
        success,
        paymentResult,
        handleCreate,
        reset,
        getDefaultValues,
        isPaymentPending,
        isPaymentSuccessful,
        isPaymentFailed,
        canRetryPayment,
        getPaymentErrorMessage,
        isSubmitting: loading
    };
};