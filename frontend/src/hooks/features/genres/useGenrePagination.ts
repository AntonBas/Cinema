import { useState, useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';

export const useGenrePagination = () => {
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<GenreResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (page: number = 0, size: number = 12): Promise<PageResponse<GenreResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await genreApi.public.getAllPaginated({ page, size });
            setGenres(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to fetch genres';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const nextPage = useCallback(async (): Promise<PageResponse<GenreResponse> | null> => {
        if (!pagination || pagination.last) return null;
        return fetchPage(pagination.number + 1, pagination.size);
    }, [pagination, fetchPage]);

    const prevPage = useCallback(async (): Promise<PageResponse<GenreResponse> | null> => {
        if (!pagination || pagination.first) return null;
        return fetchPage(pagination.number - 1, pagination.size);
    }, [pagination, fetchPage]);

    const clearError = () => setError(null);

    return {
        genres,
        pagination,
        loading,
        error,
        fetchPage,
        nextPage,
        prevPage,
        clearError
    };
};