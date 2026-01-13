import { useState, useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type {
    RefundPreviewResponse,
    RefundResponse,
    RefundPreviewRequest,
    RefundRequest
} from '@/types/refund';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useRefund = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getPreview = useCallback(async (request: RefundPreviewRequest): Promise<RefundPreviewResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await refundApi.getPreview(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to get refund preview';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const processRefund = useCallback(async (request: RefundRequest): Promise<RefundResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await refundApi.processRefund(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to process refund';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUserRefunds = useCallback(async (): Promise<RefundResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await refundApi.getUserRefunds();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to get user refunds';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        loading,
        error,
        getPreview,
        processRefund,
        getUserRefunds,
        clearError
    };
};