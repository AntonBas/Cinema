import { useState, useCallback, useEffect } from 'react';
import { useRefund } from './useRefund';
import type { RefundResponse, RefundStatus } from '@/types/refund';

export const useRefundList = (options?: { autoFetch?: boolean }) => {
    const { autoFetch = true } = options || {};
    const { loading, error, clearError } = useRefund();
    const [refunds, setRefunds] = useState<RefundResponse[]>([]);
    const [localLoading, setLocalLoading] = useState(false);

    const fetchRefunds = useCallback(async () => {
        setLocalLoading(true);
        clearError();
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

            const data = await response.json();
            setRefunds(data);
            return data;
        } catch {
            return [];
        } finally {
            setLocalLoading(false);
        }
    }, [clearError]);

    useEffect(() => {
        if (autoFetch) {
            fetchRefunds();
        }
    }, [fetchRefunds, autoFetch]);

    const refresh = useCallback(async () => {
        return await fetchRefunds();
    }, [fetchRefunds]);

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

    return {
        refunds,
        loading: loading || localLoading,
        error,
        refresh,
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
        isEmpty: refunds.length === 0,
        totalRefunds: refunds.length
    };
};