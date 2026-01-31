import { useState, useCallback } from 'react';
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

export const useBonus = () => {
    const [bonusData, setBonusData] = useState<BonusCardResponse | null>(null);
    const [balanceData, setBalanceData] = useState<BonusBalanceResponse | null>(null);
    const [rulesData, setRulesData] = useState<BonusRulesResponse[]>([]);
    const [transactions, setTransactions] = useState<BonusTransactionResponse[]>([]);
    const [pageData, setPageData] = useState<PageResponse<BonusTransactionResponse> | null>(null);

    const getMyCardHook = useApi<BonusCardResponse>();
    const getMyBalanceHook = useApi<BonusBalanceResponse>();
    const getMyTransactionsHook = useApi<PageResponse<BonusTransactionResponse>>();
    const getAllRulesHook = useApi<BonusRulesResponse[]>();
    const getRuleByTypeHook = useApi<BonusRulesResponse>();
    const updateRuleHook = useApi<BonusRulesResponse>();
    const resetRuleHook = useApi<BonusRulesResponse>();
    const getUserTransactionsHook = useApi<PageResponse<BonusTransactionResponse>>();
    const getAllTransactionsHook = useApi<PageResponse<BonusTransactionResponse>>();
    const getTransactionsByTypeHook = useApi<PageResponse<BonusTransactionResponse>>();

    const getMyCard = useCallback(async (): Promise<BonusCardResponse> => {
        return getMyCardHook.callApi(async () => {
            const response = await bonusApi.user.getMyCard();
            setBonusData(response);
            return response;
        });
    }, [getMyCardHook]);

    const getMyBalance = useCallback(async (): Promise<BonusBalanceResponse> => {
        return getMyBalanceHook.callApi(async () => {
            const response = await bonusApi.user.getMyBalance();
            setBalanceData(response);
            return response;
        });
    }, [getMyBalanceHook]);

    const getMyTransactions = useCallback(async (params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        return getMyTransactionsHook.callApi(async () => {
            const page = params?.page;
            const size = params?.size || 20;
            const response = await bonusApi.user.getMyTransactions(page, size);
            setTransactions(response.content);
            setPageData(response);
            return response;
        });
    }, [getMyTransactionsHook]);

    const getAllRules = useCallback(async (): Promise<BonusRulesResponse[]> => {
        return getAllRulesHook.callApi(async () => {
            const response = await bonusApi.admin.getAllRules();
            setRulesData(response);
            return response;
        });
    }, [getAllRulesHook]);

    const getRuleByType = useCallback(async (type: BonusTransactionType): Promise<BonusRulesResponse> => {
        return getRuleByTypeHook.callApi(async () => {
            return await bonusApi.admin.getRuleByType(type);
        });
    }, [getRuleByTypeHook]);

    const updateRule = useCallback(async (type: BonusTransactionType, request: BonusRulesRequest): Promise<BonusRulesResponse> => {
        return updateRuleHook.callApi(async () => {
            const response = await bonusApi.admin.updateRule(type, request);
            setRulesData(prevRules => prevRules.map(rule => rule.bonusType === type ? response : rule));
            return response;
        });
    }, [updateRuleHook]);

    const resetRule = useCallback(async (type: BonusTransactionType): Promise<BonusRulesResponse> => {
        return resetRuleHook.callApi(async () => {
            const response = await bonusApi.admin.resetRule(type);
            setRulesData(prevRules => prevRules.map(rule => rule.bonusType === type ? response : rule));
            return response;
        });
    }, [resetRuleHook]);

    const getUserTransactions = useCallback(async (userId: number, params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        return getUserTransactionsHook.callApi(async () => {
            const page = params?.page;
            const size = params?.size || 20;
            const response = await bonusApi.admin.getUserTransactions(userId, page, size);
            setTransactions(response.content);
            setPageData(response);
            return response;
        });
    }, [getUserTransactionsHook]);

    const getAllTransactions = useCallback(async (params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        return getAllTransactionsHook.callApi(async () => {
            const page = params?.page;
            const size = params?.size || 20;
            const response = await bonusApi.admin.getAllTransactions(page, size);
            setTransactions(response.content);
            setPageData(response);
            return response;
        });
    }, [getAllTransactionsHook]);

    const getTransactionsByType = useCallback(async (type: BonusTransactionType, params?: SearchParams): Promise<PageResponse<BonusTransactionResponse>> => {
        return getTransactionsByTypeHook.callApi(async () => {
            const page = params?.page;
            const size = params?.size || 20;
            const response = await bonusApi.admin.getTransactionsByType(type, page, size);
            setTransactions(response.content);
            setPageData(response);
            return response;
        });
    }, [getTransactionsByTypeHook]);

    return {
        bonusData,
        balanceData,
        rulesData,
        transactions,
        pageData,
        loading: getMyCardHook.loading || getMyBalanceHook.loading || getMyTransactionsHook.loading ||
            getAllRulesHook.loading || getRuleByTypeHook.loading || updateRuleHook.loading ||
            resetRuleHook.loading || getUserTransactionsHook.loading || getAllTransactionsHook.loading ||
            getTransactionsByTypeHook.loading,
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
        currentPage: pageData?.number || 0,
        totalPages: pageData?.totalPages || 0,
        totalElements: pageData?.totalElements || 0,
        pageSize: pageData?.size || 0,
        isEmpty: pageData?.empty || false,
        isFirstPage: pageData?.first || true,
        isLastPage: pageData?.last || true,
    };
};