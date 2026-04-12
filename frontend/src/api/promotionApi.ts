import { api } from '@/services/api';
import type {
    PromotionResponse,
    PromotionListResponse,
    PromotionRequest,
    ClaimPromotionRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/promotions';
const ADMIN_BASE_URL = '/api/admin/promotions';

export const promotionApi = {
    user: {
        getAvailable: () =>
            api.get<PromotionResponse[]>(BASE_URL),

        getClaimed: () =>
            api.get<PromotionResponse[]>(`${BASE_URL}/claimed`),

        claim: (request: ClaimPromotionRequest) =>
            api.post<PromotionResponse>(`${BASE_URL}/claim`, request),
    },

    admin: {
        create: (request: PromotionRequest) =>
            api.post<PromotionResponse>(ADMIN_BASE_URL, request),

        getById: (id: number) =>
            api.get<PromotionResponse>(`${ADMIN_BASE_URL}/${id}`),

        getAll: (params?: {
            query?: string;
            page?: number;
            size?: number;
            sort?: string[]
        }) =>
            api.get<PageResponse<PromotionListResponse>>(ADMIN_BASE_URL, { params }),

        update: (id: number, request: PromotionRequest) =>
            api.put<PromotionResponse>(`${ADMIN_BASE_URL}/${id}`, request),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_BASE_URL}/${id}`),
    }
};