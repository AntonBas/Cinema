import { useState, useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type { BookingResponse, BookingCreateRequest, BookingStatus } from '@/types/booking';
import type { PageResponse } from '@/types/pagination';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useBooking = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const create = useCallback(async (request: BookingCreateRequest): Promise<BookingResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bookingApi.create(request);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to create booking';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getById = useCallback(async (bookingId: number): Promise<BookingResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await bookingApi.getById(bookingId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch booking with ID: ${bookingId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUserBookings = useCallback(async (status?: BookingStatus, page: number = 0, size: number = 20): Promise<PageResponse<BookingResponse>> => {
        setLoading(true);
        setError(null);
        try {
            return await bookingApi.getUserBookings(status, page, size);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch user bookings';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const cancel = useCallback(async (bookingId: number): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await bookingApi.cancel(bookingId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to cancel booking with ID: ${bookingId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        loading,
        error,
        create,
        getById,
        getUserBookings,
        cancel,
        clearError
    };
};