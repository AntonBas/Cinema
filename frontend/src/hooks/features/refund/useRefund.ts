import { useState, useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useRefund = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

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
            const response = await fetch('/api/refunds', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch refunds');
            }

            return await response.json();
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to get user refunds';
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
        processRefund,
        getUserRefunds,
        clearError
    };
};