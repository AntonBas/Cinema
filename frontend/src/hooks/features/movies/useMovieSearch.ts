import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieDetailResponse } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

export const useMovieSearch = () => {
    const [movies, setMovies] = useState<MovieDetailResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieDetailResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchMovies = useCallback(async (params: {
        page?: number;
        size?: number;
    } = {}) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getPaginated(params.page, params.size);
            setMovies(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search movies';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        movies,
        pagination,
        loading,
        error,
        searchMovies
    };
};