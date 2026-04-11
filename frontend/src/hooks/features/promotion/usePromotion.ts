import { useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    PromotionListResponse,
    PromotionRequest,
    ClaimPromotionRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const usePromotion = () => {
    const availableApi = useApi<PromotionResponse[]>();
    const claimedApi = useApi<PromotionResponse[]>();
    const adminApi = useApi<PageResponse<PromotionListResponse>>();
    const mutationApi = useApi<PromotionResponse | void>();

    const loading = useDelayedLoading(
        availableApi.loading || claimedApi.loading || adminApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getPromotionTitle = useCallback((id: number): string => {
        const promotion = adminApi.data?.content?.find(p => p.id === id) ||
            availableApi.data?.find(p => p.id === id) ||
            claimedApi.data?.find(p => p.id === id);
        return promotion?.title || String(id);
    }, [adminApi.data, availableApi.data, claimedApi.data]);

    const getAvailable = useCallback(async () => {
        return availableApi.execute(() => promotionApi.user.getAvailable());
    }, [availableApi]);

    const getClaimed = useCallback(async () => {
        return claimedApi.execute(() => promotionApi.user.getClaimed());
    }, [claimedApi]);

    const claim = useCallback(async (request: ClaimPromotionRequest) => {
        return mutationApi.execute(
            () => promotionApi.user.claim(request),
            { successMessage: `Promotion "${getPromotionTitle(request.promotionId)}" claimed successfully` }
        );
    }, [mutationApi, getPromotionTitle]);

    const getAll = useCallback(async (params?: { page?: number; size?: number; sort?: string[] }) => {
        return adminApi.execute(() => promotionApi.admin.getAll(params));
    }, [adminApi]);

    const create = useCallback(async (request: PromotionRequest) => {
        return mutationApi.execute(
            () => promotionApi.admin.create(request),
            { successMessage: `Promotion "${request.title}" created successfully` }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: PromotionRequest) => {
        return mutationApi.execute(
            () => promotionApi.admin.update(id, request),
            { successMessage: `Promotion "${getPromotionTitle(id)}" updated successfully` }
        );
    }, [mutationApi, getPromotionTitle]);

    const remove = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => promotionApi.admin.delete(id),
            { successMessage: `Promotion "${getPromotionTitle(id)}" deleted successfully` }
        );
    }, [mutationApi, getPromotionTitle]);

    return {
        availablePromotions: availableApi.data || [],
        claimedPromotions: claimedApi.data || [],
        adminPromotions: adminApi.data?.content || [],
        pagination: adminApi.data,
        loading,
        availableError: availableApi.error,
        claimedError: claimedApi.error,
        adminError: adminApi.error,
        mutationError: mutationApi.error,
        getAvailable,
        getClaimed,
        claim,
        getAll,
        create,
        update,
        remove,
    };
};