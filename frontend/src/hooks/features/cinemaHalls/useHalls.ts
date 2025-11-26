import { useState, useEffect } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallResponse } from '@/types';

const useQuery = <T>() => {
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const execute = async (operation: () => Promise<T>) => {
        setLoading(true);
        setError(null);
        try {
            const result = await operation();
            setData(result);
            return result;
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Operation failed');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, execute };
};

export const useHalls = () => {
    const query = useQuery<CinemaHallResponse[]>();

    const loadHalls = () => query.execute(() => cinemaHallApi.getAllHalls());

    useEffect(() => {
        loadHalls();
    }, []);

    return {
        halls: query.data || [],
        loading: query.loading,
        error: query.error,
        refetch: loadHalls
    };
};