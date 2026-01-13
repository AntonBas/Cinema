import { useState, useCallback } from 'react';
import { bonusApi } from '@/api/bonusApi';
import type {
    BonusRulesResponse,
    BonusTransactionResponse,
    BonusRulesRequest,
    BonusTransactionType
} from '@/types/bonus';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useAdminBonus = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAllRules = useCallback(async (): Promise<BonusRulesResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.admin.getAllRules();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch bonus rules';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getRuleByType = useCallback(async (type: BonusTransactionType): Promise<BonusRulesResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.admin.getRuleByType(type);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch bonus rule: ${type}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest): Promise<BonusRulesResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.admin.updateRule(type, request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to update bonus rule: ${type}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const resetRule = useCallback(async (type: BonusTransactionType): Promise<BonusRulesResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bonusApi.admin.resetRule(type);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to reset bonus rule: ${type}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUserTransactions = useCallback(async (userId: number, params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const page = params?.page;
            const size = params?.size || 20;
            return await bonusApi.admin.getUserTransactions(userId, page, size);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch transactions for user: ${userId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getAllTransactions = useCallback(async (params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const page = params?.page;
            const size = params?.size || 20;
            return await bonusApi.admin.getAllTransactions(page, size);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch all transactions';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getTransactionsByType = useCallback(async (type: BonusTransactionType, params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const page = params?.page;
            const size = params?.size || 20;
            return await bonusApi.admin.getTransactionsByType(type, page, size);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch transactions by type: ${type}`;
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
        getAllRules,
        getRuleByType,
        updateRule,
        resetRule,
        getUserTransactions,
        getAllTransactions,
        getTransactionsByType,
        clearError
    };
};