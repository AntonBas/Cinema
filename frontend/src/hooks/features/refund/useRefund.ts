import { useCallback, useRef } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useRefund = () => {
    const refundApiHook = useApi<RefundResponse>();
    const refundApiRef = useRef(refundApiHook);
    refundApiRef.current = refundApiHook;

    const loading = useDelayedLoading(refundApiHook.loading, { delay: 150, minDisplayTime: 300 });

    const processRefund = useCallback(async (request: RefundRequest) => {
        return refundApiRef.current.execute(
            () => refundApi.processRefund(request),
            { successMessage: 'Refund request submitted successfully' }
        );
    }, []);

    return {
        refundResult: refundApiHook.data,
        loading,
        error: refundApiHook.error,
        processRefund,
        reset: refundApiHook.reset,
    };
};