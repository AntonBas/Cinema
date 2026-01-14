import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCardResponse, MovieStatus } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

export const useMovieStatus = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchMoviesByStatus = useCallback(async (
        status: MovieStatus,
        page: number = 0,
        size: number = 12
    ): Promise<PageResponse<MovieCardResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.admin.getByStatus(status, page, size);
            setMovies(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : `Failed to fetch ${status} movies`;
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
        movies,
        pagination,
        loading,
        error,
        fetchMoviesByStatus,
        clearError
    };
};