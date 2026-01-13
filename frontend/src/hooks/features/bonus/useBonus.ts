import { useState, useCallback } from 'react';
import { bonusApi } from '@/api/bonusApi';
import type {
    BonusCardResponse,
    BonusBalanceResponse,
    BonusTransactionResponse
} from '@/types/bonus';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useBonus = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getMyCard = useCallback(async (): Promise<BonusCardResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.user.getMyCard();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch bonus card';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getMyBalance = useCallback(async (): Promise<BonusBalanceResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.user.getMyBalance();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch bonus balance';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getMyTransactions = useCallback(async (params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const page = params?.page;
            const size = params?.size || 20;
            return await bonusApi.user.getMyTransactions(page, size);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch bonus transactions';
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
        getMyCard,
        getMyBalance,
        getMyTransactions,
        clearError
    };
};