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
    const paymentApiHook = useApi<PaymentResponse>();
    const liqPayDataApi = useApi<PaymentLiqPayDataResponse>();
    const mutationApi = useApi<PaymentResponse>();

    const loading = useDelayedLoading(
        paymentApiHook.loading || liqPayDataApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const create = useCallback(async (request: PaymentCreateRequest) => {
        return mutationApi.execute(
            () => paymentApi.create(request),
            { successMessage: 'Payment initialized successfully' }
        );
    }, [mutationApi]);

    const getById = useCallback(async (paymentId: number) => {
        return paymentApiHook.execute(() => paymentApi.getById(paymentId));
    }, [paymentApiHook]);

    const getLiqPayData = useCallback(async (paymentId: number) => {
        return liqPayDataApi.execute(() => paymentApi.getLiqPayData(paymentId));
    }, [liqPayDataApi]);

    return {
        payment: paymentApiHook.data,
        liqPayData: liqPayDataApi.data,
        loading,
        paymentError: paymentApiHook.error,
        liqPayError: liqPayDataApi.error,
        mutationError: mutationApi.error,
        create,
        getById,
        getLiqPayData,
    };
};