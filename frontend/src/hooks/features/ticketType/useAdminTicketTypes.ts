import { useState, useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticketType';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useAdminTicketTypes = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAll = useCallback(async (params?: { active?: boolean; category?: string; search?: string }): Promise<TicketTypeResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.admin.getAll(params);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch ticket types';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getById = useCallback(async (id: number): Promise<TicketTypeResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.admin.getById(id);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch ticket type with ID: ${id}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const create = useCallback(async (request: TicketTypeCreateRequest): Promise<TicketTypeResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.admin.create(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to create ticket type';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest): Promise<TicketTypeResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.admin.update(id, request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to update ticket type with ID: ${id}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const remove = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await ticketTypeApi.admin.delete(id);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to delete ticket type with ID: ${id}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const toggleActive = useCallback(async (id: number): Promise<TicketTypeResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketTypeApi.admin.toggleActive(id);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to toggle active status for ID: ${id}`;
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
        getAll,
        getById,
        create,
        update,
        remove,
        toggleActive,
        clearError
    };
};