import { useState } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatType } from '@/types/seat';

export const useSeatMutation = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateSeatType = async (hallId: number, seatId: number, seatType: SeatType) => {
        setLoading(true);
        setError(null);
        try {
            const seat = await seatApi.admin.updateSeatType(hallId, seatId, seatType);
            return seat;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to update seat type';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const activateSeat = async (hallId: number, seatId: number) => {
        setLoading(true);
        setError(null);
        try {
            const seat = await seatApi.admin.activateSeat(hallId, seatId);
            return seat;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to activate seat';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const deactivateSeat = async (hallId: number, seatId: number) => {
        setLoading(true);
        setError(null);
        try {
            const seat = await seatApi.admin.deactivateSeat(hallId, seatId);
            return seat;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to deactivate seat';
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
        updateSeatType,
        activateSeat,
        deactivateSeat,
        clearError
    };
};