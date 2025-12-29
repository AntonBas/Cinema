import { useState, useCallback } from 'react';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import type { CinemaHallRequest } from '@/types';

const useMutation = <T>() => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const execute = useCallback(async (operation: () => Promise<T>): Promise<T> => {
        setLoading(true);
        setError(null);
        try {
            return await operation();
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Operation failed';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = () => setError(null);

    return { loading, error, execute, clearError };
};

export const useCinemaHallMutation = () => {
    const mutation = useMutation();

    const createHall = useCallback((request: CinemaHallRequest) =>
        mutation.execute(() => cinemaHallApi.createHall(request)), [mutation]);

    const updateHall = useCallback((id: number, request: CinemaHallRequest) =>
        mutation.execute(() => cinemaHallApi.updateHall(id, request)), [mutation]);

    const deleteHall = useCallback((id: number) =>
        mutation.execute(() => cinemaHallApi.deleteHall(id)), [mutation]);

    const clearError = () => mutation.clearError();

    return {
        loading: mutation.loading,
        error: mutation.error,
        createHall,
        updateHall,
        deleteHall,
        clearError
    };
};