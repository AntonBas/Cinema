import { useState, useEffect } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallResponse } from '@/types';

export const useHalls = () => {
    const [halls, setHalls] = useState<CinemaHallResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadHalls = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.getAllHalls();
            setHalls(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load halls');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadHalls();
    }, []);

    return { halls, loading, error, refetch: loadHalls };
};