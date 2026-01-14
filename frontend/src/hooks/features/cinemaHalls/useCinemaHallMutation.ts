import { useState } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallRequest } from '@/types/cinemaHall';

export const useCinemaHallMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createHall = async (request: CinemaHallRequest) => {
        setLoading(true);
        setError(null);
        try {
            const hall = await cinemaHallApi.admin.create(request);
            return hall;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to create cinema hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const updateHall = async (id: number, request: CinemaHallRequest) => {
        setLoading(true);
        setError(null);
        try {
            const hall = await cinemaHallApi.admin.update(id, request);
            return hall;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update cinema hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deleteHall = async (id: number) => {
        setLoading(true);
        setError(null);
        try {
            await cinemaHallApi.admin.delete(id);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to delete cinema hall';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const clearError = () => {
        setError(null);
    };

    return {
        loading,
        error,
        createHall,
        updateHall,
        deleteHall,
        clearError
    };
};