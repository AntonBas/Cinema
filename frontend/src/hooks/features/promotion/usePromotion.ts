import { useCallback, useRef } from 'react';
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
    const promotionApiHook = useApi<PromotionResponse>();
    const mutationApi = useApi<PromotionResponse | void>();

    const availableApiRef = useRef(availableApi);
    const claimedApiRef = useRef(claimedApi);
    const adminApiRef = useRef(adminApi);
    const promotionApiRef = useRef(promotionApiHook);
    const mutationApiRef = useRef(mutationApi);

    availableApiRef.current = availableApi;
    claimedApiRef.current = claimedApi;
    adminApiRef.current = adminApi;
    promotionApiRef.current = promotionApiHook;
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(
        availableApi.loading || claimedApi.loading || adminApi.loading || promotionApiHook.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getPromotionTitle = useCallback((id: number): string => {
        const promotion = adminApi.data?.content?.find(p => p.id === id) ||
            availableApi.data?.find(p => p.id === id) ||
            claimedApi.data?.find(p => p.id === id);
        return promotion?.title || String(id);
    }, [adminApi.data, availableApi.data, claimedApi.data]);

    const getAvailable = useCallback(async () => {
        return availableApiRef.current.execute(() => promotionApi.user.getAvailable());
    }, []);

    const getClaimed = useCallback(async () => {
        return claimedApiRef.current.execute(() => promotionApi.user.getClaimed());
    }, []);

    const claim = useCallback(async (request: ClaimPromotionRequest) => {
        return mutationApiRef.current.execute(
            () => promotionApi.user.claim(request),
            { successMessage: `Promotion "${getPromotionTitle(request.promotionId)}" claimed successfully` }
        );
    }, [getPromotionTitle]);

    const getById = useCallback(async (id: number) => {
        return promotionApiRef.current.execute(() => promotionApi.admin.getById(id));
    }, []);

    const getAll = useCallback(async (params?: {
        query?: string;
        page?: number;
        size?: number;
        sort?: string[]
    }) => {
        return adminApiRef.current.execute(() => promotionApi.admin.getAll(params));
    }, []);

    const create = useCallback(async (request: PromotionRequest) => {
        return mutationApiRef.current.execute(
            () => promotionApi.admin.create(request),
            { successMessage: `Promotion "${request.title}" created successfully` }
        );
    }, []);

    const update = useCallback(async (id: number, request: PromotionRequest) => {
        return mutationApiRef.current.execute(
            () => promotionApi.admin.update(id, request),
            { successMessage: `Promotion "${getPromotionTitle(id)}" updated successfully` }
        );
    }, [getPromotionTitle]);

    const remove = useCallback(async (id: number) => {
        return mutationApiRef.current.execute(
            () => promotionApi.admin.delete(id),
            { successMessage: `Promotion "${getPromotionTitle(id)}" deleted successfully` }
        );
    }, [getPromotionTitle]);

    return {
        availablePromotions: availableApi.data || [],
        claimedPromotions: claimedApi.data || [],
        adminPromotions: adminApi.data?.content || [],
        promotion: promotionApiHook.data,
        pagination: adminApi.data,
        loading,
        availableError: availableApi.error,
        claimedError: claimedApi.error,
        adminError: adminApi.error,
        promotionError: promotionApiHook.error,
        mutationError: mutationApi.error,
        getAvailable,
        getClaimed,
        claim,
        getById,
        getAll,
        create,
        update,
        remove,
    };
};