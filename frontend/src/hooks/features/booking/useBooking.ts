import { useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type {
    BookingResponse,
    BookingCreateRequest,
    BookingStatus
} from '@/types/booking';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface BookingParams extends SearchParams {
    status?: BookingStatus;
}

export const useBooking = () => {
    const userBookingsApi = useApi<PageResponse<BookingResponse>>();
    const bookingByIdApi = useApi<BookingResponse>();
    const mutationApi = useApi<BookingResponse | void>();

    const rawLoading = userBookingsApi.loading || bookingByIdApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(userBookingsApi.error || bookingByIdApi.error || mutationApi.error);

    const create = useCallback(async (request: BookingCreateRequest) => {
        const response = await mutationApi.execute(
            () => bookingApi.create(request),
            {
                successMessage: 'Booking created successfully',
            }
        );
        userBookingsApi.invalidateCache();
        return response || null;
    }, [mutationApi, userBookingsApi]);

    const getById = useCallback(async (bookingId: number) => {
        const response = await bookingByIdApi.execute(
            () => bookingApi.getById(bookingId),
            {
                cacheKey: `booking_${bookingId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [bookingByIdApi]);

    const getUserBookings = useCallback(async (params?: BookingParams) => {
        const response = await userBookingsApi.execute(
            () => bookingApi.getUserBookings(params),
            {
                cacheKey: `user_bookings_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [userBookingsApi]);

    const cancel = useCallback(async (bookingId: number) => {
        await mutationApi.execute(
            () => bookingApi.cancel(bookingId),
            {
                successMessage: 'Booking cancelled successfully',
            }
        );
        bookingByIdApi.invalidateCache(`booking_${bookingId}`);
        userBookingsApi.invalidateCache();
    }, [mutationApi, bookingByIdApi, userBookingsApi]);

    const clearCache = useCallback(() => {
        userBookingsApi.invalidateCache();
        bookingByIdApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [userBookingsApi, bookingByIdApi, mutationApi]);

    const resetAll = useCallback(() => {
        userBookingsApi.reset();
        bookingByIdApi.reset();
        mutationApi.reset();
    }, [userBookingsApi, bookingByIdApi, mutationApi]);

    return {
        booking: bookingByIdApi.data,
        bookings: userBookingsApi.data?.content || [],
        pagination: userBookingsApi.data,

        loading,
        error,

        create,
        getById,
        getUserBookings,
        cancel,
        clearCache,
        resetAll,

        currentPage: userBookingsApi.data?.number || 0,
        totalPages: userBookingsApi.data?.totalPages || 0,
        totalElements: userBookingsApi.data?.totalElements || 0,
        pageSize: userBookingsApi.data?.size || 20,
        isEmpty: userBookingsApi.data?.empty || false,
        isFirstPage: userBookingsApi.data?.first || true,
        isLastPage: userBookingsApi.data?.last || true,
    };
};