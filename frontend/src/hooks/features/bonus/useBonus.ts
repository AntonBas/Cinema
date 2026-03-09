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

export const useBonus = () => {
    const cardApi = useApi<BonusCardResponse>();
    const balanceApi = useApi<BonusBalanceResponse>();
    const myTransactionsApi = useApi<PageResponse<BonusTransactionResponse>>();
    const rulesApi = useApi<BonusRulesResponse[]>();
    const ruleApi = useApi<BonusRulesResponse>();

    const rawLoading = cardApi.loading || balanceApi.loading || myTransactionsApi.loading ||
        rulesApi.loading || ruleApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(cardApi.error || balanceApi.error || myTransactionsApi.error ||
        rulesApi.error || ruleApi.error);

    const getMyCard = useCallback(async () => {
        const response = await cardApi.execute(() => bonusApi.user.getMyCard(), {
            cacheKey: 'my_bonus_card',
            cacheTime: 2 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [cardApi]);

    const getMyBalance = useCallback(async () => {
        const response = await balanceApi.execute(() => bonusApi.user.getMyBalance(), {
            cacheKey: 'my_bonus_balance',
            cacheTime: 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [balanceApi]);

    const getMyTransactions = useCallback(async (params?: SearchParams) => {
        const response = await myTransactionsApi.execute(() => bonusApi.user.getMyTransactions(params), {
            cacheKey: `my_transactions_${JSON.stringify(params)}`,
            cacheTime: 30 * 1000,
            showErrorNotification: true,
        });
        return response || null;
    }, [myTransactionsApi]);

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

    const clearCache = useCallback(() => {
        cardApi.invalidateCache();
        balanceApi.invalidateCache();
        myTransactionsApi.invalidateCache();
        rulesApi.invalidateCache();
        ruleApi.invalidateCache();
    }, [cardApi, balanceApi, myTransactionsApi, rulesApi, ruleApi]);

    const resetAll = useCallback(() => {
        cardApi.reset();
        balanceApi.reset();
        myTransactionsApi.reset();
        rulesApi.reset();
        ruleApi.reset();
    }, [cardApi, balanceApi, myTransactionsApi, rulesApi, ruleApi]);

    return {
        myCard: cardApi.data,
        myBalance: balanceApi.data,
        myTransactions: myTransactionsApi.data?.content || [],
        myTransactionsPagination: myTransactionsApi.data,

        allRules: rulesApi.data || [],
        rule: ruleApi.data,

        loading,
        error,

        getMyCard,
        getMyBalance,
        getMyTransactions,
        getAllRules,
        getRuleByType,
        updateRule,
        resetRule,

        clearCache,
        resetAll,

        totalPoints: balanceApi.data?.pointsBalance || 0,
        hasCard: !!cardApi.data?.id,
    };
};