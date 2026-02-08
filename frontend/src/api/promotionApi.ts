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
        getAvailable: (): Promise<PromotionResponse[]> =>
            fetchApi<PromotionResponse[]>(USER_URL),

        getMyPromotions: (): Promise<UserPromotionResponse[]> =>
            fetchApi<UserPromotionResponse[]>(`${USER_URL}/my`),

        claimPromotion: (request: UserPromotionCreateRequest): Promise<UserPromotionResponse> =>
            fetchApi<UserPromotionResponse>(`${USER_URL}/claim`, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        checkStatus: (promotionId: number): Promise<boolean> =>
            fetchApi<boolean>(`${USER_URL}/${promotionId}/status`),
    },

    admin: {
        create: (request: PromotionCreateRequest): Promise<PromotionResponse> =>
            fetchApi<PromotionResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        getById: (promotionId: number): Promise<PromotionResponse> =>
            fetchApi<PromotionResponse>(`${ADMIN_URL}/${promotionId}`),

        getAll: (): Promise<PromotionResponse[]> =>
            fetchApi<PromotionResponse[]>(ADMIN_URL),

        getActive: (): Promise<PromotionResponse[]> =>
            fetchApi<PromotionResponse[]>(`${ADMIN_URL}/active`),

        update: (promotionId: number, request: PromotionUpdateRequest): Promise<PromotionResponse> =>
            fetchApi<PromotionResponse>(`${ADMIN_URL}/${promotionId}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        delete: (promotionId: number): Promise<void> =>
            fetchApi<void>(`${ADMIN_URL}/${promotionId}`, {
                method: 'DELETE'
            }),
    }
};