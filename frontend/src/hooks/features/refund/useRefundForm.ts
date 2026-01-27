import { useState, useCallback } from 'react';
import { useRefund } from './useRefund';
import type { RefundRequest, RefundResponse } from '@/types/refund';

export const useRefundForm = () => {
    const { processRefund, loading, error, clearError } = useRefund();
    const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);
    const [success, setSuccess] = useState(false);

    const handleProcessRefund = useCallback(async (request: RefundRequest): Promise<RefundResponse | null> => {
        clearError();
        setSuccess(false);
        setRefundResult(null);
        try {
            const result = await processRefund(request);
            setRefundResult(result);
            setSuccess(true);
            return result;
        } catch {
            return null;
        }
    }, [processRefund, clearError]);

    const reset = useCallback(() => {
        setRefundResult(null);
        setSuccess(false);
        clearError();
    }, [clearError]);

    const getDefaultRefundRequest = useCallback((): RefundRequest => ({
        ticketId: 0,
        reason: ''
    }), []);

    const isRefundInProgress = useCallback((refund: RefundResponse | null): boolean => {
        if (!refund) return false;
        return refund.status === 'PENDING' || refund.status === 'APPROVED';
    }, []);

    const isRefundCompleted = useCallback((refund: RefundResponse | null): boolean => {
        if (!refund) return false;
        return refund.status === 'PROCESSED';
    }, []);

    const getRefundStatusMessage = useCallback((refund: RefundResponse | null): string => {
        if (!refund) return '';

        switch (refund.status) {
            case 'PENDING':
                return 'Your refund request is being reviewed';
            case 'APPROVED':
                return 'Refund approved, processing payment';
            case 'PROCESSED':
                return `Refund processed. Amount: ${refund.totalAmount}`;
            case 'REJECTED':
                return 'Refund request was rejected';
            case 'CANCELLED':
                return 'Refund request was cancelled';
            default:
                return '';
        }
    }, []);

    return {
        loading,
        error,
        success,
        refundResult,
        handleProcessRefund,
        reset,
        getDefaultRefundRequest,
        isRefundInProgress,
        isRefundCompleted,
        getRefundStatusMessage,
        isSubmitting: loading
    };
};