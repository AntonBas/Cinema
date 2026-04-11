import { useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useRefund = () => {
    const refundApiHook = useApi<RefundResponse>();

    const loading = useDelayedLoading(refundApiHook.loading, { delay: 150, minDisplayTime: 300 });

    const processRefund = useCallback(async (request: RefundRequest) => {
        return refundApiHook.execute(
            () => refundApi.processRefund(request),
            { successMessage: 'Refund request submitted successfully' }
        );
    }, [refundApiHook]);

    return {
        refundResult: refundApiHook.data,
        loading,
        error: refundApiHook.error,
        processRefund,
        reset: refundApiHook.reset,
    };
};