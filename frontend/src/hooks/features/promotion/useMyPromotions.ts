import { useState, useCallback, useEffect } from 'react';
import { usePromotion } from './usePromotion';
import type { UserPromotionResponse } from '@/types/promotion';

export const useMyPromotions = (options?: { autoFetch?: boolean }) => {
    const { autoFetch = true } = options || {};
    const { getMyPromotions, loading, error, clearError } = usePromotion();
    const [myPromotions, setMyPromotions] = useState<UserPromotionResponse[]>([]);
    const [localLoading, setLocalLoading] = useState(false);

    const fetchMyPromotions = useCallback(async () => {
        setLocalLoading(true);
        clearError();
        try {
            const data = await getMyPromotions();
            setMyPromotions(data);
            return data;
        } catch {
            return [];
        } finally {
            setLocalLoading(false);
        }
    }, [getMyPromotions, clearError]);

    useEffect(() => {
        if (autoFetch) {
            fetchMyPromotions();
        }
    }, [fetchMyPromotions, autoFetch]);

    const refresh = useCallback(async () => {
        return await fetchMyPromotions();
    }, [fetchMyPromotions]);

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

    return {
        myPromotions,
        loading: loading || localLoading,
        error,
        refresh,
        getTotalPointsEarned,
        hasClaimedPromotion,
        getClaimedPromotion,
        sortByDate,
        isEmpty: myPromotions.length === 0
    };
};