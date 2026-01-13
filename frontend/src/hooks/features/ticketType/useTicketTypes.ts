import { useState, useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeSimpleResponse
} from '@/types/ticketType';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useTicketTypes = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAllActive = useCallback(async (): Promise<TicketTypeResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.getAllActive();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch ticket types';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getSimpleActive = useCallback(async (): Promise<TicketTypeSimpleResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.getSimpleActive();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch simple ticket types';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getAvailableForAge = useCallback(async (age: number): Promise<TicketTypeSimpleResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.getAvailableForAge(age);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch ticket types for age: ${age}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const validateAge = useCallback(async (ticketTypeId: number, age: number): Promise<boolean> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.validateAge(ticketTypeId, age);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to validate age';
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
        getAllActive,
        getSimpleActive,
        getAvailableForAge,
        validateAge,
        clearError
    };
};