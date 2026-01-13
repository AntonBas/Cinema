import { useState, useCallback, useEffect } from 'react';
import { useAdminPromotion } from './useAdminPromotion';
import type { PromotionResponse } from '@/types/promotion';

interface UseAdminPromotionListOptions {
    activeOnly?: boolean;
    autoFetch?: boolean;
}

export const useAdminPromotionList = (options?: UseAdminPromotionListOptions) => {
    const { activeOnly = false, autoFetch = true } = options || {};
    const { getAll, getActive, loading, error, clearError } = useAdminPromotion();
    const [promotions, setPromotions] = useState<PromotionResponse[]>([]);
    const [localLoading, setLocalLoading] = useState(false);

    const fetchPromotions = useCallback(async () => {
        setLocalLoading(true);
        clearError();
        try {
            let data: PromotionResponse[];
            if (activeOnly) {
                data = await getActive();
            } else {
                data = await getAll();
            }
            setPromotions(data);
            return data;
        } catch {
            return [];
        } finally {
            setLocalLoading(false);
        }
    }, [getAll, getActive, activeOnly, clearError]);

    useEffect(() => {
        if (autoFetch) {
            fetchPromotions();
        }
    }, [fetchPromotions, autoFetch]);

    const refresh = useCallback(async () => {
        return await fetchPromotions();
    }, [fetchPromotions]);

    const addPromotion = useCallback((promotion: PromotionResponse) => {
        setPromotions(prev => [...prev, promotion]);
    }, []);

    const updatePromotion = useCallback((updatedPromotion: PromotionResponse) => {
        setPromotions(prev =>
            prev.map(promotion =>
                promotion.id === updatedPromotion.id ? updatedPromotion : promotion
            )
        );
    }, []);

    const removePromotion = useCallback((promotionId: number) => {
        setPromotions(prev => prev.filter(promotion => promotion.id !== promotionId));
    }, []);

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

    const filterByStatus = useCallback((status: string) => {
        return promotions.filter(promotion => getPromotionStatus(promotion) === status);
    }, [promotions, getPromotionStatus]);

    const getActivePromotions = useCallback(() => {
        return promotions.filter(promotion => isPromotionActive(promotion));
    }, [promotions, isPromotionActive]);

    return {
        promotions,
        loading: loading || localLoading,
        error,
        refresh,
        addPromotion,
        updatePromotion,
        removePromotion,
        isPromotionActive,
        getPromotionStatus,
        getStatusDisplay,
        filterByStatus,
        getActivePromotions,
        isEmpty: promotions.length === 0
    };
};