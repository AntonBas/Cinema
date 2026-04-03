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
    const getMyCardApi = useApi<BonusCardResponse>();
    const getMyBalanceApi = useApi<BonusBalanceResponse>();
    const getMyTransactionsApi = useApi<PageResponse<BonusTransactionResponse>>();
    const getAllRulesApi = useApi<BonusRulesResponse[]>();
    const getRuleByTypeApi = useApi<BonusRulesResponse>();
    const updateRuleApi = useApi<BonusRulesResponse>();
    const resetRuleApi = useApi<BonusRulesResponse>();

    const rawLoading = getMyCardApi.loading || getMyBalanceApi.loading || getMyTransactionsApi.loading ||
        getAllRulesApi.loading || getRuleByTypeApi.loading || updateRuleApi.loading || resetRuleApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getMyCardApi.error || getMyBalanceApi.error || getMyTransactionsApi.error ||
        getAllRulesApi.error || getRuleByTypeApi.error || updateRuleApi.error || resetRuleApi.error);

    const getMyCard = useCallback(async () => {
        const response = await getMyCardApi.execute(() => bonusApi.getMyCard(), {
            showErrorNotification: false,
        });
        return response || null;
    }, [getMyCardApi]);

    const getMyBalance = useCallback(async () => {
        const response = await getMyBalanceApi.execute(() => bonusApi.getMyBalance(), {
            showErrorNotification: false,
        });
        return response || null;
    }, [getMyBalanceApi]);

    const getMyTransactions = useCallback(async (params?: SearchParams) => {
        const response = await getMyTransactionsApi.execute(() => bonusApi.getMyTransactions(params), {
            showErrorNotification: true,
        });
        return response || null;
    }, [getMyTransactionsApi]);

    const getAllRules = useCallback(async () => {
        const response = await getAllRulesApi.execute(() => bonusApi.getAllRules(), {
            showErrorNotification: false,
        });
        return response || null;
    }, [getAllRulesApi]);

    const getRuleByType = useCallback(async (type: BonusTransactionType) => {
        const response = await getRuleByTypeApi.execute(() => bonusApi.getRuleByType(type), {
            showErrorNotification: false,
        });
        return response || null;
    }, [getRuleByTypeApi]);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        const ruleName = type.replace('_', ' ').toLowerCase();
        const response = await updateRuleApi.execute(() => bonusApi.updateRule(type, request), {
            successMessage: `${ruleName} rule updated successfully`,
        });
        return response || null;
    }, [updateRuleApi]);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        const ruleName = type.replace('_', ' ').toLowerCase();
        const response = await resetRuleApi.execute(() => bonusApi.resetRule(type), {
            successMessage: `${ruleName} rule reset to defaults`,
        });
        return response || null;
    }, [resetRuleApi]);

    const resetAll = useCallback(() => {
        getMyCardApi.reset();
        getMyBalanceApi.reset();
        getMyTransactionsApi.reset();
        getAllRulesApi.reset();
        getRuleByTypeApi.reset();
        updateRuleApi.reset();
        resetRuleApi.reset();
    }, [getMyCardApi, getMyBalanceApi, getMyTransactionsApi, getAllRulesApi, getRuleByTypeApi, updateRuleApi, resetRuleApi]);

    return {
        myCard: getMyCardApi.data,
        myBalance: getMyBalanceApi.data,
        myTransactions: getMyTransactionsApi.data?.content || [],
        myTransactionsPagination: getMyTransactionsApi.data,
        allRules: getAllRulesApi.data || [],
        rule: getRuleByTypeApi.data,
        loading,
        error,
        getMyCard,
        getMyBalance,
        getMyTransactions,
        getAllRules,
        getRuleByType,
        updateRule,
        resetRule,
        resetAll,
        totalPoints: getMyBalanceApi.data?.pointsBalance || 0,
        hasCard: !!getMyCardApi.data?.id,
    };
};