import { useState, useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const useGenreSearch = () => {
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<GenreResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchGenres = useCallback(async (params: SearchParams = {}): Promise<PageResponse<GenreResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await genreApi.public.search(params);
            setGenres(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to search genres';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getAllGenresPaginated = useCallback(async (
        page: number = 0,
        size: number = 12
    ): Promise<PageResponse<GenreResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await genreApi.public.getAllPaginated({ page, size });
            setGenres(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to load genres';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        genres,
        pagination,
        loading,
        error,
        searchGenres,
        getAllGenresPaginated,
        clearError
    };
};