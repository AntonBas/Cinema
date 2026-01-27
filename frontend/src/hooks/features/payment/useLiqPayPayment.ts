import { useState, useCallback, useRef } from 'react';
import { usePayment } from './usePayment';
import type { PaymentLiqPayDataResponse, PaymentResponse } from '@/types/payment';

export const useLiqPayPayment = () => {
    const { getLiqPayData, getById, loading, error, clearError } = usePayment();

    const [liqPayData, setLiqPayData] = useState<PaymentLiqPayDataResponse | null>(null);
    const [paymentStatus, setPaymentStatus] = useState<PaymentResponse | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);

    const initializePayment = useCallback(async (paymentId: number): Promise<PaymentLiqPayDataResponse | null> => {
        setIsProcessing(true);
        clearError();
        try {
            const data = await getLiqPayData(paymentId);
            setLiqPayData(data);

            const status = await getById(paymentId);
            setPaymentStatus(status);

            return data;
        } catch {
            return null;
        } finally {
            setIsProcessing(false);
        }
    }, [getLiqPayData, getById, clearError]);

    const checkPaymentStatus = useCallback(async (paymentId: number): Promise<PaymentResponse | null> => {
        clearError();
        try {
            const status = await getById(paymentId);
            setPaymentStatus(status);
            return status;
        } catch {
            return null;
        }
    }, [getById, clearError]);

    const startStatusPolling = useCallback((paymentId: number, intervalMs: number = 5000) => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
        }

        const interval = setInterval(async () => {
            const status = await checkPaymentStatus(paymentId);

            if (status && (status.status === 'SUCCESS' || status.status === 'FAILED' || status.status === 'CANCELLED' || status.status === 'EXPIRED')) {
                if (pollingIntervalRef.current) {
                    clearInterval(pollingIntervalRef.current);
                    pollingIntervalRef.current = null;
                }
            }
        }, intervalMs);

        pollingIntervalRef.current = interval;
        return interval;
    }, [checkPaymentStatus]);

    const stopStatusPolling = useCallback(() => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    }, []);

    const getPaymentTimeLeft = useCallback((): string | null => {
        if (!paymentStatus || !paymentStatus.createdAt) return null;

        const createdAt = new Date(paymentStatus.createdAt);
        const now = new Date();
        const expiresAt = new Date(createdAt.getTime() + 30 * 60 * 1000);
        const diffMs = expiresAt.getTime() - now.getTime();

        if (diffMs <= 0) return 'Expired';

        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        return `${diffMinutes} minutes`;
    }, [paymentStatus]);

    const openLiqPayPopup = useCallback((): void => {
        if (!liqPayData) return;

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'https://www.liqpay.ua/api/3/checkout';
        form.target = '_blank';

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
        document.body.removeChild(form);
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
        setPaymentStatus(null);
        stopStatusPolling();
        clearError();
    }, [stopStatusPolling, clearError]);

    const isPaymentInProgress = useCallback((): boolean => {
        if (!paymentStatus) return false;
        return paymentStatus.status === 'PENDING' || paymentStatus.status === 'PROCESSING';
    }, [paymentStatus]);

    const isPaymentComplete = useCallback((): boolean => {
        if (!paymentStatus) return false;
        return paymentStatus.status === 'SUCCESS';
    }, [paymentStatus]);

    return {
        loading: loading || isProcessing,
        error,
        liqPayData,
        paymentStatus,
        initializePayment,
        checkPaymentStatus,
        startStatusPolling,
        stopStatusPolling,
        getPaymentTimeLeft,
        openLiqPayPopup,
        getLiqPayFormData,
        reset,
        isPaymentInProgress,
        isPaymentComplete,
        hasLiqPayData: !!liqPayData,
        hasPaymentStatus: !!paymentStatus
    };
};