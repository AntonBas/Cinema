import { useState, useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallDto, CinemaHallRequest, SeatLayoutRequest } from '@/types';

export const useCinemaHallMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createHall = useCallback(async (request: CinemaHallRequest): Promise<CinemaHallDto> => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.createHall(request);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const updateHall = useCallback(async (id: number, request: CinemaHallRequest): Promise<CinemaHallDto> => {
        setLoading(true);
        setError(null);
        try {
            const data = await cinemaHallApi.updateHall(id, request);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const deleteHall = useCallback(async (id: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await cinemaHallApi.deleteHall(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const generateSeats = useCallback(async (id: number, request: SeatLayoutRequest): Promise<any> => {
        setLoading(true);
        setError(null);
        try {
            return await cinemaHallApi.generateSeats(id, request);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to generate seats';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        loading,
        error,
        createHall,
        updateHall,
        deleteHall,
        generateSeats
    };
};