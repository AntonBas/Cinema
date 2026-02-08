import type {
    BonusBalanceResponse,
    BonusCardResponse,
    BonusRulesResponse,
    BonusTransactionResponse,
    BonusRulesRequest,
    BonusTransactionType
} from '@/types/bonus';
import type { PageResponse } from '@/types/pagination';
import { buildPagedUrl } from '@/utils/paginationUtils';
import { handleApiError } from '@/utils/apiErrorHandler';

const USER_URL = '/api/bonus';
const ADMIN_URL = '/api/admin/bonus';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
    });
    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;
    return response.json();
};

export const bonusApi = {
    user: {
        getMyCard: () => fetchApi<BonusCardResponse>(`${USER_URL}/my-card`),

        getMyBalance: () => fetchApi<BonusBalanceResponse>(`${USER_URL}/my-balance`),

        getMyTransactions: (params?: { page?: number; size?: number }) => {
            const url = buildPagedUrl(`${USER_URL}/my-transactions`, { ...params, size: params?.size || 20 });
            return fetchApi<PageResponse<BonusTransactionResponse>>(url);
        }
    },

    admin: {
        getAllRules: () => fetchApi<BonusRulesResponse[]>(`${ADMIN_URL}/rules`),

        getRuleByType: (type: BonusTransactionType) =>
            fetchApi<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}`),

        updateRule: (type: BonusTransactionType, request: BonusRulesRequest) =>
            fetchApi<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        resetRule: (type: BonusTransactionType) =>
            fetchApi<BonusRulesResponse>(`${ADMIN_URL}/rules/${type}/reset`, {
                method: 'POST',
            }),

        getUserTransactions: (userId: number, params?: { page?: number; size?: number }) => {
            const url = buildPagedUrl(`${ADMIN_URL}/users/${userId}/transactions`, { ...params, size: params?.size || 20 });
            return fetchApi<PageResponse<BonusTransactionResponse>>(url);
        },

        getAllTransactions: (params?: { page?: number; size?: number }) => {
            const url = buildPagedUrl(`${ADMIN_URL}/transactions`, { ...params, size: params?.size || 20 });
            return fetchApi<PageResponse<BonusTransactionResponse>>(url);
        },

        getTransactionsByType: (type: BonusTransactionType, params?: { page?: number; size?: number }) => {
            const url = buildPagedUrl(`${ADMIN_URL}/transactions/type/${type}`, { ...params, size: params?.size || 20 });
            return fetchApi<PageResponse<BonusTransactionResponse>>(url);
        }
    }
};