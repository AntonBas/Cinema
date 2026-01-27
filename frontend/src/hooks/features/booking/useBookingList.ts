import { useState, useCallback, useEffect } from 'react';
import { useBooking } from './useBooking';
import type { BookingResponse, BookingStatus } from '@/types/booking';
import type { PageResponse } from '@/types/pagination';

interface UseBookingListOptions {
    status?: BookingStatus;
    pageSize?: number;
    autoFetch?: boolean;
}

export const useBookingList = (options: UseBookingListOptions = {}) => {
    const { status, pageSize = 20, autoFetch = true } = options;
    const { getUserBookings, loading, error, clearError } = useBooking();

    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [pageData, setPageData] = useState<PageResponse<BookingResponse> | null>(null);
    const [isFetching, setIsFetching] = useState(false);

    const fetchBookings = useCallback(async (page: number = 0) => {
        setIsFetching(true);
        clearError();
        try {
            const response = await getUserBookings(status, page, pageSize);
            setBookings(response.content);
            setPageData(response);
            return response;
        } catch {
            return null;
        } finally {
            setIsFetching(false);
        }
    }, [getUserBookings, status, pageSize, clearError]);

    useEffect(() => {
        if (autoFetch) {
            fetchBookings(0);
        }
    }, [fetchBookings, autoFetch]);

    const loadPage = useCallback(async (page: number) => {
        return await fetchBookings(page);
    }, [fetchBookings]);

    const nextPage = useCallback(async () => {
        if (pageData && !pageData.last) {
            return await loadPage(pageData.number + 1);
        }
        return null;
    }, [pageData, loadPage]);

    const prevPage = useCallback(async () => {
        if (pageData && !pageData.first) {
            return await loadPage(pageData.number - 1);
        }
        return null;
    }, [pageData, loadPage]);

    const refresh = useCallback(async () => {
        if (pageData) {
            return await fetchBookings(pageData.number);
        }
        return await fetchBookings(0);
    }, [fetchBookings, pageData]);

    const getStatusDisplay = useCallback((status: string): string => {
        const displayMap: Record<string, string> = {
            'DRAFT': 'Draft',
            'PENDING': 'Pending',
            'CONFIRMED': 'Confirmed',
            'CANCELLED': 'Cancelled',
            'EXPIRED': 'Expired',
            'FAILED': 'Failed',
            'REFUNDED': 'Refunded'
        };
        return displayMap[status] || status;
    }, []);

    const getStatusColor = useCallback((status: string): string => {
        const colorMap: Record<string, string> = {
            'DRAFT': 'default',
            'PENDING': 'warning',
            'CONFIRMED': 'success',
            'CANCELLED': 'error',
            'EXPIRED': 'default',
            'FAILED': 'error',
            'REFUNDED': 'info'
        };
        return colorMap[status] || 'default';
    }, []);

    const isActiveBooking = useCallback((booking: BookingResponse): boolean => {
        return booking.status === 'CONFIRMED' || booking.status === 'PENDING';
    }, []);

    const canCancelBooking = useCallback((booking: BookingResponse): boolean => {
        return booking.status === 'PENDING' || booking.status === 'CONFIRMED';
    }, []);

    const filterByStatus = useCallback((filterStatus: string) => {
        return bookings.filter(booking => booking.status === filterStatus);
    }, [bookings]);

    const filterBySession = useCallback((sessionId: number) => {
        return bookings.filter(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const getBookingBySessionId = useCallback((sessionId: number): BookingResponse | undefined => {
        return bookings.find(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const hasBookingForSession = useCallback((sessionId: number): boolean => {
        return bookings.some(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const getTotalSpent = useCallback((): number => {
        return bookings.reduce((total, booking) => {
            if (booking.status === 'CONFIRMED') {
                const price = parseFloat(booking.finalPrice) || 0;
                return total + price;
            }
            return total;
        }, 0);
    }, [bookings]);

    const getBookingByNumber = useCallback((bookingNumber: string): BookingResponse | undefined => {
        return bookings.find(booking => booking.bookingNumber === bookingNumber);
    }, [bookings]);

    const getCurrentPageNumber = useCallback((): number => {
        return pageData?.number ?? 0;
    }, [pageData]);

    const getSessionIds = useCallback((): number[] => {
        const sessionIds = bookings.map(booking => booking.sessionId);
        return [...new Set(sessionIds)];
    }, [bookings]);

    return {
        bookings,
        pageData,
        loading: loading || isFetching,
        error,
        loadPage,
        nextPage,
        prevPage,
        refresh,
        getStatusDisplay,
        getStatusColor,
        isActiveBooking,
        canCancelBooking,
        filterByStatus,
        filterBySession,
        getBookingBySessionId,
        hasBookingForSession,
        getTotalSpent,
        getBookingByNumber,
        getCurrentPageNumber,
        getSessionIds,
        hasBookings: bookings.length > 0,
        isEmpty: pageData?.empty || false,
        currentPage: pageData?.number || 0,
        totalPages: pageData?.totalPages || 0,
        totalElements: pageData?.totalElements || 0,
        pageSize: pageData?.size || pageSize,
        isFirstPage: pageData?.first || true,
        isLastPage: pageData?.last || true,
        canGoNext: pageData ? !pageData.last : false,
        canGoPrev: pageData ? !pageData.first : false
    };
};