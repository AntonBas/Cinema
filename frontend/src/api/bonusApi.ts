import { api } from '@/services/api';
import type {
    BonusBalanceResponse,
    BonusRulesResponse,
    BonusTransactionResponse,
    BonusRulesRequest,
    BonusTransactionType
} from '@/types/bonus';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/bonus';
const ADMIN_BASE_URL = '/api/admin/bonus';

export const bonusApi = {
    getBalance: () => api.get<BonusBalanceResponse>(`${BASE_URL}/balance`),

    getTransactions: (params?: { page?: number; size?: number }) =>
        api.get<PageResponse<BonusTransactionResponse>>(`${BASE_URL}/transactions`, { params }),

    getAllRules: () => api.get<BonusRulesResponse[]>(`${ADMIN_BASE_URL}/rules`),

    updateRule: (type: BonusTransactionType, request: BonusRulesRequest) =>
        api.put<BonusRulesResponse>(`${ADMIN_BASE_URL}/rules/${type}`, request),

    resetRule: (type: BonusTransactionType) =>
        api.put<BonusRulesResponse>(`${ADMIN_BASE_URL}/rules/${type}/reset`)
};