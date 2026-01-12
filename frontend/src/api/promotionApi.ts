import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import { handleApiError } from '@/utils/apiErrorHandler';

const USER_URL = '/api/promotions';
const ADMIN_URL = '/api/admin/promotions';

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

export const promotionApi = {
    user: {
        getAvailable: () => fetchApi<PromotionResponse[]>(USER_URL),

        getMyPromotions: () => fetchApi<UserPromotionResponse[]>(`${USER_URL}/my`),

        claimPromotion: (request: UserPromotionCreateRequest) =>
            fetchApi<UserPromotionResponse>(`${USER_URL}/claim`, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        checkStatus: (promotionId: number) =>
            fetchApi<boolean>(`${USER_URL}/${promotionId}/status`),
    },

    admin: {
        create: (request: PromotionCreateRequest) =>
            fetchApi<PromotionResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        getById: (promotionId: number) =>
            fetchApi<PromotionResponse>(`${ADMIN_URL}/${promotionId}`),

        getAll: () => fetchApi<PromotionResponse[]>(ADMIN_URL),

        getActive: () => fetchApi<PromotionResponse[]>(`${ADMIN_URL}/active`),

        update: (promotionId: number, request: PromotionUpdateRequest) =>
            fetchApi<PromotionResponse>(`${ADMIN_URL}/${promotionId}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        delete: (promotionId: number) =>
            fetchApi<void>(`${ADMIN_URL}/${promotionId}`, {
                method: 'DELETE'
            }),
    }
};