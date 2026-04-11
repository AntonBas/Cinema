import { useCallback, useRef } from 'react';
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

    const bookingApiRef = useRef(bookingApiHook);
    const mutationApiRef = useRef(mutationApi);

    bookingApiRef.current = bookingApiHook;
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(
        bookingApiHook.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const create = useCallback(async (request: BookingCreateRequest) => {
        return mutationApiRef.current.execute(
            () => bookingApi.create(request),
            { successMessage: 'Booking created successfully' }
        );
    }, []);

    const getById = useCallback(async (bookingId: number) => {
        return bookingApiRef.current.execute(() => bookingApi.getById(bookingId));
    }, []);

    const cancel = useCallback(async (bookingId: number) => {
        return mutationApiRef.current.execute(
            () => bookingApi.cancel(bookingId),
            { successMessage: 'Booking cancelled successfully' }
        );
    }, []);

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