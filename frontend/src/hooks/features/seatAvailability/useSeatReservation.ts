import { useState, useCallback } from 'react';
import { ApiErrorException } from '@/utils/apiErrorHandler';

interface ReservationData {
    seatIds: number[];
    ticketTypeIds?: Record<number, number>;
    sessionId: number;
    userId?: string;
}

export const useSeatReservation = () => {
    const [reserving, setReserving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAuthHeaders = useCallback((): HeadersInit => {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
        };
    }, []);

    const createTemporaryReservation = useCallback(async (reservationData: ReservationData) => {
        setReserving(true);
        setError(null);

        try {
            const response = await fetch('/api/reservations/temporary', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(reservationData)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new ApiErrorException(errorData);
            }

            return await response.json();
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to reserve seats';
            setError(message);
            throw err;
        } finally {
            setReserving(false);
        }
    }, [getAuthHeaders]);

    const confirmReservation = useCallback(async (reservationId: string) => {
        setReserving(true);
        setError(null);

        try {
            const response = await fetch(`/api/reservations/${reservationId}/confirm`, {
                method: 'POST',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new ApiErrorException(errorData);
            }

            return await response.json();
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to confirm reservation';
            setError(message);
            throw err;
        } finally {
            setReserving(false);
        }
    }, [getAuthHeaders]);

    const cancelReservation = useCallback(async (reservationId: string) => {
        setReserving(true);
        setError(null);

        try {
            const response = await fetch(`/api/reservations/${reservationId}/cancel`, {
                method: 'POST',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new ApiErrorException(errorData);
            }
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to cancel reservation';
            setError(message);
            throw err;
        } finally {
            setReserving(false);
        }
    }, [getAuthHeaders]);

    return {
        reserving,
        error,
        createTemporaryReservation,
        confirmReservation,
        cancelReservation,
        clearError: () => setError(null)
    };
};