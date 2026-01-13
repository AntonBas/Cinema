import { useState, useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type {
    BookingResponse,
    BookingCreateRequest,
    BookingStatus
} from '@/types/booking';
import type { PageResponse, SearchParams } from '@/types/pagination';
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

    const getUserBookings = useCallback(async (status?: BookingStatus, params?: SearchParams): Promise<PageResponse<BookingResponse>> => {
        setLoading(true);
        setError(null);
        try {
            const page = params?.page;
            const size = params?.size || 20;
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

    const getAvailableBonusPoints = useCallback(async (totalPrice: string): Promise<number> => {
        setLoading(true);
        setError(null);
        try {
            return await bookingApi.getAvailableBonusPoints(totalPrice);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to calculate available bonus points';
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
        getAvailableBonusPoints,
        clearError
    };
};