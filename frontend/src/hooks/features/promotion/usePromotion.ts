import { useState, useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import { useApi } from '@/hooks/common/useApi';

interface UsePromotionListOptions {
    activeOnly?: boolean;
    autoFetch?: boolean;
}

export const usePromotion = () => {
    const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
    const [myPromotions, setMyPromotions] = useState<UserPromotionResponse[]>([]);
    const [adminPromotions, setAdminPromotions] = useState<PromotionResponse[]>([]);

    const getAvailableHook = useApi<PromotionResponse[]>();
    const getMyPromotionsHook = useApi<UserPromotionResponse[]>();
    const claimPromotionHook = useApi<UserPromotionResponse>();
    const checkStatusHook = useApi<boolean>();
    const createHook = useApi<PromotionResponse>();
    const getByIdHook = useApi<PromotionResponse>();
    const getAllHook = useApi<PromotionResponse[]>();
    const getActiveHook = useApi<PromotionResponse[]>();
    const updateHook = useApi<PromotionResponse>();
    const removeHook = useApi<void>();

    const getAvailable = useCallback(async (): Promise<PromotionResponse[]> => {
        return getAvailableHook.callApi(async () => {
            const data = await promotionApi.user.getAvailable();
            setPromotions(data);
            return data;
        }, { showErrorNotification: false });
    }, [getAvailableHook]);

    const getMyPromotions = useCallback(async (): Promise<UserPromotionResponse[]> => {
        return getMyPromotionsHook.callApi(async () => {
            const data = await promotionApi.user.getMyPromotions();
            setMyPromotions(data);
            return data;
        }, { showErrorNotification: false });
    }, [getMyPromotionsHook]);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest): Promise<UserPromotionResponse> => {
        return claimPromotionHook.callApi(async () => {
            return await promotionApi.user.claimPromotion(request);
        }, { showErrorNotification: false });
    }, [claimPromotionHook]);

    const checkStatus = useCallback(async (promotionId: number): Promise<boolean> => {
        return checkStatusHook.callApi(async () => {
            return await promotionApi.user.checkStatus(promotionId);
        }, { showErrorNotification: false });
    }, [checkStatusHook]);

    const create = useCallback(async (request: PromotionCreateRequest): Promise<PromotionResponse> => {
        return createHook.callApi(async () => {
            const response = await promotionApi.admin.create(request);
            setAdminPromotions(prev => [...prev, response]);
            return response;
        }, { showErrorNotification: false });
    }, [createHook]);

    const getById = useCallback(async (promotionId: number): Promise<PromotionResponse> => {
        return getByIdHook.callApi(async () => {
            return await promotionApi.admin.getById(promotionId);
        }, { showErrorNotification: false });
    }, [getByIdHook]);

    const getAll = useCallback(async (): Promise<PromotionResponse[]> => {
        return getAllHook.callApi(async () => {
            const data = await promotionApi.admin.getAll();
            setAdminPromotions(data);
            return data;
        }, { showErrorNotification: false });
    }, [getAllHook]);

    const getActive = useCallback(async (): Promise<PromotionResponse[]> => {
        return getActiveHook.callApi(async () => {
            const data = await promotionApi.admin.getActive();
            setAdminPromotions(data);
            return data;
        }, { showErrorNotification: false });
    }, [getActiveHook]);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest): Promise<PromotionResponse> => {
        return updateHook.callApi(async () => {
            const response = await promotionApi.admin.update(promotionId, request);
            setAdminPromotions(prev => prev.map(p => p.id === promotionId ? response : p));
            return response;
        }, { showErrorNotification: false });
    }, [updateHook]);

    const remove = useCallback(async (promotionId: number): Promise<void> => {
        return removeHook.callApi(async () => {
            await promotionApi.admin.delete(promotionId);
            setAdminPromotions(prev => prev.filter(p => p.id !== promotionId));
        }, { showErrorNotification: false });
    }, [removeHook]);

    const fetchPromotionsList = useCallback(async (options?: UsePromotionListOptions) => {
        const { activeOnly = false } = options || {};

        if (activeOnly) {
            return getAvailableHook.callApi(async () => {
                const data = await promotionApi.user.getAvailable();
                setPromotions(data);
                return data;
            }, { showErrorNotification: false });
        } else {
            return getAvailableHook.callApi(async () => {
                const data = await promotionApi.user.getAvailable();
                setPromotions(data);
                return data;
            }, { showErrorNotification: false });
        }
    }, [getAvailableHook]);

    const fetchMyPromotions = useCallback(async () => {
        return getMyPromotionsHook.callApi(async () => {
            const data = await promotionApi.user.getMyPromotions();
            setMyPromotions(data);
            return data;
        }, { showErrorNotification: false });
    }, [getMyPromotionsHook]);

    const fetchAdminPromotions = useCallback(async (activeOnly: boolean = false) => {
        if (activeOnly) {
            return getActiveHook.callApi(async () => {
                const data = await promotionApi.admin.getActive();
                setAdminPromotions(data);
                return data;
            }, { showErrorNotification: false });
        } else {
            return getAllHook.callApi(async () => {
                const data = await promotionApi.admin.getAll();
                setAdminPromotions(data);
                return data;
            }, { showErrorNotification: false });
        }
    }, [getActiveHook, getAllHook]);

    const isPromotionActive = useCallback((promotion: PromotionResponse): boolean => {
        if (!promotion.startDate && !promotion.endDate) return true;

        const now = new Date();
        const startDate = promotion.startDate ? new Date(promotion.startDate) : null;
        const endDate = promotion.endDate ? new Date(promotion.endDate) : null;

        if (startDate && now < startDate) return false;
        if (endDate && now > endDate) return false;

        return true;
    }, []);

    const getPromotionStatus = useCallback((promotion: PromotionResponse): string => {
        if (!promotion.startDate && !promotion.endDate) return 'active';

        const now = new Date();
        const startDate = promotion.startDate ? new Date(promotion.startDate) : null;
        const endDate = promotion.endDate ? new Date(promotion.endDate) : null;

        if (startDate && now < startDate) return 'upcoming';
        if (endDate && now > endDate) return 'expired';

        return 'active';
    }, []);

    const getStatusDisplay = useCallback((status: string): string => {
        const displayMap: Record<string, string> = {
            'active': 'Active',
            'upcoming': 'Upcoming',
            'expired': 'Expired'
        };
        return displayMap[status] || status;
    }, []);

    const filterByStatus = useCallback((status: string, list: PromotionResponse[]) => {
        return list.filter(promotion => getPromotionStatus(promotion) === status);
    }, [getPromotionStatus]);

    const getTotalPointsEarned = useCallback((): number => {
        return myPromotions.reduce((total, promotion) => total + promotion.pointsAwarded, 0);
    }, [myPromotions]);

    const hasClaimedPromotion = useCallback((promotionId: number): boolean => {
        return myPromotions.some(promotion => promotion.promotionId === promotionId);
    }, [myPromotions]);

    const getClaimedPromotion = useCallback((promotionId: number): UserPromotionResponse | undefined => {
        return myPromotions.find(promotion => promotion.promotionId === promotionId);
    }, [myPromotions]);

    const sortByDate = useCallback((order: 'asc' | 'desc' = 'desc') => {
        return [...myPromotions].sort((a, b) => {
            const dateA = new Date(a.claimedAt).getTime();
            const dateB = new Date(b.claimedAt).getTime();
            return order === 'asc' ? dateA - dateB : dateB - dateA;
        });
    }, [myPromotions]);

    const addPromotion = useCallback((promotion: PromotionResponse) => {
        setAdminPromotions(prev => [...prev, promotion]);
    }, []);

    const updatePromotion = useCallback((updatedPromotion: PromotionResponse) => {
        setAdminPromotions(prev => prev.map(p => p.id === updatedPromotion.id ? updatedPromotion : p));
    }, []);

    const removePromotion = useCallback((promotionId: number) => {
        setAdminPromotions(prev => prev.filter(p => p.id !== promotionId));
    }, []);

    const formatDateForInput = useCallback((date?: string): string => {
        if (!date) return '';
        return new Date(date).toISOString().split('T')[0];
    }, []);

    const parseDateFromInput = useCallback((dateString: string): string => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toISOString();
    }, []);

    const validateDates = useCallback((startDate?: string, endDate?: string): boolean => {
        if (!startDate && !endDate) return true;
        if (!startDate || !endDate) return true;

        const start = new Date(startDate);
        const end = new Date(endDate);

        return end >= start;
    }, []);

    const getDefaultValues = useCallback((): PromotionCreateRequest => ({
        title: '',
        description: '',
        bonusPoints: 100,
        startDate: '',
        endDate: ''
    }), []);

    return {
        promotions,
        myPromotions,
        adminPromotions,
        loading: getAvailableHook.loading || getMyPromotionsHook.loading || claimPromotionHook.loading ||
            checkStatusHook.loading || createHook.loading || getByIdHook.loading ||
            getAllHook.loading || getActiveHook.loading || updateHook.loading || removeHook.loading,
        getAvailable,
        getMyPromotions,
        claimPromotion,
        checkStatus,
        create,
        getById,
        getAll,
        getActive,
        update,
        remove,
        fetchPromotionsList,
        fetchMyPromotions,
        fetchAdminPromotions,
        isPromotionActive,
        getPromotionStatus,
        getStatusDisplay,
        filterByStatus,
        getTotalPointsEarned,
        hasClaimedPromotion,
        getClaimedPromotion,
        sortByDate,
        addPromotion,
        updatePromotion,
        removePromotion,
        formatDateForInput,
        parseDateFromInput,
        validateDates,
        getDefaultValues,
        isEmpty: promotions.length === 0,
        isMyPromotionsEmpty: myPromotions.length === 0,
        isAdminPromotionsEmpty: adminPromotions.length === 0
    };
};