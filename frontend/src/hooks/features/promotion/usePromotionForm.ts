import { useState, useCallback } from 'react';
import { useAdminPromotion } from './useAdminPromotion';
import type {
    PromotionCreateRequest,
    PromotionUpdateRequest
} from '@/types/promotion';

export const usePromotionForm = () => {
    const { create, update, loading, error, clearError } = useAdminPromotion();
    const [success, setSuccess] = useState(false);

    const handleCreate = useCallback(async (data: PromotionCreateRequest): Promise<boolean> => {
        clearError();
        setSuccess(false);
        try {
            await create(data);
            setSuccess(true);
            return true;
        } catch {
            return false;
        }
    }, [create, clearError]);

    const handleUpdate = useCallback(async (promotionId: number, data: PromotionUpdateRequest): Promise<boolean> => {
        clearError();
        setSuccess(false);
        try {
            await update(promotionId, data);
            setSuccess(true);
            return true;
        } catch {
            return false;
        }
    }, [update, clearError]);

    const reset = useCallback(() => {
        setSuccess(false);
        clearError();
    }, [clearError]);

    const getDefaultValues = useCallback((): PromotionCreateRequest => ({
        title: '',
        description: '',
        bonusPoints: 100,
        startDate: '',
        endDate: ''
    }), []);

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

    return {
        loading,
        error,
        success,
        handleCreate,
        handleUpdate,
        reset,
        getDefaultValues,
        formatDateForInput,
        parseDateFromInput,
        validateDates,
        isSubmitting: loading
    };
};