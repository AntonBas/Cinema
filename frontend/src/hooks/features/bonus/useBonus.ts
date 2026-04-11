import { useCallback } from 'react';
import { bonusApi } from '@/api/bonusApi';
import type {
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
    const balanceApi = useApi<BonusBalanceResponse>();
    const transactionsApi = useApi<PageResponse<BonusTransactionResponse>>();
    const rulesApi = useApi<BonusRulesResponse[]>();
    const updateRuleApi = useApi<BonusRulesResponse>();
    const resetRuleApi = useApi<BonusRulesResponse>();

    const loading = useDelayedLoading(
        balanceApi.loading || transactionsApi.loading || rulesApi.loading || updateRuleApi.loading || resetRuleApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getMyBalance = useCallback(async () => {
        return balanceApi.execute(() => bonusApi.getBalance());
    }, [balanceApi]);

    const getMyTransactions = useCallback(async (params?: SearchParams) => {
        return transactionsApi.execute(() => bonusApi.getTransactions(params));
    }, [transactionsApi]);

    const getAllRules = useCallback(async () => {
        return rulesApi.execute(() => bonusApi.getAllRules());
    }, [rulesApi]);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        const ruleName = type.replace(/_/g, ' ').toLowerCase();
        return updateRuleApi.execute(
            () => bonusApi.updateRule(type, request),
            { successMessage: `${ruleName} rule updated successfully` }
        );
    }, [updateRuleApi]);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        const ruleName = type.replace(/_/g, ' ').toLowerCase();
        return resetRuleApi.execute(
            () => bonusApi.resetRule(type),
            { successMessage: `${ruleName} rule reset to defaults` }
        );
    }, [resetRuleApi]);

    return {
        balance: balanceApi.data,
        transactions: transactionsApi.data?.content || [],
        transactionsPagination: transactionsApi.data,
        rules: rulesApi.data || [],
        loading,
        balanceError: balanceApi.error,
        transactionsError: transactionsApi.error,
        rulesError: rulesApi.error,
        getMyBalance,
        getMyTransactions,
        getAllRules,
        updateRule,
        resetRule,
    };
};