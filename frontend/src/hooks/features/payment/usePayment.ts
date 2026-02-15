import { useCallback } from 'react';
import { paymentApi } from '@/api/paymentApi';
import type {
    PaymentResponse,
    PaymentCreateRequest,
    PaymentLiqPayDataResponse
} from '@/types/payment';
import { useApi } from '@/hooks/common/useApi';

export const usePayment = () => {
    const paymentByIdApi = useApi<PaymentResponse>();
    const liqPayDataApi = useApi<PaymentLiqPayDataResponse>();
    const createPaymentApi = useApi<PaymentResponse>();

    const create = useCallback(async (request: PaymentCreateRequest) => {
        return createPaymentApi.execute(
            () => paymentApi.create(request),
            {
                successMessage: 'Payment initialized successfully',
            }
        );
    }, [createPaymentApi]);

    const getById = useCallback(async (paymentId: number) => {
        return paymentByIdApi.execute(
            () => paymentApi.getById(paymentId),
            {
                cacheKey: `payment_${paymentId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [paymentByIdApi]);

    const getLiqPayData = useCallback(async (paymentId: number) => {
        return liqPayDataApi.execute(
            () => paymentApi.getLiqPayData(paymentId),
            {
                cacheKey: `liqpay_data_${paymentId}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [liqPayDataApi]);

    const clearCache = useCallback(() => {
        paymentByIdApi.invalidateCache();
        liqPayDataApi.invalidateCache();
        createPaymentApi.invalidateCache();
    }, [paymentByIdApi, liqPayDataApi, createPaymentApi]);

    const loading = paymentByIdApi.loading || liqPayDataApi.loading ||
        createPaymentApi.loading;

    const error = !!(paymentByIdApi.error || liqPayDataApi.error ||
        createPaymentApi.error);

    return {
        payment: paymentByIdApi.data,
        liqPayData: liqPayDataApi.data,

        loading,
        error,

        create,
        getById,
        getLiqPayData,
        clearCache,

        resetPayment: paymentByIdApi.reset,
        resetLiqPayData: liqPayDataApi.reset,
        resetCreatePayment: createPaymentApi.reset,
    };
};