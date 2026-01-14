import { useState, useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRole } from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const usePersonPagination = () => {
    const [persons, setPersons] = useState<PersonResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (page: number = 0, size: number = 12): Promise<PageResponse<PersonResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await personApi.public.search({ page, size });
            setPersons(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch persons';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const nextPage = useCallback(async (): Promise<PageResponse<PersonResponse> | null> => {
        if (!pagination || pagination.last) return null;
        return fetchPage(pagination.number + 1, pagination.size);
    }, [pagination, fetchPage]);

    const prevPage = useCallback(async (): Promise<PageResponse<PersonResponse> | null> => {
        if (!pagination || pagination.first) return null;
        return fetchPage(pagination.number - 1, pagination.size);
    }, [pagination, fetchPage]);

    const searchPersons = useCallback(async (
        params: SearchParams & { role?: PersonRole }
    ): Promise<PageResponse<PersonResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await personApi.public.search(params);
            setPersons(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search persons';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getByRole = useCallback(async (
        role: PersonRole,
        page: number = 0,
        size: number = 12
    ): Promise<PageResponse<PersonResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await personApi.public.getByRole(role, { page, size });
            setPersons(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to get persons by role';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        persons,
        pagination,
        loading,
        error,
        fetchPage,
        nextPage,
        prevPage,
        searchPersons,
        getByRole,
        clearError
    };
};