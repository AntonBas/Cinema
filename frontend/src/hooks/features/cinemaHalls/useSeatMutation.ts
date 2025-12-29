import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatType } from '@/types';

export const useSeatMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const executeMutation = useCallback(async <T>(operation: () => Promise<T>): Promise<T> => {
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

    const updateSeatType = useCallback((hallId: number, seatId: number, seatType: SeatType) =>
        executeMutation(() => seatApi.updateSeatType(hallId, seatId, seatType)), [executeMutation]);

    const activateSeat = useCallback((hallId: number, seatId: number) =>
        executeMutation(() => seatApi.activateSeat(hallId, seatId)), [executeMutation]);

    const deactivateSeat = useCallback((hallId: number, seatId: number) =>
        executeMutation(() => seatApi.deactivateSeat(hallId, seatId)), [executeMutation]);

    const clearError = () => setError(null);

    return {
        loading,
        error,
        updateSeatType,
        activateSeat,
        deactivateSeat,
        clearError
    };
};