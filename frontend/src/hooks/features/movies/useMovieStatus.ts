import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCardResponse } from '@/types/movie';

export const useMovieStatus = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchMoviesByStatus = useCallback(async (status: 'current' | 'upcoming' | 'archived') => {
        setLoading(true);
        setError(null);
        try {
            let response: MovieCardResponse[];
            switch (status) {
                case 'current':
                    response = await movieApi.getCurrentlyShowingMovies();
                    break;
                case 'upcoming':
                    response = await movieApi.getUpcomingMovies();
                    break;
                case 'archived':
                    response = await movieApi.getArchivedMovies();
                    break;
                default:
                    response = [];
            }
            setMovies(response);
            return response;
        } catch (err) {
            const message = err instanceof Error ? err.message : `Failed to fetch ${status} movies`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        movies,
        loading,
        error,
        fetchMoviesByStatus
    };
};