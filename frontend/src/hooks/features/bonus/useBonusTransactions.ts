import { useState, useCallback, useEffect } from 'react';
import { useBonus } from './useBonus';
import type { BonusTransactionResponse } from '@/types/bonus';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseBonusTransactionsOptions {
    initialPage?: number;
    pageSize?: number;
    autoFetch?: boolean;
}

export const useBonusTransactions = (options: UseBonusTransactionsOptions = {}) => {
    const { initialPage = 0, pageSize = 20, autoFetch = true } = options;
    const { getMyTransactions, loading, error, clearError } = useBonus();

    const [transactions, setTransactions] = useState<BonusTransactionResponse[]>([]);
    const [pageData, setPageData] = useState<PageResponse<BonusTransactionResponse> | null>(null);
    const [isFetching, setIsFetching] = useState(false);

    const fetchTransactions = useCallback(async (page: number = initialPage) => {
        setIsFetching(true);
        clearError();
        try {
            const params: SearchParams = {
                page,
                size: pageSize
            };
            const response = await getMyTransactions(params);
            setTransactions(response.content);
            setPageData(response);
            return response;
        } catch {
            return null;
        } finally {
            setIsFetching(false);
        }
    }, [getMyTransactions, pageSize, initialPage, clearError]);

    useEffect(() => {
        if (autoFetch) {
            fetchTransactions(initialPage);
        }
    }, [fetchTransactions, autoFetch, initialPage]);

    const loadPage = useCallback(async (page: number) => {
        return await fetchTransactions(page);
    }, [fetchTransactions]);

    const nextPage = useCallback(async () => {
        if (pageData && !pageData.last) {
            return await loadPage(pageData.number + 1);
        }
        return null;
    }, [pageData, loadPage]);

    const prevPage = useCallback(async () => {
        if (pageData && !pageData.first) {
            return await loadPage(pageData.number - 1);
        }
        return null;
    }, [pageData, loadPage]);

    const refresh = useCallback(async () => {
        if (pageData) {
            return await fetchTransactions(pageData.number);
        }
        return await fetchTransactions(initialPage);
    }, [fetchTransactions, pageData, initialPage]);

    const getTransactionTypeDisplay = useCallback((type: string): string => {
        const displayMap: Record<string, string> = {
            'WELCOME_BONUS': 'Welcome Bonus',
            'BIRTHDAY_BONUS': 'Birthday Bonus',
            'PROMOTION_BONUS': 'Promotion Bonus',
            'BOOKING_SPEND': 'Booking Spend',
            'PAYMENT_ACCRUAL': 'Payment Accrual',
            'REFUND_RETURN': 'Refund Return',
            'BOOKING_CANCEL': 'Booking Cancel'
        };
        return displayMap[type] || type;
    }, []);

    const getTransactionTypeColor = useCallback((type: string): string => {
        const colorMap: Record<string, string> = {
            'WELCOME_BONUS': 'success',
            'BIRTHDAY_BONUS': 'primary',
            'PROMOTION_BONUS': 'info',
            'BOOKING_SPEND': 'warning',
            'PAYMENT_ACCRUAL': 'success',
            'REFUND_RETURN': 'info',
            'BOOKING_CANCEL': 'error'
        };
        return colorMap[type] || 'default';
    }, []);

    const isPositiveTransaction = useCallback((transaction: BonusTransactionResponse): boolean => {
        return transaction.pointsChange > 0;
    }, []);

    const filterByType = useCallback((type: string) => {
        return transactions.filter(transaction => transaction.type === type);
    }, [transactions]);

    const getTransactionSummary = useCallback(() => {
        const totalEarned = transactions
            .filter(t => t.pointsChange > 0)
            .reduce((sum, t) => sum + t.pointsChange, 0);

        const totalSpent = transactions
            .filter(t => t.pointsChange < 0)
            .reduce((sum, t) => sum + Math.abs(t.pointsChange), 0);

        return {
            totalEarned,
            totalSpent,
            netChange: totalEarned - totalSpent
        };
    }, [transactions]);

    const currentPage = pageData?.number || 0;
    const totalPages = pageData?.totalPages || 0;
    const totalElements = pageData?.totalElements || 0;
    const currentPageSize = pageData?.size || pageSize;

    return {
        transactions,
        pageData,

        loading: loading || isFetching,
        error,

        loadPage,
        nextPage,
        prevPage,
        refresh,

        getTransactionTypeDisplay,
        getTransactionTypeColor,
        isPositiveTransaction,
        filterByType,
        getTransactionSummary,

        hasTransactions: transactions.length > 0,
        isEmpty: pageData?.empty || false,
        currentPage,
        totalPages,
        totalElements,
        pageSize: currentPageSize,
        isFirstPage: pageData?.first || true,
        isLastPage: pageData?.last || true,
        canGoNext: pageData ? !pageData.last : false,
        canGoPrev: pageData ? !pageData.first : false
    };
};