import { useState, useCallback, useRef } from 'react';
import { refundApi } from '@/api/refundApi';
import type { RefundResponse, RefundRequest, RefundStatus } from '@/types/refund';
import { useApi } from '@/hooks/common/useApi';

export const useRefund = () => {
    const [refunds, setRefunds] = useState<RefundResponse[]>([]);
    const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);

    const apiHookRef = useRef(useApi<RefundResponse[]>());
    const apiHook = apiHookRef.current;

    const processRefundHook = useApi<RefundResponse>();

    const processRefund = useCallback(async (request: RefundRequest): Promise<RefundResponse> => {
        return processRefundHook.callApi(async () => {
            const response = await refundApi.processRefund(request);
            setRefundResult(response);
            setRefunds(prev => [...prev, response]);
            return response;
        }, { showErrorNotification: false });
    }, [processRefundHook]);

    const getUserRefunds = useCallback(async (): Promise<RefundResponse[]> => {
        return apiHook.callApi(async () => {
            const response = await fetch('/api/refunds', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch refunds');
            }

            const data = await response.json();
            setRefunds(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const fetchRefunds = useCallback(async (autoFetch = true) => {
        if (!autoFetch) return [];

        return apiHook.callApi(async () => {
            const response = await fetch('/api/refunds', {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch refunds');
            }

            const data = await response.json();
            setRefunds(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const getRefundByNumber = useCallback((refundNumber: string): RefundResponse | undefined => {
        return refunds.find(refund => refund.refundNumber === refundNumber);
    }, [refunds]);

    const getRefundById = useCallback((refundId: number): RefundResponse | undefined => {
        return refunds.find(refund => refund.id === refundId);
    }, [refunds]);

    const getRefundsByStatus = useCallback((status: RefundStatus): RefundResponse[] => {
        return refunds.filter(refund => refund.status === status);
    }, [refunds]);

    const getRefundsByTicketId = useCallback((ticketId: number): RefundResponse[] => {
        return refunds.filter(refund =>
            refund.items.some(item => item.ticketId === ticketId)
        );
    }, [refunds]);

    const getTotalRefundedAmount = useCallback((): number => {
        return refunds.reduce((total, refund) => {
            if (refund.status === 'PROCESSED') {
                const amount = parseFloat(refund.totalAmount) || 0;
                return total + amount;
            }
            return total;
        }, 0);
    }, [refunds]);

    const getPendingRefundsCount = useCallback((): number => {
        return refunds.filter(refund =>
            refund.status === 'PENDING' || refund.status === 'APPROVED'
        ).length;
    }, [refunds]);

    const sortByDate = useCallback((order: 'asc' | 'desc' = 'desc') => {
        return [...refunds].sort((a, b) => {
            const dateA = new Date(a.createdAt).getTime();
            const dateB = new Date(b.createdAt).getTime();
            return order === 'asc' ? dateA - dateB : dateB - dateA;
        });
    }, [refunds]);

    const getLatestRefund = useCallback((): RefundResponse | undefined => {
        if (refunds.length === 0) return undefined;
        return sortByDate('desc')[0];
    }, [refunds, sortByDate]);

    const getRefundSummary = useCallback(() => {
        const processed = refunds.filter(r => r.status === 'PROCESSED').length;
        const pending = refunds.filter(r => r.status === 'PENDING' || r.status === 'APPROVED').length;
        const rejected = refunds.filter(r => r.status === 'REJECTED' || r.status === 'CANCELLED').length;
        const totalAmount = getTotalRefundedAmount();

        return {
            total: refunds.length,
            processed,
            pending,
            rejected,
            totalAmount,
            successRate: refunds.length > 0 ? (processed / refunds.length) * 100 : 0
        };
    }, [refunds, getTotalRefundedAmount]);

    const hasRefundForTicket = useCallback((ticketId: number): boolean => {
        return refunds.some(refund =>
            refund.items.some(item => item.ticketId === ticketId)
        );
    }, [refunds]);

    const getRefundItemDetails = useCallback((refund: RefundResponse, ticketId: number) => {
        return refund.items.find(item => item.ticketId === ticketId);
    }, []);

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

    const reset = useCallback(() => {
        setRefundResult(null);
    }, []);

    return {
        refunds,
        refundResult,
        loading: processRefundHook.loading || apiHook.loading,
        processRefund,
        getUserRefunds,
        fetchRefunds,
        getRefundByNumber,
        getRefundById,
        getRefundsByStatus,
        getRefundsByTicketId,
        getTotalRefundedAmount,
        getPendingRefundsCount,
        sortByDate,
        getLatestRefund,
        getRefundSummary,
        hasRefundForTicket,
        getRefundItemDetails,
        getDefaultRefundRequest,
        isRefundInProgress,
        isRefundCompleted,
        getRefundStatusMessage,
        reset,
        isEmpty: refunds.length === 0,
        totalRefunds: refunds.length,
        isSubmitting: processRefundHook.loading
    };
};