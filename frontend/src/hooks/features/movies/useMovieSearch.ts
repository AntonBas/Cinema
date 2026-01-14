import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieSessionSearchResponse,
    MovieStatus
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';

export const useMovieSearch = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [sessionMovies, setSessionMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchMovies = useCallback(async (
        params: SearchParams & { status?: MovieStatus } = {}
    ): Promise<PageResponse<MovieCardResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.public.getFilteredMovies(
                params.search,
                params.status,
                params.page,
                params.size || 20
            );
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

    const searchMoviesForSession = useCallback(async (
        sessionDate: string,
        search: string = ''
    ): Promise<MovieSessionSearchResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.admin.searchForSession(sessionDate, search);
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

    const getCurrentlyShowingPaginated = useCallback(async (
        page: number = 0,
        size: number = 12
    ): Promise<PageResponse<MovieCardResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.public.getCurrentlyShowingPaginated(page, size);
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

    const getUpcomingPaginated = useCallback(async (
        page: number = 0,
        size: number = 12
    ): Promise<PageResponse<MovieCardResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.public.getUpcomingPaginated(page, size);
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