import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types';

export const useSeats = () => {
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [seat, setSeat] = useState<SeatResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const executeQuery = useCallback(async <T>(operation: () => Promise<T>): Promise<T> => {
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

    const getSeatsByHall = useCallback((hallId: number) =>
        executeQuery(async () => {
            const data = await seatApi.getSeatsByHall(hallId);
            setSeats(data);
            return data;
        }), [executeQuery]);

    const getSeatById = useCallback((hallId: number, seatId: number) =>
        executeQuery(async () => {
            const data = await seatApi.getSeatById(hallId, seatId);
            setSeat(data);
            return data;
        }), [executeQuery]);

    const getSeatByPosition = useCallback((hallId: number, row: number, number: number) =>
        executeQuery(async () => {
            const data = await seatApi.getSeatByPosition(hallId, row, number);
            setSeat(data);
            return data;
        }), [executeQuery]);

    const checkSeatAvailability = useCallback((hallId: number, row: number, number: number) =>
        executeQuery(() => seatApi.checkSeatAvailability(hallId, row, number)), [executeQuery]);

    const countSeatsByHall = useCallback((hallId: number) =>
        executeQuery(() => seatApi.countSeatsByHall(hallId)), [executeQuery]);

    const getSeatsByType = useCallback((hallId: number, seatType: SeatType) =>
        executeQuery(async () => {
            const data = await seatApi.getSeatsByType(hallId, seatType);
            setSeats(data);
            return data;
        }), [executeQuery]);

    const getActiveSeatsByHall = useCallback((hallId: number) =>
        executeQuery(async () => {
            const data = await seatApi.getActiveSeatsByHall(hallId);
            setSeats(data);
            return data;
        }), [executeQuery]);

    const clearSeats = () => {
        setSeats([]);
        setSeat(null);
    };

    const clearError = () => setError(null);

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