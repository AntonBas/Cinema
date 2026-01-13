import { useState, useCallback } from 'react';
import { useAdminTicketTypes } from './useAdminTicketTypes';
import type {
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest,
    TicketTypeCategory
} from '@/types/ticketType';

export const useTicketTypeForm = () => {
    const { create, update, loading, error, clearError } = useAdminTicketTypes();
    const [success, setSuccess] = useState(false);

    const handleCreate = useCallback(async (data: TicketTypeCreateRequest): Promise<boolean> => {
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

    const handleUpdate = useCallback(async (id: number, data: TicketTypeUpdateRequest): Promise<boolean> => {
        clearError();
        setSuccess(false);
        try {
            await update(id, data);
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

    const getDefaultValues = useCallback((): TicketTypeCreateRequest => ({
        code: '',
        displayName: '',
        priceMultiplier: '1.0',
        category: 'STANDARD',
        minAge: null,
        maxAge: null,
        requiresDocument: false,
        documentType: null,
        active: true
    }), []);

    const getCategoryOptions = useCallback((): Array<{ value: TicketTypeCategory; label: string }> => [
        { value: 'STANDARD', label: 'Standard' },
        { value: 'CHILD', label: 'Child' },
        { value: 'STUDENT', label: 'Student' },
        { value: 'DISABLED', label: 'Disabled' },
        { value: 'MILITARY', label: 'Military' },
        { value: 'SENIOR', label: 'Senior' },
        { value: 'SPECIAL', label: 'Special' }
    ], []);

    const isAgeRestricted = useCallback((age: number, ticketType: { minAge?: number | null; maxAge?: number | null }): boolean => {
        const minAge = ticketType.minAge ?? null;
        const maxAge = ticketType.maxAge ?? null;

        if (minAge !== null && age < minAge) return true;
        if (maxAge !== null && age > maxAge) return true;
        return false;
    }, []);

    return {
        loading,
        error,
        success,
        handleCreate,
        handleUpdate,
        reset,
        getDefaultValues,
        getCategoryOptions,
        isAgeRestricted,
        isSubmitting: loading
    };
};