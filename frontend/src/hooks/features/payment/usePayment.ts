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
    const getPaymentByIdApi = useApi<PaymentResponse>();
    const getLiqPayDataApi = useApi<PaymentLiqPayDataResponse>();
    const createPaymentApi = useApi<PaymentResponse>();

    const rawLoading = getPaymentByIdApi.loading || getLiqPayDataApi.loading || createPaymentApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getPaymentByIdApi.error || getLiqPayDataApi.error || createPaymentApi.error);

    const create = useCallback(async (request: PaymentCreateRequest) => {
        const response = await createPaymentApi.execute(
            () => paymentApi.create(request),
            { successMessage: 'Payment initialized successfully' }
        );
        return response || null;
    }, [createPaymentApi]);

    const getById = useCallback(async (paymentId: number) => {
        const response = await getPaymentByIdApi.execute(
            () => paymentApi.getById(paymentId),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPaymentByIdApi]);

    const getLiqPayData = useCallback(async (paymentId: number) => {
        const response = await getLiqPayDataApi.execute(
            () => paymentApi.getLiqPayData(paymentId),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getLiqPayDataApi]);

    const resetAll = useCallback(() => {
        getPaymentByIdApi.reset();
        getLiqPayDataApi.reset();
        createPaymentApi.reset();
    }, [getPaymentByIdApi, getLiqPayDataApi, createPaymentApi]);

    return {
        payment: getPaymentByIdApi.data,
        liqPayData: getLiqPayDataApi.data,
        loading,
        error,
        create,
        getById,
        getLiqPayData,
        resetAll,
    };
};