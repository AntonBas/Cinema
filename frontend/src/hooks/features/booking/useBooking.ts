import { useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type {
    BookingResponse,
    BookingCreateRequest,
} from '@/types/booking';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useBooking = () => {
    const bookingByIdApi = useApi<BookingResponse>();
    const mutationApi = useApi<BookingResponse | void>();

    const rawLoading = bookingByIdApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(bookingByIdApi.error || mutationApi.error);

    const create = useCallback(async (request: BookingCreateRequest) => {
        const response = await mutationApi.execute(
            () => bookingApi.create(request),
            {
                successMessage: 'Booking created successfully',
            }
        );
        return response || null;
    }, [mutationApi]);

    const getById = useCallback(async (bookingId: number) => {
        const response = await bookingByIdApi.execute(
            () => bookingApi.getById(bookingId),
            {
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [bookingByIdApi]);

    const cancel = useCallback(async (bookingId: number) => {
        await mutationApi.execute(
            () => bookingApi.cancel(bookingId),
            {
                successMessage: 'Booking cancelled successfully',
            }
        );
    }, [mutationApi]);

    const resetAll = useCallback(() => {
        bookingByIdApi.reset();
        mutationApi.reset();
    }, [bookingByIdApi, mutationApi]);

    return {
        booking: bookingByIdApi.data,
        loading,
        error,
        create,
        getById,
        cancel,
        resetAll,
    };
};