import { useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const usePromotion = () => {
    const userPromotionsApi = useApi<PromotionResponse[]>();
    const adminPromotionsApi = useApi<PageResponse<PromotionResponse>>();
    const promotionApiInstance = useApi<PromotionResponse>();
    const mutationApi = useApi<PromotionResponse | UserPromotionResponse | void>();

    const rawLoading = userPromotionsApi.loading || adminPromotionsApi.loading ||
        promotionApiInstance.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(userPromotionsApi.error || adminPromotionsApi.error ||
        promotionApiInstance.error || mutationApi.error);

    const getAvailable = useCallback(async () => {
        const response = await userPromotionsApi.execute(
            () => promotionApi.user.getAvailable(),
            {
                cacheKey: 'available_promotions',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [userPromotionsApi]);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest) => {
        const response = await mutationApi.execute(
            () => promotionApi.user.claim(request),
            {
                successMessage: 'Promotion claimed successfully',
            }
        );
        userPromotionsApi.invalidateCache('available_promotions');
        return response || null;
    }, [mutationApi, userPromotionsApi]);

    const checkStatus = useCallback(async (promotionId: number) => {
        return await promotionApi.user.checkStatus(promotionId);
    }, []);

    const getById = useCallback(async (promotionId: number) => {
        const response = await promotionApiInstance.execute(
            () => promotionApi.admin.getById(promotionId),
            {
                cacheKey: `promotion_${promotionId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [promotionApiInstance]);

    const getAll = useCallback(async (pageable?: { page: number; size: number; sort?: string[] }) => {
        const response = await adminPromotionsApi.execute(
            () => promotionApi.admin.getAll(pageable),
            {
                cacheKey: `all_promotions_${JSON.stringify(pageable)}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminPromotionsApi]);

    const getActive = useCallback(async (pageable?: { page: number; size: number; sort?: string[] }) => {
        const response = await adminPromotionsApi.execute(
            () => promotionApi.admin.getActive(pageable),
            {
                cacheKey: `active_promotions_${JSON.stringify(pageable)}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminPromotionsApi]);

    const create = useCallback(async (request: PromotionCreateRequest) => {
        const response = await mutationApi.execute(
            () => promotionApi.admin.create(request),
            {
                successMessage: 'Promotion created successfully',
            }
        );
        adminPromotionsApi.invalidateCache();
        userPromotionsApi.invalidateCache('available_promotions');
        return response || null;
    }, [mutationApi, adminPromotionsApi, userPromotionsApi]);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest) => {
        const response = await mutationApi.execute(
            () => promotionApi.admin.update(promotionId, request),
            {
                successMessage: 'Promotion updated successfully',
            }
        );
        promotionApiInstance.invalidateCache(`promotion_${promotionId}`);
        adminPromotionsApi.invalidateCache();
        userPromotionsApi.invalidateCache('available_promotions');
        return response || null;
    }, [mutationApi, promotionApiInstance, adminPromotionsApi, userPromotionsApi]);

    const remove = useCallback(async (promotionId: number) => {
        await mutationApi.execute(
            () => promotionApi.admin.delete(promotionId),
            {
                successMessage: 'Promotion deleted successfully',
            }
        );
        promotionApiInstance.invalidateCache(`promotion_${promotionId}`);
        adminPromotionsApi.invalidateCache();
        userPromotionsApi.invalidateCache('available_promotions');
    }, [mutationApi, promotionApiInstance, adminPromotionsApi, userPromotionsApi]);

    const clearCache = useCallback(() => {
        userPromotionsApi.invalidateCache();
        adminPromotionsApi.invalidateCache();
        promotionApiInstance.invalidateCache();
        mutationApi.invalidateCache();
    }, [userPromotionsApi, adminPromotionsApi, promotionApiInstance, mutationApi]);

    const resetAll = useCallback(() => {
        userPromotionsApi.reset();
        adminPromotionsApi.reset();
        promotionApiInstance.reset();
        mutationApi.reset();
    }, [userPromotionsApi, adminPromotionsApi, promotionApiInstance, mutationApi]);

    return {
        availablePromotions: userPromotionsApi.data || [],
        promotion: promotionApiInstance.data,
        promotionsPage: adminPromotionsApi.data,
        promotions: adminPromotionsApi.data?.content || [],

        loading,
        error,

        getAvailable,
        claimPromotion,
        checkStatus,
        getById,
        getAll,
        getActive,
        create,
        update,
        remove,
        clearCache,
        resetAll,
    };
};