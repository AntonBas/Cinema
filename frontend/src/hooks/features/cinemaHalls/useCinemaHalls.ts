import { useState, useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallWithSeatsResponse, HallLayoutResponse } from '@/types';

const useQuery = <T>() => {
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const execute = useCallback(async (operation: () => Promise<T>): Promise<T> => {
        setLoading(true);
        setError(null);
        try {
            const result = await operation();
            setData(result);
            return result;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Operation failed';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return { data, loading, error, execute };
};

export const useCinemaHalls = () => {
    const hallWithSeatsQuery = useQuery<CinemaHallWithSeatsResponse>();
    const hallLayoutQuery = useQuery<HallLayoutResponse>();

    const getHallWithSeats = useCallback((id: number) =>
        hallWithSeatsQuery.execute(() => cinemaHallApi.getHallWithSeats(id)), [hallWithSeatsQuery]);

    const getHallLayout = useCallback((id: number) =>
        hallLayoutQuery.execute(() => cinemaHallApi.getHallLayout(id)), [hallLayoutQuery]);

    const searchHalls = useCallback(async (name?: string) => {
        return await cinemaHallApi.searchHalls(name);
    }, []);

    return {
        hallWithSeats: hallWithSeatsQuery.data,
        hallLayout: hallLayoutQuery.data,
        loading: hallWithSeatsQuery.loading || hallLayoutQuery.loading,
        error: hallWithSeatsQuery.error || hallLayoutQuery.error,
        getHallWithSeats,
        getHallLayout,
        searchHalls
    };
};