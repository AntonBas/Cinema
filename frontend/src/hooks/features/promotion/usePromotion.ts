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

    const getAvailable = useCallback(async () => {
        return availablePromotionsApi.callApi(
            () => promotionApi.user.getAvailable(),
            {
                cacheKey: 'available_promotions',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [availablePromotionsApi]);

    const getMyPromotions = useCallback(async () => {
        return myPromotionsApi.callApi(
            () => promotionApi.user.getMyPromotions(),
            {
                cacheKey: 'my_promotions',
                cacheTime: 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [myPromotionsApi]);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest) => {
        return claimPromotionApi.callApi(
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
        return promotionByIdApi.callApi(
            () => promotionApi.admin.getById(promotionId),
            {
                cacheKey: `promotion_${promotionId}`,
                cacheTime: 2 * 60 * 1000,
            }
        );
    }, [promotionByIdApi]);

    const getAll = useCallback(async () => {
        return allPromotionsApi.callApi(
            () => promotionApi.admin.getAll(),
            {
                cacheKey: 'all_promotions',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allPromotionsApi]);

    const create = useCallback(async (request: PromotionCreateRequest) => {
        const createApi = useApi<PromotionResponse>();
        return createApi.callApi(
            () => promotionApi.admin.create(request),
            {
                successMessage: 'Promotion created successfully',
                onSuccess: () => {
                    allPromotionsApi.invalidateCache();
                    availablePromotionsApi.invalidateCache();
                },
            }
        );
    }, [allPromotionsApi, availablePromotionsApi]);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest) => {
        const updateApi = useApi<PromotionResponse>();
        return updateApi.callApi(
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
    }, [promotionByIdApi, allPromotionsApi, availablePromotionsApi, myPromotionsApi]);

    const remove = useCallback(async (promotionId: number) => {
        const deleteApi = useApi<void>();
        return deleteApi.callApi(
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
    }, [promotionByIdApi, allPromotionsApi, availablePromotionsApi, myPromotionsApi]);

    const clearPromotionCache = useCallback(() => {
        availablePromotionsApi.invalidateCache();
        myPromotionsApi.invalidateCache();
        promotionByIdApi.invalidateCache();
        allPromotionsApi.invalidateCache();
    }, [availablePromotionsApi, myPromotionsApi, promotionByIdApi, allPromotionsApi]);

    return {
        availablePromotions: availablePromotionsApi.data || [],
        myPromotions: myPromotionsApi.data || [],
        promotion: promotionByIdApi.data,
        allPromotions: allPromotionsApi.data || [],

        loading: availablePromotionsApi.state.isLoading || myPromotionsApi.state.isLoading,
        error: availablePromotionsApi.state.isError || myPromotionsApi.state.isError,

        getAvailable,
        getMyPromotions,
        claimPromotion,
        getById,
        getAll,
        create,
        update,
        remove,
        clearPromotionCache,

        resetAvailable: availablePromotionsApi.reset,
        resetMyPromotions: myPromotionsApi.reset,
        refetchAvailable: availablePromotionsApi.refetch,
        refetchMyPromotions: myPromotionsApi.refetch,
    };
};