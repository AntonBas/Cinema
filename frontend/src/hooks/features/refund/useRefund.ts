import { useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useRefund = () => {
    const refundApiInstance = useApi<RefundResponse>();

    const loading = useDelayedLoading(refundApiInstance.loading, { delay: 150, minDisplayTime: 300 });
    const error = !!refundApiInstance.error;

    const processRefund = useCallback(async (request: RefundRequest) => {
        const response = await refundApiInstance.execute(
            () => refundApi.processRefund(request),
            {
                successMessage: 'Refund request submitted successfully',
            }
        );
        return response || null;
    }, [refundApiInstance]);

    const clearCache = useCallback(() => {
        refundApiInstance.invalidateCache();
    }, [refundApiInstance]);

    const reset = useCallback(() => {
        refundApiInstance.reset();
    }, [refundApiInstance]);

    return {
        refundResult: refundApiInstance.data,

        loading,
        error,
        isSubmitting: loading,
        isSuccess: !!(refundApiInstance.data && !loading && !error),

        processRefund,
        clearCache,
        reset,
    };
};