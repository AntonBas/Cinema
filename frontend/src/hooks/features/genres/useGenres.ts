import { useState, useEffect } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreDto } from '@/types/genre';

export const useGenres = () => {
    const [genres, setGenres] = useState<GenreDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadGenres = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await genreApi.getAll();
            setGenres(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load genres');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadGenres();
    }, []);

    return { genres, loading, error, refetch: loadGenres };
};