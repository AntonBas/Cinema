import { useCallback } from 'react';
import { paymentApi } from '@/api/paymentApi';
import type {
    PaymentResponse,
    PaymentCreateRequest,
    PaymentLiqPayDataResponse
} from '@/types/payment';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const usePayment = () => {
    const paymentApiInstance = useApi<PaymentResponse>();
    const liqPayDataApi = useApi<PaymentLiqPayDataResponse>();
    const mutationApi = useApi<PaymentResponse>();

    const rawLoading = paymentApiInstance.loading || liqPayDataApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(paymentApiInstance.error || liqPayDataApi.error || mutationApi.error);

    const create = useCallback(async (request: PaymentCreateRequest) => {
        const response = await mutationApi.execute(
            () => paymentApi.create(request),
            {
                successMessage: 'Payment initialized successfully',
            }
        );
        return response || null;
    }, [mutationApi]);

    const getById = useCallback(async (paymentId: number) => {
        const response = await paymentApiInstance.execute(
            () => paymentApi.getById(paymentId),
            {
                cacheKey: `payment_${paymentId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [paymentApiInstance]);

    const getLiqPayData = useCallback(async (paymentId: number) => {
        const response = await liqPayDataApi.execute(
            () => paymentApi.getLiqPayData(paymentId),
            {
                cacheKey: `liqpay_data_${paymentId}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [liqPayDataApi]);

    const clearCache = useCallback(() => {
        paymentApiInstance.invalidateCache();
        liqPayDataApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [paymentApiInstance, liqPayDataApi, mutationApi]);

    const resetAll = useCallback(() => {
        paymentApiInstance.reset();
        liqPayDataApi.reset();
        mutationApi.reset();
    }, [paymentApiInstance, liqPayDataApi, mutationApi]);

    return {
        payment: paymentApiInstance.data,
        liqPayData: liqPayDataApi.data,

        loading,
        error,

        create,
        getById,
        getLiqPayData,
        clearCache,
        resetAll,
    };
};