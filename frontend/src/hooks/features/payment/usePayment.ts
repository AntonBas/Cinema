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
        return createPaymentApi.callApi(
            () => paymentApi.create(request),
            {
                successMessage: 'Payment initialized successfully',
            }
        );
    }, [createPaymentApi]);

    const getById = useCallback(async (paymentId: number) => {
        return paymentByIdApi.callApi(
            () => paymentApi.getById(paymentId),
            {
                cacheKey: `payment_${paymentId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [paymentByIdApi]);

    const getLiqPayData = useCallback(async (paymentId: number) => {
        return liqPayDataApi.callApi(
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

    return {
        payment: paymentByIdApi.data,
        liqPayData: liqPayDataApi.data,

        loading: paymentByIdApi.state.isLoading || liqPayDataApi.state.isLoading ||
            createPaymentApi.state.isLoading,
        error: paymentByIdApi.state.isError || liqPayDataApi.state.isError ||
            createPaymentApi.state.isError,

        create,
        getById,
        getLiqPayData,
        clearCache,

        resetPayment: paymentByIdApi.reset,
        resetLiqPayData: liqPayDataApi.reset,
        resetCreatePayment: createPaymentApi.reset,
        refetchPayment: paymentByIdApi.refetch,
        refetchLiqPayData: liqPayDataApi.refetch,
    };
};