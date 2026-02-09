import { useCallback } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';

export const useRefund = () => {
    const refundsApi = useApi<RefundResponse[]>();
    const processRefundApi = useApi<RefundResponse>();

    const getRefunds = useCallback(async () => {
        return refundsApi.callApi(
            async () => {
                const response = await fetch('/api/refunds', {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch refunds');
                }

                return response.json();
            },
            {
                cacheKey: 'user_refunds',
                cacheTime: 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [refundsApi]);

    const processRefund = useCallback(async (request: RefundRequest) => {
        return processRefundApi.callApi(
            () => refundApi.processRefund(request),
            {
                successMessage: 'Refund request submitted successfully',
                onSuccess: (response) => {
                    refundsApi.updateData(current => current ? [...current, response] : [response]);
                },
            }
        );
    }, [processRefundApi, refundsApi]);

    const refreshRefunds = useCallback(async () => {
        return refundsApi.callApi(
            async () => {
                const response = await fetch('/api/refunds', {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                    }
                });

                if (!response.ok) {
                    throw new Error('Failed to fetch refunds');
                }

                return response.json();
            },
            {
                cacheKey: 'user_refunds',
                cacheTime: 0,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, [refundsApi]);

    const clearRefundCache = useCallback(() => {
        refundsApi.invalidateCache();
    }, [refundsApi]);

    return {
        refunds: refundsApi.data || [],
        refundResult: processRefundApi.data,

        loading: refundsApi.state.isLoading || processRefundApi.state.isLoading,
        error: refundsApi.state.isError || processRefundApi.state.isError,
        isSubmitting: processRefundApi.state.isLoading,

        getRefunds,
        processRefund,
        refreshRefunds,
        clearRefundCache,

        resetRefunds: refundsApi.reset,
        resetProcess: processRefundApi.reset,
        refetchRefunds: refundsApi.refetch,
    };
};