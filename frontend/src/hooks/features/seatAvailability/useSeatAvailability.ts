import { useState, useCallback } from 'react';
import type { SeatAvailabilityResponse } from '@/types/seatAvailability';
import { ApiErrorException } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/sessions';

export const useSeatAvailability = (sessionId: number) => {
    const [seatData, setSeatData] = useState<SeatAvailabilityResponse | null>(null);
    const [availableSeatsCount, setAvailableSeatsCount] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAuthHeaders = useCallback((): HeadersInit => {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
        };
    }, []);

    const fetchApi = useCallback(async <T>(url: string, options: RequestInit = {}): Promise<T> => {
        const response = await fetch(url, {
            headers: getAuthHeaders(),
            ...options,
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new ApiErrorException(errorData);
        }
        if (response.status === 204) return undefined as T;
        return response.json();
    }, [getAuthHeaders]);

    const fetchSeatAvailability = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchApi<SeatAvailabilityResponse>(`${BASE_URL}/${sessionId}/seats/availability`);
            setSeatData(data);
            setAvailableSeatsCount(data.availableSeats);
            return data;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch seat availability';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [sessionId, fetchApi]);

    const checkSpecificSeat = useCallback(async (seatId: number) => {
        try {
            const response = await fetch(`${BASE_URL}/${sessionId}/seats/${seatId}/availability`, {
                headers: getAuthHeaders(),
            });

            if (response.ok) {
                return true;
            }

            if (response.status === 404) {
                return true;
            }

            return false;
        } catch (err) {
            console.error('Seat availability check failed:', err);
            return true;
        }
    }, [sessionId, getAuthHeaders]);

    const fetchAvailableSeatsCount = useCallback(async () => {
        try {
            const count = await fetchApi<number>(`${BASE_URL}/${sessionId}/available-seats/count`);
            setAvailableSeatsCount(count);
            return count;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch available seats count';
            setError(message);
            throw err;
        }
    }, [sessionId, fetchApi]);

    const updateSeatStatus = useCallback((seatId: number, isAvailable: boolean) => {
        setSeatData(prev => {
            if (!prev) return prev;

            return {
                ...prev,
                seats: prev.seats.map(seat =>
                    seat.id === seatId
                        ? { ...seat, available: isAvailable }
                        : seat
                ),
                availableSeats: isAvailable
                    ? prev.availableSeats + 1
                    : prev.availableSeats - 1
            };
        });
    }, []);

    const getSeatInfo = useCallback((seatId: number) => {
        if (!seatData) return null;
        return seatData.seats.find(seat => seat.id === seatId) || null;
    }, [seatData]);

    const filterSeatsByRow = useCallback((row: number) => {
        if (!seatData) return [];
        return seatData.seats.filter(seat => seat.row === row);
    }, [seatData]);

    const getSeatPrice = useCallback((seatId: number, ticketTypeId?: number) => {
        const seatInfo = getSeatInfo(seatId);
        if (!seatInfo) return null;

        if (ticketTypeId) {
            const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
            return ticketPrice ? parseFloat(ticketPrice.finalPrice) : null;
        }

        return seatInfo.ticketPrices[0] ? parseFloat(seatInfo.ticketPrices[0].finalPrice) : null;
    }, [getSeatInfo]);

    return {
        seatData,
        availableSeatsCount,
        loading,
        error,
        fetchSeatAvailability,
        checkSpecificSeat,
        fetchAvailableSeatsCount,
        updateSeatStatus,
        getSeatInfo,
        filterSeatsByRow,
        getSeatPrice,
        hasData: !!seatData,
        totalSeats: seatData?.seats.length || 0,
        occupiedSeats: seatData ? seatData.seats.length - seatData.availableSeats : 0,
        seats: seatData?.seats || [],
        hallName: seatData?.hallName,
        movieTitle: seatData?.movieTitle,
        basePrice: seatData?.basePrice
    };
};