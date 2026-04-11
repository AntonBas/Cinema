import { useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type {
    BookingResponse,
    BookingCreateRequest,
} from '@/types/booking';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useBooking = () => {
    const bookingApiHook = useApi<BookingResponse>();
    const mutationApi = useApi<BookingResponse | void>();

    const loading = useDelayedLoading(
        bookingApiHook.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const create = useCallback(async (request: BookingCreateRequest) => {
        return mutationApi.execute(
            () => bookingApi.create(request),
            { successMessage: 'Booking created successfully' }
        );
    }, [mutationApi]);

    const getById = useCallback(async (bookingId: number) => {
        return bookingApiHook.execute(() => bookingApi.getById(bookingId));
    }, [bookingApiHook]);

    const cancel = useCallback(async (bookingId: number) => {
        return mutationApi.execute(
            () => bookingApi.cancel(bookingId),
            { successMessage: 'Booking cancelled successfully' }
        );
    }, [mutationApi]);

    return {
        booking: bookingApiHook.data,
        loading,
        error: bookingApiHook.error || mutationApi.error,
        create,
        getById,
        cancel,
        reset: () => {
            bookingApiHook.reset();
            mutationApi.reset();
        },
    };
};