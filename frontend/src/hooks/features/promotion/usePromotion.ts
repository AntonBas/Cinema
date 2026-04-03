import { useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    PromotionAdminResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const usePromotion = () => {
    const getAvailablePromotionsApi = useApi<PromotionResponse[]>();
    const getAllPromotionsApi = useApi<PageResponse<PromotionAdminResponse>>();
    const getPromotionByIdApi = useApi<PromotionResponse>();
    const claimPromotionApi = useApi<PromotionResponse>();
    const createPromotionApi = useApi<PromotionResponse>();
    const updatePromotionApi = useApi<PromotionResponse>();
    const deletePromotionApi = useApi<void>();

    const rawLoading = getAvailablePromotionsApi.loading || getAllPromotionsApi.loading ||
        getPromotionByIdApi.loading || claimPromotionApi.loading || createPromotionApi.loading ||
        updatePromotionApi.loading || deletePromotionApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAvailablePromotionsApi.error || getAllPromotionsApi.error ||
        getPromotionByIdApi.error || claimPromotionApi.error || createPromotionApi.error ||
        updatePromotionApi.error || deletePromotionApi.error);

    const getAvailable = useCallback(async () => {
        const response = await getAvailablePromotionsApi.execute(
            () => promotionApi.user.getAvailable(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAvailablePromotionsApi]);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest, promotionName?: string) => {
        const response = await claimPromotionApi.execute(
            () => promotionApi.user.claim(request),
            { successMessage: `Promotion "${promotionName || request.promotionId}" claimed successfully` }
        );
        return response || null;
    }, [claimPromotionApi]);

    const getById = useCallback(async (promotionId: number) => {
        const response = await getPromotionByIdApi.execute(
            () => promotionApi.admin.getById(promotionId),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPromotionByIdApi]);

    const getAll = useCallback(async (pageable?: { page: number; size: number; sort?: string[] }) => {
        const response = await getAllPromotionsApi.execute(
            () => promotionApi.admin.getAll(pageable),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAllPromotionsApi]);

    const create = useCallback(async (request: PromotionCreateRequest) => {
        const response = await createPromotionApi.execute(
            () => promotionApi.admin.create(request),
            { successMessage: `Promotion "${request.title}" created successfully` }
        );
        return response || null;
    }, [createPromotionApi]);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest, oldTitle?: string) => {
        const response = await updatePromotionApi.execute(
            () => promotionApi.admin.update(promotionId, request),
            { successMessage: `Promotion "${oldTitle || request.title}" updated successfully` }
        );
        return response || null;
    }, [updatePromotionApi]);

    const remove = useCallback(async (promotionId: number, promotionTitle?: string) => {
        await deletePromotionApi.execute(
            () => promotionApi.admin.delete(promotionId),
            { successMessage: `Promotion "${promotionTitle || promotionId}" deleted successfully` }
        );
    }, [deletePromotionApi]);

    const resetAll = useCallback(() => {
        getAvailablePromotionsApi.reset();
        getAllPromotionsApi.reset();
        getPromotionByIdApi.reset();
        claimPromotionApi.reset();
        createPromotionApi.reset();
        updatePromotionApi.reset();
        deletePromotionApi.reset();
    }, [getAvailablePromotionsApi, getAllPromotionsApi, getPromotionByIdApi, claimPromotionApi, createPromotionApi, updatePromotionApi, deletePromotionApi]);

    return {
        availablePromotions: getAvailablePromotionsApi.data || [],
        promotion: getPromotionByIdApi.data,
        promotionsPage: getAllPromotionsApi.data,
        promotions: getAllPromotionsApi.data?.content || [],
        loading,
        error,
        getAvailable,
        claimPromotion,
        getById,
        getAll,
        create,
        update,
        remove,
        resetAll,
    };
};