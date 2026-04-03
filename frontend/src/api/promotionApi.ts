import { api } from '@/services/api';
import type {
    PromotionResponse,
    PromotionAdminResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/promotions';
const ADMIN_BASE_URL = '/api/admin/promotions';

export const promotionApi = {
    user: {
        getAvailable: () =>
            api.get<PromotionResponse[]>(BASE_URL),

        claim: (request: UserPromotionCreateRequest) =>
            api.post<PromotionResponse>(`${BASE_URL}/claim`, request),
    },

    admin: {
        create: (request: PromotionCreateRequest) =>
            api.post<PromotionResponse>(ADMIN_BASE_URL, request),

        getById: (promotionId: number) =>
            api.get<PromotionResponse>(`${ADMIN_BASE_URL}/${promotionId}`),

        getAll: (pageable?: { page: number; size: number; sort?: string[] }) =>
            api.get<PageResponse<PromotionAdminResponse>>(ADMIN_BASE_URL, { params: pageable }),

        update: (promotionId: number, request: PromotionUpdateRequest) =>
            api.put<PromotionResponse>(`${ADMIN_BASE_URL}/${promotionId}`, request),

        delete: (promotionId: number) =>
            api.delete<void>(`${ADMIN_BASE_URL}/${promotionId}`),
    }
};