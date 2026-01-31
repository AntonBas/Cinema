import { useState, useCallback } from 'react';
import { paymentApi } from '@/api/paymentApi';
import type { PaymentResponse, PaymentCreateRequest, PaymentLiqPayDataResponse } from '@/types/payment';
import { useApi } from '@/hooks/common/useApi';

export const usePayment = () => {
    const [activePayments, setActivePayments] = useState<PaymentResponse[]>([]);
    const [liqPayData, setLiqPayData] = useState<PaymentLiqPayDataResponse | null>(null);
    const [success, setSuccess] = useState(false);

    const createHook = useApi<PaymentResponse>();
    const getByIdHook = useApi<PaymentResponse>();
    const getLiqPayDataHook = useApi<PaymentLiqPayDataResponse>();

    const create = useCallback(async (request: PaymentCreateRequest): Promise<PaymentResponse> => {
        return createHook.callApi(async () => {
            const response = await paymentApi.create(request);
            setSuccess(true);
            setActivePayments(prev => [...prev, response]);
            return response;
        });
    }, [createHook]);

    const getById = useCallback(async (paymentId: number): Promise<PaymentResponse> => {
        return getByIdHook.callApi(async () => {
            return await paymentApi.getById(paymentId);
        });
    }, [getByIdHook]);

    const getLiqPayData = useCallback(async (paymentId: number): Promise<PaymentLiqPayDataResponse> => {
        return getLiqPayDataHook.callApi(async () => {
            const response = await paymentApi.getLiqPayData(paymentId);
            setLiqPayData(response);
            return response;
        });
    }, [getLiqPayDataHook]);

    const getPaymentForBooking = useCallback((bookingId: number): PaymentResponse | null => {
        const allPayments = activePayments.filter(p => p.bookingId === bookingId);
        if (allPayments.length > 0) {
            return allPayments[allPayments.length - 1];
        }
        return null;
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

    const redirectToLiqPay = useCallback((): void => {
        if (!liqPayData) return;

        if (liqPayData.paymentUrl && liqPayData.paymentUrl.includes('liqpay.ua')) {
            window.location.href = liqPayData.paymentUrl;
            return;
        }

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'https://www.liqpay.ua/api/3/checkout';

        const dataInput = document.createElement('input');
        dataInput.type = 'hidden';
        dataInput.name = 'data';
        dataInput.value = liqPayData.data;
        form.appendChild(dataInput);

        const signatureInput = document.createElement('input');
        signatureInput.type = 'hidden';
        signatureInput.name = 'signature';
        signatureInput.value = liqPayData.signature;
        form.appendChild(signatureInput);

        document.body.appendChild(form);
        form.submit();
    }, [liqPayData]);

    const getLiqPayFormData = useCallback((): { data: string; signature: string } | null => {
        if (!liqPayData) return null;
        return {
            data: liqPayData.data,
            signature: liqPayData.signature
        };
    }, [liqPayData]);

    const reset = useCallback(() => {
        setLiqPayData(null);
        setSuccess(false);
        setActivePayments([]);
    }, []);

    const isPaymentInProgress = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'PENDING' || payment.status === 'PROCESSING';
    }, []);

    const isPaymentComplete = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'SUCCESS';
    }, []);

    const isPaymentFailed = useCallback((payment: PaymentResponse | null): boolean => {
        if (!payment) return false;
        return payment.status === 'FAILED' || payment.status === 'CANCELLED' || payment.status === 'EXPIRED';
    }, []);

    const getPaymentErrorMessage = useCallback((payment: PaymentResponse | null): string | null => {
        if (!payment) return null;
        return payment.errorDescription || payment.errorCode || null;
    }, []);

    const getDefaultValues = useCallback((): PaymentCreateRequest => ({
        bookingId: 0
    }), []);

    return {
        loading: createHook.loading || getByIdHook.loading || getLiqPayDataHook.loading,
        success,
        liqPayData,
        activePayments,
        create,
        getById,
        getLiqPayData,
        getPaymentForBooking,
        addPaymentToActive,
        removePaymentFromActive,
        getPendingPayments,
        getFailedPayments,
        getSuccessfulPayments,
        canRetryPayment,
        redirectToLiqPay,
        getLiqPayFormData,
        reset,
        isPaymentInProgress,
        isPaymentComplete,
        isPaymentFailed,
        getPaymentErrorMessage,
        getDefaultValues,
        hasActivePayments: activePayments.length > 0,
        hasLiqPayData: !!liqPayData,
        isSubmitting: createHook.loading
    };
};