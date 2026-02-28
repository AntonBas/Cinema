import { api } from '@/services/api';
import type {
    BonusBalanceResponse,
    BonusCardResponse,
    BonusRulesResponse,
    BonusTransactionResponse,
    BonusRulesRequest,
    BonusTransactionType
} from '@/types/bonus';
import type { PageResponse } from '@/types/pagination';

const USER_URL = '/api/bonus';
const ADMIN_URL = '/api/admin/bonus';

export const bonusApi = {
    user: {
        getMyCard: () => api.get<BonusCardResponse>(`${USER_URL}/my-card`),

        getMyBalance: () => api.get<BonusBalanceResponse>(`${USER_URL}/my-balance`),

        getMyTransactions: (params?: { page?: number; size?: number }) =>
            api.get<PageResponse<BonusTransactionResponse>>(`${USER_URL}/my-transactions`, {
                params: { ...params, size: params?.size || 20 }
            })
    },

    admin: {
        getAllRules: () => api.get<BonusRulesResponse[]>(`${ADMIN_URL}/rules`),

        getRuleByType: (type: BonusTransactionType) =>
            api.get<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}`),

        updateRule: (type: BonusTransactionType, request: BonusRulesRequest) =>
            api.put<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}`, request),

        resetRule: (type: BonusTransactionType) =>
            api.put<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}/reset`),

        getUserTransactions: (userId: number, params?: { page?: number; size?: number }) =>
            api.get<PageResponse<BonusTransactionResponse>>(`${ADMIN_URL}/users/${userId}/transactions`, {
                params: { ...params, size: params?.size || 20 }
            }),

        getAllTransactions: (params?: { page?: number; size?: number }) =>
            api.get<PageResponse<BonusTransactionResponse>>(`${ADMIN_URL}/transactions`, {
                params: { ...params, size: params?.size || 20 }
            }),

        getTransactionsByType: (type: BonusTransactionType, params?: { page?: number; size?: number }) =>
            api.get<PageResponse<BonusTransactionResponse>>(`${ADMIN_URL}/transactions/type/${type}`, {
                params: { ...params, size: params?.size || 20 }
            })
    }
};