import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieSessionSearchResponse } from '@/types/movie';

export const useMovieSessionSearch = () => {
    const [movies, setMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchMoviesForSession = useCallback(async (
        sessionDate: string,
        search: string = ''
    ): Promise<MovieSessionSearchResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.admin.searchForSession(sessionDate, search);
            setMovies(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search movies for session';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearResults = useCallback(() => {
        setMovies([]);
        setError(null);
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        movies,
        loading,
        error,
        searchMoviesForSession,
        clearResults,
        clearError
    };
};