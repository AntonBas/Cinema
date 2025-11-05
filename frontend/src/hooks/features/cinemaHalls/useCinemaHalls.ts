import { useState, useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallWithSeatsDto, HallLayoutDto } from '@/types';

export const useCinemaHalls = () => {
    const [hallWithSeats, setHallWithSeats] = useState<CinemaHallWithSeatsDto | null>(null);
    const [hallLayout, setHallLayout] = useState<HallLayoutDto | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getHallWithSeats = useCallback(async (id: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.getHallWithSeats(id);
            setHallWithSeats(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch hall with seats';
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
            const data = await cinemaHallApi.getHallLayout(id);
            setHallLayout(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to fetch hall layout';
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
            return await cinemaHallApi.searchHalls(name);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to search halls';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        hallWithSeats,
        hallLayout,
        loading,
        error,
        getHallWithSeats,
        getHallLayout,
        searchHalls
    };
};