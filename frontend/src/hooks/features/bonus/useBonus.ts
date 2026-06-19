import { useCallback, useRef } from 'react';
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

    const balanceApiRef = useRef(balanceApi);
    const transactionsApiRef = useRef(transactionsApi);
    const rulesApiRef = useRef(rulesApi);
    const updateRuleApiRef = useRef(updateRuleApi);
    const resetRuleApiRef = useRef(resetRuleApi);

    balanceApiRef.current = balanceApi;
    transactionsApiRef.current = transactionsApi;
    rulesApiRef.current = rulesApi;
    updateRuleApiRef.current = updateRuleApi;
    resetRuleApiRef.current = resetRuleApi;

    const loading = useDelayedLoading(
        balanceApi.loading || transactionsApi.loading || rulesApi.loading || updateRuleApi.loading || resetRuleApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getMyBalance = useCallback(async () => {
        return balanceApiRef.current.execute(() => bonusApi.getBalance());
    }, []);

    const getMyTransactions = useCallback(async (params?: SearchParams) => {
        return transactionsApiRef.current.execute(() => bonusApi.getTransactions(params));
    }, []);

    const getAllRules = useCallback(async () => {
        return rulesApiRef.current.execute(() => bonusApi.getAllRules());
    }, []);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest) => {
        const ruleName = type.replace(/_/g, ' ').toLowerCase();
        return updateRuleApiRef.current.execute(
            () => bonusApi.updateRule(type, request),
            { successMessage: `${ruleName} rule updated successfully` }
        );
    }, []);

    const resetRule = useCallback(async (type: BonusTransactionType) => {
        const ruleName = type.replace(/_/g, ' ').toLowerCase();
        return resetRuleApiRef.current.execute(
            () => bonusApi.resetRule(type),
            { successMessage: `${ruleName} rule reset to defaults` }
        );
    }, []);

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