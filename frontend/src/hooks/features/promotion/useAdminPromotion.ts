import { useState, useCallback } from 'react';
import { promotionApi } from '@/api/promotionApi';
import type {
    PromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest
} from '@/types/promotion';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useAdminPromotion = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const create = useCallback(async (request: PromotionCreateRequest): Promise<PromotionResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.admin.create(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to create promotion';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getById = useCallback(async (promotionId: number): Promise<PromotionResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.admin.getById(promotionId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch promotion with ID: ${promotionId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getAll = useCallback(async (): Promise<PromotionResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.admin.getAll();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch all promotions';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getActive = useCallback(async (): Promise<PromotionResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.admin.getActive();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch active promotions';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const update = useCallback(async (promotionId: number, request: PromotionUpdateRequest): Promise<PromotionResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await promotionApi.admin.update(promotionId, request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to update promotion with ID: ${promotionId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const remove = useCallback(async (promotionId: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await promotionApi.admin.delete(promotionId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to delete promotion with ID: ${promotionId}`;
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
        create,
        getById,
        getAll,
        getActive,
        update,
        remove,
        clearError
    };
};