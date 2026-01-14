import { useState, useEffect } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieCardResponse } from '@/types/movie';

export const useMovies = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadMovies = async (): Promise<MovieCardResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            const response = await movieApi.public.getMoviesPaginated(0, 100);
            setMovies(response.content);
            return response.content;
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to load movies';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadMovies();
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        movies,
        loading,
        error,
        refetch: loadMovies,
        clearError
    };
};