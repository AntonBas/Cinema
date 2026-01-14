import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';

export const useSeats = () => {
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [seat, setSeat] = useState<SeatResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getSeatsByHall = useCallback(async (hallId: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await seatApi.getSeatsByHall(hallId);
            setSeats(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load seats';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getSeatById = useCallback(async (hallId: number, seatId: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await seatApi.getSeatById(hallId, seatId);
            setSeat(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load seat';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getSeatByPosition = useCallback(async (hallId: number, row: number, number: number) => {
        setLoading(true);
        setError(null);
        try {
            const data = await seatApi.getSeatByPosition(hallId, row, number);
            setSeat(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load seat';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const checkSeatAvailability = useCallback(async (hallId: number, row: number, number: number) => {
        setLoading(true);
        setError(null);
        try {
            return await seatApi.checkSeatAvailability(hallId, row, number);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to check seat availability';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const countSeatsByHall = useCallback(async (hallId: number) => {
        setLoading(true);
        setError(null);
        try {
            return await seatApi.countSeatsByHall(hallId);
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to count seats';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getSeatsByType = useCallback(async (hallId: number, seatType: SeatType) => {
        setLoading(true);
        setError(null);
        try {
            const data = await seatApi.getSeatsByType(hallId, seatType);
            setSeats(data);
            return data;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load seats by type';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getActiveSeatsByHall = useCallback(async (hallId: number) => {
        setLoading(true);
        setError(null);
        try {
            const allSeats = await seatApi.getSeatsByHall(hallId);
            const activeSeats = allSeats.filter(seat => seat.active);
            setSeats(activeSeats);
            return activeSeats;
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Failed to load active seats';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearSeats = () => {
        setSeats([]);
        setSeat(null);
    };

    const clearError = () => {
        setError(null);
    };

    return {
        seats,
        seat,
        loading,
        error,
        getSeatsByHall,
        getSeatById,
        getSeatByPosition,
        checkSeatAvailability,
        countSeatsByHall,
        getSeatsByType,
        getActiveSeatsByHall,
        clearSeats,
        clearError
    };
};