import { useState, useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    UserPromotionResponse,
    UserPromotionCreateRequest
} from '@/types/promotion';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const usePromotion = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAvailable = useCallback(async (): Promise<PromotionResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.user.getAvailable();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch available promotions';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getMyPromotions = useCallback(async (): Promise<UserPromotionResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.user.getMyPromotions();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch your promotions';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const claimPromotion = useCallback(async (request: UserPromotionCreateRequest): Promise<UserPromotionResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.user.claimPromotion(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to claim promotion';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const checkStatus = useCallback(async (promotionId: number): Promise<boolean> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.user.checkStatus(promotionId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to check promotion status';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        loading,
        error,
        getAvailable,
        getMyPromotions,
        claimPromotion,
        checkStatus,
        clearError
    };
};