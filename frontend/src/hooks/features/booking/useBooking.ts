import { useCallback } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type {
    BookingResponse,
    BookingCreateRequest
} from '@/types/booking';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useBooking = () => {
    const bookingByIdApi = useApi<BookingResponse>();
    const userBookingsApi = useApi<PageResponse<BookingResponse>>();
    const createBookingApi = useApi<BookingResponse>();
    const cancelBookingApi = useApi<void>();

    const create = useCallback(async (request: BookingCreateRequest) => {
        return createBookingApi.execute(
            () => bookingApi.create(request),
            {
                successMessage: 'Booking created successfully',
                onSuccess: () => {
                    userBookingsApi.invalidateCache();
                },
            }
        );
    }, [createBookingApi, userBookingsApi]);

    const getById = useCallback(async (bookingId: number) => {
        return bookingByIdApi.execute(
            () => bookingApi.getById(bookingId),
            {
                cacheKey: `booking_${bookingId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [bookingByIdApi]);

    const getUserBookings = useCallback(async (params?: any) => {
        return userBookingsApi.execute(
            () => bookingApi.getUserBookings(params),
            {
                cacheKey: `user_bookings_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [userBookingsApi]);

    const cancel = useCallback(async (bookingId: number) => {
        return cancelBookingApi.execute(
            () => bookingApi.cancel(bookingId),
            {
                successMessage: 'Booking cancelled successfully',
                onSuccess: () => {
                    bookingByIdApi.invalidateCache(`booking_${bookingId}`);
                    userBookingsApi.invalidateCache();
                },
            }
        );
    }, [cancelBookingApi, bookingByIdApi, userBookingsApi]);

    const clearCache = useCallback(() => {
        bookingByIdApi.invalidateCache();
        userBookingsApi.invalidateCache();
        createBookingApi.invalidateCache();
        cancelBookingApi.invalidateCache();
    }, [bookingByIdApi, userBookingsApi, createBookingApi, cancelBookingApi]);

    const loading = bookingByIdApi.loading || userBookingsApi.loading ||
        createBookingApi.loading || cancelBookingApi.loading;

    const error = !!(bookingByIdApi.error || userBookingsApi.error ||
        createBookingApi.error || cancelBookingApi.error);

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

        resetBooking: bookingByIdApi.reset,
        resetBookings: userBookingsApi.reset,

        currentPage: userBookingsApi.data?.number || 0,
        totalPages: userBookingsApi.data?.totalPages || 0,
        totalElements: userBookingsApi.data?.totalElements || 0,
        pageSize: userBookingsApi.data?.size || 20,
        isEmpty: userBookingsApi.data?.empty || false,
        isFirstPage: userBookingsApi.data?.first || true,
        isLastPage: userBookingsApi.data?.last || true,
    };
};