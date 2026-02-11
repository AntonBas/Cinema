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
                showErrorNotification: false,
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

    const getActive = useCallback(async () => {
        const api = useApi<PromotionResponse[]>();
        return api.callApi(
            () => promotionApi.admin.getActive(),
            {
                cacheKey: 'active_promotions_admin',
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const create = useCallback(async (request: PromotionCreateRequest) => {
        return createPromotionApi.callApi(
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
        return updatePromotionApi.callApi(
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
        return deletePromotionApi.callApi(
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

    return {
        availablePromotions: availablePromotionsApi.data || [],
        myPromotions: myPromotionsApi.data || [],
        promotion: promotionByIdApi.data,
        allPromotions: allPromotionsApi.data || [],

        loading: availablePromotionsApi.state.isLoading || myPromotionsApi.state.isLoading ||
            claimPromotionApi.state.isLoading || promotionByIdApi.state.isLoading ||
            allPromotionsApi.state.isLoading || createPromotionApi.state.isLoading ||
            updatePromotionApi.state.isLoading || deletePromotionApi.state.isLoading,
        error: availablePromotionsApi.state.isError || myPromotionsApi.state.isError ||
            claimPromotionApi.state.isError || promotionByIdApi.state.isError ||
            allPromotionsApi.state.isError || createPromotionApi.state.isError ||
            updatePromotionApi.state.isError || deletePromotionApi.state.isError,

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
        refetchAvailable: availablePromotionsApi.refetch,
        refetchMyPromotions: myPromotionsApi.refetch,
        refetchPromotion: promotionByIdApi.refetch,
        refetchAllPromotions: allPromotionsApi.refetch,
    };
};