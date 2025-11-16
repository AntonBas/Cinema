import { useState, useEffect } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieDetailResponse } from '@/types/movie';

export const useMovies = () => {
    const [movies, setMovies] = useState<MovieDetailResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadMovies = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await movieApi.getAll();
            setMovies(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load movies');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadMovies();
    }, []);

    return { movies, loading, error, refetch: loadMovies };
};