import { useState, useCallback } from 'react';
import { useRefund } from './useRefund';
import type { RefundPreviewRequest, RefundRequest, RefundPreviewResponse, RefundResponse } from '@/types/refund';

export const useRefundForm = () => {
    const { getPreview, processRefund, loading, error, clearError } = useRefund();
    const [previewResult, setPreviewResult] = useState<RefundPreviewResponse | null>(null);
    const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);
    const [success, setSuccess] = useState(false);

    const handleGetPreview = useCallback(async (request: RefundPreviewRequest): Promise<RefundPreviewResponse | null> => {
        clearError();
        setPreviewResult(null);
        try {
            const result = await getPreview(request);
            setPreviewResult(result);
            return result;
        } catch {
            return null;
        }
    }, [getPreview, clearError]);

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
        setPreviewResult(null);
        setRefundResult(null);
        setSuccess(false);
        clearError();
    }, [clearError]);

    const getDefaultPreviewRequest = useCallback((): RefundPreviewRequest => ({
        ticketId: 0
    }), []);

    const getDefaultRefundRequest = useCallback((): RefundRequest => ({
        ticketId: 0,
        reason: ''
    }), []);

    const calculateRefundEligibility = useCallback((preview: RefundPreviewResponse | null): {
        isRefundable: boolean;
        refundPercentage: number;
        netAmount: number;
        hasFees: boolean;
    } => {
        if (!preview) {
            return {
                isRefundable: false,
                refundPercentage: 0,
                netAmount: 0,
                hasFees: false
            };
        }

        const refundAmount = parseFloat(preview.refundAmount) || 0;
        const feeAmount = parseFloat(preview.feeAmount) || 0;
        const netAmount = refundAmount - feeAmount;
        const refundPercentage = parseFloat(preview.refundPercentage) || 0;

        return {
            isRefundable: preview.isRefundable,
            refundPercentage,
            netAmount,
            hasFees: feeAmount > 0
        };
    }, []);

    const formatCurrency = useCallback((amount: string): string => {
        const num = parseFloat(amount) || 0;
        return new Intl.NumberFormat('uk-UA', {
            style: 'currency',
            currency: 'UAH',
            minimumFractionDigits: 2
        }).format(num);
    }, []);

    const formatPercentage = useCallback((percentage: string): string => {
        const num = parseFloat(percentage) || 0;
        return `${num}%`;
    }, []);

    const getRemainingTimeDisplay = useCallback((remainingTime?: string): string => {
        if (!remainingTime) return 'N/A';

        const parts = remainingTime.split(':');
        if (parts.length !== 3) return remainingTime;

        const hours = parseInt(parts[0]);
        const minutes = parseInt(parts[1]);

        if (hours > 0) {
            return `${hours}h ${minutes}m`;
        }
        return `${minutes} minutes`;
    }, []);

    const isRefundInProgress = useCallback((refund: RefundResponse | null): boolean => {
        if (!refund) return false;
        return refund.status === 'PENDING' || refund.status === 'APPROVED';
    }, []);

    const isRefundCompleted = useCallback((refund: RefundResponse | null): boolean => {
        if (!refund) return false;
        return refund.status === 'PROCESSED';
    }, []);

    const isRefundFailed = useCallback((refund: RefundResponse | null): boolean => {
        if (!refund) return false;
        return refund.status === 'REJECTED' || refund.status === 'CANCELLED';
    }, []);

    const getRefundStatusMessage = useCallback((refund: RefundResponse | null): string => {
        if (!refund) return '';

        switch (refund.status) {
            case 'PENDING':
                return 'Your refund request is being reviewed';
            case 'APPROVED':
                return 'Refund approved, processing payment';
            case 'PROCESSED':
                return `Refund processed. Amount: ${formatCurrency(refund.totalAmount)}`;
            case 'REJECTED':
                return 'Refund request was rejected';
            case 'CANCELLED':
                return 'Refund request was cancelled';
            default:
                return '';
        }
    }, [formatCurrency]);

    return {
        loading,
        error,
        success,
        previewResult,
        refundResult,
        handleGetPreview,
        handleProcessRefund,
        reset,
        getDefaultPreviewRequest,
        getDefaultRefundRequest,
        calculateRefundEligibility,
        formatCurrency,
        formatPercentage,
        getRemainingTimeDisplay,
        isRefundInProgress,
        isRefundCompleted,
        isRefundFailed,
        getRefundStatusMessage,
        isSubmitting: loading
    };
};