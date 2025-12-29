import { useState, useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallResponse, CinemaHallWithSeatsResponse, HallLayoutResponse } from '@/types';

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

    const clearError = () => setError(null);

    return { data, loading, error, execute, clearError };
};

export const useCinemaHalls = () => {
    const allHallsQuery = useQuery<CinemaHallResponse[]>();
    const hallWithSeatsQuery = useQuery<CinemaHallWithSeatsResponse>();
    const hallLayoutQuery = useQuery<HallLayoutResponse>();

    const getAllHalls = useCallback(() =>
        allHallsQuery.execute(() => cinemaHallApi.getAllHalls()), [allHallsQuery]);

    const getHallWithSeats = useCallback((id: number) =>
        hallWithSeatsQuery.execute(() => cinemaHallApi.getHallWithSeats(id)), [hallWithSeatsQuery]);

    const getHallLayout = useCallback((id: number) =>
        hallLayoutQuery.execute(() => cinemaHallApi.getHallLayout(id)), [hallLayoutQuery]);

    const searchHalls = useCallback(async (name?: string) => {
        return await cinemaHallApi.searchHalls(name);
    }, []);

    const clearError = () => {
        allHallsQuery.clearError();
        hallWithSeatsQuery.clearError();
        hallLayoutQuery.clearError();
    };

    return {
        allHalls: allHallsQuery.data || [],
        hallWithSeats: hallWithSeatsQuery.data,
        hallLayout: hallLayoutQuery.data,
        loading: allHallsQuery.loading || hallWithSeatsQuery.loading || hallLayoutQuery.loading,
        error: allHallsQuery.error || hallWithSeatsQuery.error || hallLayoutQuery.error,
        getAllHalls,
        getHallWithSeats,
        getHallLayout,
        searchHalls,
        clearError
    };
};