import { useState, useCallback } from 'react';
import { usePayment } from './usePayment';
import type { PaymentLiqPayDataResponse } from '@/types/payment';

export const useLiqPayPayment = () => {
    const { getLiqPayData, loading, error, clearError } = usePayment();

    const [liqPayData, setLiqPayData] = useState<PaymentLiqPayDataResponse | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const initializePayment = useCallback(async (paymentId: number): Promise<PaymentLiqPayDataResponse | null> => {
        setIsProcessing(true);
        clearError();
        try {
            const data = await getLiqPayData(paymentId);
            setLiqPayData(data);
            return data;
        } catch {
            return null;
        } finally {
            setIsProcessing(false);
        }
    }, [getLiqPayData, clearError]);

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
        clearError();
    }, [clearError]);

    return {
        loading: loading || isProcessing,
        error,
        liqPayData,
        initializePayment,
        openLiqPayPopup,
        getLiqPayFormData,
        reset,
        hasLiqPayData: !!liqPayData
    };
};