import { useState, useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreDto } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const useGenreSearch = () => {
    const [genres, setGenres] = useState<GenreDto[]>([]);
    const [pagination, setPagination] = useState<PageResponse<GenreDto> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchGenres = useCallback(async (params: SearchParams = {}) => {
        setLoading(true);
        setError(null);
        try {
            const response = await genreApi.search(params);
            setGenres(response.content);
            setPagination(response);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to search genres';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        genres,
        pagination,
        loading,
        error,
        searchGenres
    };
};