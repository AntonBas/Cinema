import { useCallback } from 'react';
import { bonusApi } from '@/api/bonusApi';
import type {
    BonusCardResponse,
    BonusBalanceResponse,
    BonusTransactionResponse,
    BonusRulesResponse,
    BonusRulesRequest,
    BonusTransactionType
} from '@/types/bonus';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useBonus = () => {
    const myCardApi = useApi<BonusCardResponse>();
    const myBalanceApi = useApi<BonusBalanceResponse>();
    const myTransactionsApi = useApi<PageResponse<BonusTransactionResponse>>();
    const allRulesApi = useApi<BonusRulesResponse[]>();

    const getMyCard = useCallback(async () => {
        return myCardApi.callApi(
            () => bonusApi.user.getMyCard(),
            {
                cacheKey: 'my_bonus_card',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [myCardApi]);

    const getMyBalance = useCallback(async () => {
        return myBalanceApi.callApi(
            () => bonusApi.user.getMyBalance(),
            {
                cacheKey: 'my_bonus_balance',
                cacheTime: 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [myBalanceApi]);

    const getMyTransactions = useCallback(async (params?: any) => {
        return myTransactionsApi.callApi(
            () => bonusApi.user.getMyTransactions(params),
            {
                cacheKey: `my_transactions_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [myTransactionsApi]);

    const getAllRules = useCallback(async () => {
        return allRulesApi.callApi(
            () => bonusApi.admin.getAllRules(),
            {
                cacheKey: 'bonus_rules',
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, [allRulesApi]);

    const getRuleByType = useCallback(async (type: BonusTransactionType) => {
        const api = useApi<BonusRulesResponse>();
        return api.callApi(
            () => bonusApi.admin.getRuleByType(type),
            {
                cacheKey: `bonus_rule_${type}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        const api = useApi<BonusRulesResponse>();
        return api.callApi(
            () => bonusApi.admin.updateRule(type, request),
            {
                successMessage: 'Bonus rule updated successfully',
                onSuccess: () => {
                    allRulesApi.invalidateCache();
                },
            }
        );
    }, [allRulesApi]);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        const api = useApi<BonusRulesResponse>();
        return api.callApi(
            () => bonusApi.admin.resetRule(type),
            {
                successMessage: 'Bonus rule reset successfully',
                onSuccess: () => {
                    allRulesApi.invalidateCache();
                },
            }
        );
    }, [allRulesApi]);

    const getUserTransactions = useCallback(async (userId: number, params?: any) => {
        const api = useApi<PageResponse<BonusTransactionResponse>>();
        return api.callApi(
            () => bonusApi.admin.getUserTransactions(userId, params),
            {
                cacheKey: `user_transactions_${userId}_${JSON.stringify(params)}`,
                cacheTime: 60 * 1000,
            }
        );
    }, []);

    const getAllTransactions = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<BonusTransactionResponse>>();
        return api.callApi(
            () => bonusApi.admin.getAllTransactions(params),
            {
                cacheKey: `all_transactions_${JSON.stringify(params)}`,
                cacheTime: 60 * 1000,
            }
        );
    }, []);

    const getTransactionsByType = useCallback(async (type: BonusTransactionType, params?: any) => {
        const api = useApi<PageResponse<BonusTransactionResponse>>();
        return api.callApi(
            () => bonusApi.admin.getTransactionsByType(type, params),
            {
                cacheKey: `transactions_type_${type}_${JSON.stringify(params)}`,
                cacheTime: 60 * 1000,
            }
        );
    }, []);

    const clearCache = useCallback(() => {
        myCardApi.invalidateCache();
        myBalanceApi.invalidateCache();
        myTransactionsApi.invalidateCache();
        allRulesApi.invalidateCache();
    }, [myCardApi, myBalanceApi, myTransactionsApi, allRulesApi]);

    return {
        myCard: myCardApi.data,
        myBalance: myBalanceApi.data,
        myTransactions: myTransactionsApi.data?.content || [],
        allRules: allRulesApi.data || [],

        loading: myCardApi.state.isLoading || myBalanceApi.state.isLoading ||
            myTransactionsApi.state.isLoading || allRulesApi.state.isLoading,
        error: myCardApi.state.isError || myBalanceApi.state.isError ||
            myTransactionsApi.state.isError || allRulesApi.state.isError,

        getMyCard,
        getMyBalance,
        getMyTransactions,
        getAllRules,
        getRuleByType,
        updateRule,
        resetRule,
        getUserTransactions,
        getAllTransactions,
        getTransactionsByType,
        clearCache,

        resetMyCard: myCardApi.reset,
        resetMyBalance: myBalanceApi.reset,
        resetMyTransactions: myTransactionsApi.reset,
        resetAllRules: allRulesApi.reset,
        refetchMyCard: myCardApi.refetch,
        refetchMyBalance: myBalanceApi.refetch,
        refetchMyTransactions: myTransactionsApi.refetch,
        refetchAllRules: allRulesApi.refetch,

        myTransactionsPagination: myTransactionsApi.data,
        totalPoints: myBalanceApi.data?.pointsBalance || 0,
        hasCard: !!myCardApi.data,
    };
};