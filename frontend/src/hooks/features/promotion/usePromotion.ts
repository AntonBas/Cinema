import { useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import { useApi } from '@/hooks/common/useApi';

export const usePromotion = () => {
    const availablePromotionsApi = useApi<PromotionResponse[]>();
    const myPromotionsApi = useApi<UserPromotionResponse[]>();
    const claimPromotionApi = useApi<UserPromotionResponse>();
    const promotionByIdApi = useApi<PromotionResponse>();
    const allPromotionsApi = useApi<PromotionResponse[]>();
    const createPromotionApi = useApi<PromotionResponse>();
    const updatePromotionApi = useApi<PromotionResponse>();
    const deletePromotionApi = useApi<void>();

    const getAvailable = useCallback(async () => {
        return availablePromotionsApi.execute(
            () => promotionApi.user.getAvailable(),
            {
                cacheKey: 'available_promotions',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [availablePromotionsApi]);

    const getMyPromotions = useCallback(async () => {
        return myPromotionsApi.execute(
            () => promotionApi.user.getMyPromotions(),
            {
                cacheKey: 'my_promotions',
                cacheTime: 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [myPromotionsApi]);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest) => {
        return claimPromotionApi.execute(
            () => promotionApi.user.claimPromotion(request),
            {
                successMessage: 'Promotion claimed successfully',
                onSuccess: () => {
                    myPromotionsApi.invalidateCache();
                    availablePromotionsApi.invalidateCache();
                },
            }
        );
    }, [claimPromotionApi, myPromotionsApi, availablePromotionsApi]);

    const getById = useCallback(async (promotionId: number) => {
        return promotionByIdApi.execute(
            () => promotionApi.admin.getById(promotionId),
            {
                cacheKey: `promotion_${promotionId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [promotionByIdApi]);

    const getAll = useCallback(async () => {
        return allPromotionsApi.execute(
            () => promotionApi.admin.getAll(),
            {
                cacheKey: 'all_promotions',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allPromotionsApi]);

    const getActive = useCallback(async () => {
        return promotionByIdApi.execute(
            () => promotionApi.admin.getActive(),
            {
                cacheKey: 'active_promotions_admin',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [promotionByIdApi]);

    const create = useCallback(async (request: PromotionCreateRequest) => {
        return createPromotionApi.execute(
            () => promotionApi.admin.create(request),
            {
                successMessage: 'Promotion created successfully',
                onSuccess: () => {
                    allPromotionsApi.invalidateCache();
                    availablePromotionsApi.invalidateCache();
                },
            }
        );
    }, [createPromotionApi, allPromotionsApi, availablePromotionsApi]);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest) => {
        return updatePromotionApi.execute(
            () => promotionApi.admin.update(promotionId, request),
            {
                successMessage: 'Promotion updated successfully',
                onSuccess: () => {
                    promotionByIdApi.invalidateCache(`promotion_${promotionId}`);
                    allPromotionsApi.invalidateCache();
                    availablePromotionsApi.invalidateCache();
                    myPromotionsApi.invalidateCache();
                },
            }
        );
    }, [updatePromotionApi, promotionByIdApi, allPromotionsApi, availablePromotionsApi, myPromotionsApi]);

    const remove = useCallback(async (promotionId: number) => {
        return deletePromotionApi.execute(
            () => promotionApi.admin.delete(promotionId),
            {
                successMessage: 'Promotion deleted successfully',
                onSuccess: () => {
                    promotionByIdApi.invalidateCache(`promotion_${promotionId}`);
                    allPromotionsApi.invalidateCache();
                    availablePromotionsApi.invalidateCache();
                    myPromotionsApi.invalidateCache();
                },
            }
        );
    }, [deletePromotionApi, promotionByIdApi, allPromotionsApi, availablePromotionsApi, myPromotionsApi]);

    const clearPromotionCache = useCallback(() => {
        availablePromotionsApi.invalidateCache();
        myPromotionsApi.invalidateCache();
        promotionByIdApi.invalidateCache();
        allPromotionsApi.invalidateCache();
        createPromotionApi.invalidateCache();
        updatePromotionApi.invalidateCache();
        deletePromotionApi.invalidateCache();
    }, [availablePromotionsApi, myPromotionsApi, promotionByIdApi, allPromotionsApi,
        createPromotionApi, updatePromotionApi, deletePromotionApi]);

    const loading = availablePromotionsApi.loading || myPromotionsApi.loading ||
        claimPromotionApi.loading || promotionByIdApi.loading ||
        allPromotionsApi.loading || createPromotionApi.loading ||
        updatePromotionApi.loading || deletePromotionApi.loading;

    const error = !!(availablePromotionsApi.error || myPromotionsApi.error ||
        claimPromotionApi.error || promotionByIdApi.error ||
        allPromotionsApi.error || createPromotionApi.error ||
        updatePromotionApi.error || deletePromotionApi.error);

    return {
        availablePromotions: availablePromotionsApi.data || [],
        myPromotions: myPromotionsApi.data || [],
        promotion: promotionByIdApi.data,
        allPromotions: allPromotionsApi.data || [],

        loading,
        error,

        getAvailable,
        getMyPromotions,
        claimPromotion,
        getById,
        getAll,
        getActive,
        create,
        update,
        remove,
        clearPromotionCache,

        resetAvailable: availablePromotionsApi.reset,
        resetMyPromotions: myPromotionsApi.reset,
        resetPromotion: promotionByIdApi.reset,
        resetAllPromotions: allPromotionsApi.reset,
    };
};