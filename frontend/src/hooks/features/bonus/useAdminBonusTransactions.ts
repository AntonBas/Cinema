import { useState, useCallback, useEffect } from 'react';
import { useAdminBonus } from './useAdminBonus';
import type { BonusTransactionResponse, BonusTransactionType } from '@/types/bonus';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseAdminBonusTransactionsOptions {
    initialPage?: number;
    pageSize?: number;
    type?: BonusTransactionType;
    userId?: number;
    autoFetch?: boolean;
}

export const useAdminBonusTransactions = (options: UseAdminBonusTransactionsOptions = {}) => {
    const {
        initialPage = 0,
        pageSize = 20,
        type,
        userId,
        autoFetch = true
    } = options;

    const {
        getUserTransactions,
        getAllTransactions,
        getTransactionsByType,
        loading,
        error,
        clearError
    } = useAdminBonus();

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

            let response: PageResponse<BonusTransactionResponse>;

            if (userId) {
                response = await getUserTransactions(userId, params);
            } else if (type) {
                response = await getTransactionsByType(type, params);
            } else {
                response = await getAllTransactions(params);
            }

            setTransactions(response.content);
            setPageData(response);
            return response;
        } catch {
            return null;
        } finally {
            setIsFetching(false);
        }
    }, [getUserTransactions, getAllTransactions, getTransactionsByType, userId, type, pageSize, initialPage, clearError]);

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
            return await loadPage(pageData.currentPage + 1);
        }
        return null;
    }, [pageData, loadPage]);

    const prevPage = useCallback(async () => {
        if (pageData && !pageData.first) {
            return await loadPage(pageData.currentPage - 1);
        }
        return null;
    }, [pageData, loadPage]);

    const refresh = useCallback(async () => {
        if (pageData) {
            return await fetchTransactions(pageData.currentPage);
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
        getTransactionSummary,
        hasTransactions: transactions.length > 0,
        isEmpty: pageData?.empty || false,
        currentPage: pageData?.currentPage || 0,
        totalPages: pageData?.totalPages || 0,
        totalElements: pageData?.totalElements || 0,
        pageSize: pageData?.pageSize || pageSize,
        isFirstPage: pageData?.first || true,
        isLastPage: pageData?.last || true,
        canGoNext: pageData ? !pageData.last : false,
        canGoPrev: pageData ? !pageData.first : false,
        filterType: type,
        filterUserId: userId
    };
};