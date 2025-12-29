import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCardResponse, MovieSessionSearchResponse, MovieFilter } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';

export const useMovieSearch = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [sessionMovies, setSessionMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchMovies = useCallback(async (filter: Partial<MovieFilter> = {}) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getFilteredMovies(filter);
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

    const searchMoviesForSession = useCallback(async (sessionDate: string, search: string = '') => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.searchMoviesForSessionCreation(sessionDate, search);
            setSessionMovies(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search movies for session';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getCurrentlyShowingPaginated = useCallback(async (page: number = 0, size: number = 12) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getCurrentlyShowingMoviesPaginated(page, size);
            setMovies(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch currently showing movies';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUpcomingPaginated = useCallback(async (page: number = 0, size: number = 12) => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.getUpcomingMoviesPaginated(page, size);
            setMovies(response.content);
            setPagination(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch upcoming movies';
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
        sessionMovies,
        pagination,
        loading,
        error,
        searchMovies,
        searchMoviesForSession,
        getCurrentlyShowingPaginated,
        getUpcomingPaginated,
        clearError
    };
};