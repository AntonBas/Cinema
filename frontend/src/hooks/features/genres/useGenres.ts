import { useState, useEffect } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse } from '@/types/genre';

export const useGenres = () => {
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadGenres = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await genreApi.getAll();
            setGenres(data);
            return data;
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to load genres';
            setError(errorMessage);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadGenres();
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        genres,
        loading,
        error,
        refetch: loadGenres,
        clearError
    };
};