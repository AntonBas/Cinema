import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatDto, SeatType } from '@/types';

export const useSeats = () => {
    const [seats, setSeats] = useState<SeatDto[]>([]);
    const [seat, setSeat] = useState<SeatDto | null>(null);
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
            const message = err instanceof Error ? err.message : 'Failed to fetch seats';
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
            const message = err instanceof Error ? err.message : 'Failed to fetch seat';
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
            const message = err instanceof Error ? err.message : 'Failed to fetch seat by position';
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
            const message = err instanceof Error ? err.message : 'Failed to fetch seats by type';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

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
        getSeatsByType
    };
};