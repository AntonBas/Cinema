import { useState, useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse } from '@/types/person';
import type { PageResponse } from '@/types/pagination';

export const usePersonPagination = () => {
    const [persons, setPersons] = useState<PersonResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<PersonResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (page: number = 0, size: number = 12): Promise<PageResponse<PersonResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await personApi.getAllPaginated(page, size);
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
        return fetchPage(pagination.currentPage + 1, pagination.pageSize);
    }, [pagination, fetchPage]);

    const prevPage = useCallback(async (): Promise<PageResponse<PersonResponse> | null> => {
        if (!pagination || pagination.first) return null;
        return fetchPage(pagination.currentPage - 1, pagination.pageSize);
    }, [pagination, fetchPage]);

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
        clearError
    };
};