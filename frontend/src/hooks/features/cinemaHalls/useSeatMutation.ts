import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatType } from '@/types';

export const useSeatMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        setLoading(true);
        setError(null);
        try {
            return await seatApi.updateSeatType(hallId, seatId, seatType);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update seat type';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    return {
        loading,
        error,
        updateSeatType
    };
};