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
    const rulesApi = useApi<BonusRulesResponse[]>();
    const ruleApi = useApi<BonusRulesResponse>();
    const transactionsApi = useApi<PageResponse<BonusTransactionResponse>>();

    const getMyCard = useCallback(async () => {
        return myCardApi.callApi(() => bonusApi.user.getMyCard(), {
            cacheKey: 'my_bonus_card',
            cacheTime: 2 * 60 * 1000,
            showErrorNotification: false,
        });
    }, [myCardApi]);

    const getMyBalance = useCallback(async () => {
        return myBalanceApi.callApi(() => bonusApi.user.getMyBalance(), {
            cacheKey: 'my_bonus_balance',
            cacheTime: 60 * 1000,
            showErrorNotification: false,
        });
    }, [myBalanceApi]);

    const getMyTransactions = useCallback(async (params?: { page?: number; size?: number }) => {
        return myTransactionsApi.callApi(() => bonusApi.user.getMyTransactions(params), {
            cacheKey: `my_transactions_${JSON.stringify(params)}`,
            cacheTime: 30 * 1000,
            showErrorNotification: false,
        });
    }, [myTransactionsApi]);

    const getAllRules = useCallback(async () => {
        return rulesApi.callApi(() => bonusApi.admin.getAllRules(), {
            cacheKey: 'bonus_rules',
            cacheTime: 5 * 60 * 1000,
        });
    }, [rulesApi]);

    const getRuleByType = useCallback(async (type: BonusTransactionType) => {
        return ruleApi.callApi(() => bonusApi.admin.getRuleByType(type), {
            cacheKey: `bonus_rule_${type}`,
            cacheTime: 5 * 60 * 1000,
        });
    }, [ruleApi]);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        return ruleApi.callApi(() => bonusApi.admin.updateRule(type, request), {
            successMessage: 'Bonus rule updated successfully',
            onSuccess: () => {
                rulesApi.invalidateCache();
                ruleApi.invalidateCache(`bonus_rule_${type}`);
            },
        });
    }, [ruleApi, rulesApi]);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        return ruleApi.callApi(() => bonusApi.admin.resetRule(type), {
            successMessage: 'Bonus rule reset successfully',
            onSuccess: () => {
                rulesApi.invalidateCache();
                ruleApi.invalidateCache(`bonus_rule_${type}`);
            },
        });
    }, [ruleApi, rulesApi]);

    const getUserTransactions = useCallback(async (userId: number, params?: { page?: number; size?: number }) => {
        return transactionsApi.callApi(() => bonusApi.admin.getUserTransactions(userId, params), {
            cacheKey: `user_transactions_${userId}_${JSON.stringify(params)}`,
            cacheTime: 60 * 1000,
        });
    }, [transactionsApi]);

    const getAllTransactions = useCallback(async (params?: { page?: number; size?: number }) => {
        return transactionsApi.callApi(() => bonusApi.admin.getAllTransactions(params), {
            cacheKey: `all_transactions_${JSON.stringify(params)}`,
            cacheTime: 60 * 1000,
        });
    }, [transactionsApi]);

    const getTransactionsByType = useCallback(async (type: BonusTransactionType, params?: { page?: number; size?: number }) => {
        return transactionsApi.callApi(() => bonusApi.admin.getTransactionsByType(type, params), {
            cacheKey: `transactions_type_${type}_${JSON.stringify(params)}`,
            cacheTime: 60 * 1000,
        });
    }, [transactionsApi]);

    const clearCache = useCallback(() => {
        myCardApi.invalidateCache();
        myBalanceApi.invalidateCache();
        myTransactionsApi.invalidateCache();
        rulesApi.invalidateCache();
        ruleApi.invalidateCache();
        transactionsApi.invalidateCache();
    }, [myCardApi, myBalanceApi, myTransactionsApi, rulesApi, ruleApi, transactionsApi]);

    const resetAll = useCallback(() => {
        myCardApi.reset();
        myBalanceApi.reset();
        myTransactionsApi.reset();
        rulesApi.reset();
        ruleApi.reset();
        transactionsApi.reset();
    }, [myCardApi, myBalanceApi, myTransactionsApi, rulesApi, ruleApi, transactionsApi]);

    return {
        data: {
            myCard: myCardApi.data,
            myBalance: myBalanceApi.data,
            myTransactions: myTransactionsApi.data?.content || [],
            allRules: rulesApi.data || [],
            rule: ruleApi.data,
            transactions: transactionsApi.data?.content || [],
        },
        pagination: {
            myTransactions: myTransactionsApi.data,
            transactions: transactionsApi.data,
        },
        loading: myCardApi.loading || myBalanceApi.loading ||
            myTransactionsApi.loading || rulesApi.loading ||
            ruleApi.loading || transactionsApi.loading,
        error: myCardApi.error || myBalanceApi.error ||
            myTransactionsApi.error || rulesApi.error ||
            ruleApi.error || transactionsApi.error,
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
        resetAll,
        refetch: {
            myCard: myCardApi.refetch,
            myBalance: myBalanceApi.refetch,
            myTransactions: myTransactionsApi.refetch,
            rules: rulesApi.refetch,
            rule: ruleApi.refetch,
            transactions: transactionsApi.refetch,
        },
        totalPoints: myBalanceApi.data?.pointsBalance || 0,
        hasCard: !!myCardApi.data,
    };
};