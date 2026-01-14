import { useState, useCallback, useEffect } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallResponse, CinemaHallWithSeatsResponse, HallLayoutResponse } from '@/types/cinemaHall';

export const useCinemaHalls = () => {
    const [allHalls, setAllHalls] = useState<CinemaHallResponse[]>([]);
    const [hallWithSeats, setHallWithSeats] = useState<CinemaHallWithSeatsResponse | null>(null);
    const [hallLayout, setHallLayout] = useState<HallLayoutResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAllHalls = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.getAll();
            setAllHalls(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load cinema halls';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        getAllHalls();
    }, [getAllHalls]);

    const getHallWithSeats = useCallback(async (id: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.getWithSeats(id);
            setHallWithSeats(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load hall with seats';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getHallLayout = useCallback(async (id: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.getLayout(id);
            setHallLayout(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load hall layout';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const searchHalls = useCallback(async (name?: string) => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.search(name);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search halls';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = () => {
        setError(null);
    };

    return {
        allHalls,
        hallWithSeats,
        hallLayout,
        loading,
        error,
        getAllHalls,
        getHallWithSeats,
        getHallLayout,
        searchHalls,
        clearError
    };
};