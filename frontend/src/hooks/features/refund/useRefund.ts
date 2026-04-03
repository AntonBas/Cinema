import { useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useRefund = () => {
    const processRefundApi = useApi<RefundResponse>();

    const loading = useDelayedLoading(processRefundApi.loading, { delay: 150, minDisplayTime: 300 });
    const error = !!processRefundApi.error;

    const processRefund = useCallback(async (request: RefundRequest) => {
        const response = await processRefundApi.execute(
            () => refundApi.processRefund(request),
            { successMessage: 'Refund request submitted successfully' }
        );
        return response || null;
    }, [processRefundApi]);

    const reset = useCallback(() => {
        processRefundApi.reset();
    }, [processRefundApi]);

    return {
        refundResult: processRefundApi.data,
        loading,
        error,
        isSubmitting: loading,
        isSuccess: !!(processRefundApi.data && !loading && !error),
        processRefund,
        reset,
    };
};