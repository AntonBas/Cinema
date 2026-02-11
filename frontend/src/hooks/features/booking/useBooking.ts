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
        return createBookingApi.callApi(
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
        return bookingByIdApi.callApi(
            () => bookingApi.getById(bookingId),
            {
                cacheKey: `booking_${bookingId}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [bookingByIdApi]);

    const getUserBookings = useCallback(async (params?: any) => {
        return userBookingsApi.callApi(
            () => bookingApi.getUserBookings(params),
            {
                cacheKey: `user_bookings_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [userBookingsApi]);

    const cancel = useCallback(async (bookingId: number) => {
        return cancelBookingApi.callApi(
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

    return {
        booking: bookingByIdApi.data,
        bookings: userBookingsApi.data?.content || [],
        pagination: userBookingsApi.data,

        loading: bookingByIdApi.state.isLoading || userBookingsApi.state.isLoading ||
            createBookingApi.state.isLoading || cancelBookingApi.state.isLoading,
        error: bookingByIdApi.state.isError || userBookingsApi.state.isError ||
            createBookingApi.state.isError || cancelBookingApi.state.isError,

        create,
        getById,
        getUserBookings,
        cancel,
        clearCache,

        resetBooking: bookingByIdApi.reset,
        resetBookings: userBookingsApi.reset,
        refetchBookings: userBookingsApi.refetch,

        currentPage: userBookingsApi.data?.number || 0,
        totalPages: userBookingsApi.data?.totalPages || 0,
        totalElements: userBookingsApi.data?.totalElements || 0,
        pageSize: userBookingsApi.data?.size || 20,
        isEmpty: userBookingsApi.data?.empty || false,
        isFirstPage: userBookingsApi.data?.first || true,
        isLastPage: userBookingsApi.data?.last || true,
    };
};