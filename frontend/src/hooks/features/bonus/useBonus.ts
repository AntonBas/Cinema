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
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface TransactionParams extends SearchParams {
    userId?: number;
    type?: BonusTransactionType;
}

export const useBonus = () => {
    const userApi = useApi<BonusCardResponse | BonusBalanceResponse | PageResponse<BonusTransactionResponse>>();
    const rulesApi = useApi<BonusRulesResponse[]>();
    const ruleApi = useApi<BonusRulesResponse>();
    const transactionsApi = useApi<PageResponse<BonusTransactionResponse>>();

    const rawLoading = userApi.loading || rulesApi.loading || ruleApi.loading || transactionsApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(userApi.error || rulesApi.error || ruleApi.error || transactionsApi.error);

    const getMyCard = useCallback(async () => {
        const response = await userApi.execute(() => bonusApi.user.getMyCard(), {
            cacheKey: 'my_bonus_card',
            cacheTime: 2 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [userApi]);

    const getMyBalance = useCallback(async () => {
        const response = await userApi.execute(() => bonusApi.user.getMyBalance(), {
            cacheKey: 'my_bonus_balance',
            cacheTime: 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [userApi]);

    const getMyTransactions = useCallback(async (params?: SearchParams) => {
        const response = await userApi.execute(() => bonusApi.user.getMyTransactions(params), {
            cacheKey: `my_transactions_${JSON.stringify(params)}`,
            cacheTime: 30 * 1000,
            showErrorNotification: true,
        });
        return response || null;
    }, [userApi]);

    const getAllRules = useCallback(async () => {
        const response = await rulesApi.execute(() => bonusApi.admin.getAllRules(), {
            cacheKey: 'bonus_rules',
            cacheTime: 5 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [rulesApi]);

    const getRuleByType = useCallback(async (type: BonusTransactionType) => {
        const response = await ruleApi.execute(() => bonusApi.admin.getRuleByType(type), {
            cacheKey: `bonus_rule_${type}`,
            cacheTime: 5 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [ruleApi]);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        const response = await ruleApi.execute(() => bonusApi.admin.updateRule(type, request), {
            successMessage: 'Bonus rule updated successfully',
        });
        rulesApi.invalidateCache();
        ruleApi.invalidateCache(`bonus_rule_${type}`);
        return response || null;
    }, [ruleApi, rulesApi]);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        const response = await ruleApi.execute(() => bonusApi.admin.resetRule(type), {
            successMessage: 'Bonus rule reset successfully',
        });
        rulesApi.invalidateCache();
        ruleApi.invalidateCache(`bonus_rule_${type}`);
        return response || null;
    }, [ruleApi, rulesApi]);

    const getTransactions = useCallback(async (params?: TransactionParams) => {
        const { userId, type, ...restParams } = params || {};

        let apiCall;
        if (userId) {
            apiCall = () => bonusApi.admin.getUserTransactions(userId, restParams);
        } else if (type) {
            apiCall = () => bonusApi.admin.getTransactionsByType(type, restParams);
        } else {
            apiCall = () => bonusApi.admin.getAllTransactions(restParams);
        }

        const cacheKey = `transactions_${userId ? `user_${userId}` : type ? `type_${type}` : 'all'}_${JSON.stringify(restParams)}`;

        const response = await transactionsApi.execute(apiCall, {
            cacheKey,
            cacheTime: 60 * 1000,
            showErrorNotification: true,
        });
        return response || null;
    }, [transactionsApi]);

    const clearCache = useCallback(() => {
        userApi.invalidateCache();
        rulesApi.invalidateCache();
        ruleApi.invalidateCache();
        transactionsApi.invalidateCache();
    }, [userApi, rulesApi, ruleApi, transactionsApi]);

    const resetAll = useCallback(() => {
        userApi.reset();
        rulesApi.reset();
        ruleApi.reset();
        transactionsApi.reset();
    }, [userApi, rulesApi, ruleApi, transactionsApi]);

    return {
        myCard: userApi.data as BonusCardResponse | null,
        myBalance: userApi.data as BonusBalanceResponse | null,
        myTransactions: (userApi.data as PageResponse<BonusTransactionResponse>)?.content || [],
        myTransactionsPagination: userApi.data as PageResponse<BonusTransactionResponse> | null,

        allRules: rulesApi.data || [],
        rule: ruleApi.data,

        transactions: transactionsApi.data?.content || [],
        transactionsPagination: transactionsApi.data,

        loading,
        error,

        getMyCard,
        getMyBalance,
        getMyTransactions,
        getAllRules,
        getRuleByType,
        updateRule,
        resetRule,
        getTransactions,

        clearCache,
        resetAll,

        totalPoints: (userApi.data as BonusBalanceResponse)?.pointsBalance || 0,
        hasCard: !!(userApi.data as BonusCardResponse)?.id,
    };
};