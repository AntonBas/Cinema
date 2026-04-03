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

const BASE_URL = '/api/bonus';
const ADMIN_BASE_URL = '/api/admin/bonus';

export const bonusApi = {
    getMyCard: () => api.get<BonusCardResponse>(`${BASE_URL}/my-card`),

    getMyBalance: () => api.get<BonusBalanceResponse>(`${BASE_URL}/my-balance`),

    getMyTransactions: (params?: { page?: number; size?: number }) =>
        api.get<PageResponse<BonusTransactionResponse>>(`${BASE_URL}/my-transactions`, { params }),

    getAllRules: () => api.get<BonusRulesResponse[]>(`${ADMIN_BASE_URL}/rules`),

    getRuleByType: (type: BonusTransactionType) =>
        api.get<BonusRulesResponse>(`${ADMIN_BASE_URL}/rules/${type}`),

    updateRule: (type: BonusTransactionType, request: BonusRulesRequest) =>
        api.put<BonusRulesResponse>(`${ADMIN_BASE_URL}/rules/${type}`, request),

    resetRule: (type: BonusTransactionType) =>
        api.put<BonusRulesResponse>(`${ADMIN_BASE_URL}/rules/${type}/reset`)
};