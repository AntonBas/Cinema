import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCardResponse } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

export const useMovieStatus = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchMoviesByStatus = useCallback(async (status: 'current' | 'upcoming' | 'archived', page: number = 0, size: number = 12) => {
        setLoading(true);
        setError(null);
        try {
            let response: PageResponse<MovieCardResponse>;

            switch (status) {
                case 'current':
                    response = await movieApi.getCurrentlyShowingMoviesPaginated(page, size);
                    break;
                case 'upcoming':
                    response = await movieApi.getUpcomingMoviesPaginated(page, size);
                    break;
                case 'archived':
                    response = await movieApi.getArchivedMoviesPaginated(page, size);
                    break;
                default:
                    throw new Error(`Invalid status: ${status}`);
            }

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

    const getNewReleases = useCallback(async (limit: number = 5) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getNewReleases(limit);
            setMovies(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch new releases';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getEndingSoon = useCallback(async (limit: number = 5) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getEndingSoon(limit);
            setMovies(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch ending soon movies';
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
        getNewReleases,
        getEndingSoon,
        clearError
    };
};