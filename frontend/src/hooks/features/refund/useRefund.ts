import { useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';

export const useRefund = () => {
    const processRefundApi = useApi<RefundResponse>();

    const processRefund = useCallback(async (request: RefundRequest) => {
        return processRefundApi.callApi(
            () => refundApi.processRefund(request),
            {
                successMessage: 'Refund request submitted successfully',
            }
        );
    }, [processRefundApi]);

    const clearRefundCache = useCallback(() => {
        processRefundApi.invalidateCache();
    }, [processRefundApi]);

    const resetProcess = useCallback(() => {
        processRefundApi.reset();
    }, [processRefundApi]);

    return {
        refundResult: processRefundApi.data,

        loading: processRefundApi.state.isLoading,
        error: processRefundApi.state.isError,
        isSubmitting: processRefundApi.state.isLoading,
        isSuccess: processRefundApi.state.isSuccess,

        processRefund,
        clearRefundCache,
        resetProcess,
        refetchRefund: processRefundApi.refetch,
    };
};