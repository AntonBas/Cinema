import { useState, useCallback, useRef } from 'react';
import { bookingApi } from '@/api/bookingApi';
import type { BookingResponse, BookingCreateRequest, BookingStatus, SeatSelectionRequest } from '@/types/booking';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useBooking = () => {
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [pageData, setPageData] = useState<PageResponse<BookingResponse> | null>(null);
    const [cancellingId, setCancellingId] = useState<number | null>(null);
    const [currentStatus, setCurrentStatus] = useState<BookingStatus | undefined>(undefined);

    const apiHookRef = useRef(useApi<PageResponse<BookingResponse>>());
    const apiHook = apiHookRef.current;

    const createHook = useApi<BookingResponse>();
    const getByIdHook = useApi<BookingResponse>();
    const cancelHook = useApi<void>();

    const create = useCallback(async (request: BookingCreateRequest): Promise<BookingResponse> => {
        return createHook.callApi(async () => {
            const response = await bookingApi.create(request);
            setBookings(prev => [response, ...prev]);
            return response;
        });
    }, [createHook]);

    const getById = useCallback(async (bookingId: number): Promise<BookingResponse> => {
        return getByIdHook.callApi(async () => {
            return await bookingApi.getById(bookingId);
        });
    }, [getByIdHook]);

    const getUserBookings = useCallback(async (status?: BookingStatus, page: number = 0, size: number = 20): Promise<PageResponse<BookingResponse>> => {
        setCurrentStatus(status);
        return apiHook.callApi(async () => {
            const response = await bookingApi.getUserBookings(status, page, size);
            setBookings(response.content);
            setPageData(response);
            return response;
        });
    }, [apiHook]);

    const cancel = useCallback(async (bookingId: number): Promise<void> => {
        setCancellingId(bookingId);
        try {
            await cancelHook.callApi(async () => {
                await bookingApi.cancel(bookingId);
                setBookings(prevBookings => prevBookings.filter(booking => booking.id !== bookingId));
            });
        } finally {
            setCancellingId(null);
        }
    }, [cancelHook]);

    const calculateTotalPrice = useCallback((selections: Array<{ price: string }>): string => {
        const total = selections.reduce((sum, selection) => {
            const price = parseFloat(selection.price) || 0;
            return sum + price;
        }, 0);
        return total.toFixed(2);
    }, []);

    const calculateFinalPrice = useCallback((totalPrice: string, bonusPoints: number, pointValue: number = 0.01): string => {
        const price = parseFloat(totalPrice) || 0;
        const discount = bonusPoints * pointValue;
        const finalPrice = Math.max(0, price - discount);
        return finalPrice.toFixed(2);
    }, []);

    const validateSeats = useCallback((selections: SeatSelectionRequest[]): boolean => {
        if (selections.length === 0) return false;
        const seatIds = selections.map(s => s.seatId);
        const uniqueSeatIds = new Set(seatIds);
        return seatIds.length === uniqueSeatIds.size;
    }, []);

    const isActiveBooking = useCallback((booking: BookingResponse): boolean => {
        return booking.status === 'CONFIRMED' || booking.status === 'PENDING';
    }, []);

    const canCancelBooking = useCallback((booking: BookingResponse): boolean => {
        return booking.status === 'PENDING' || booking.status === 'CONFIRMED';
    }, []);

    const getBookingBySessionId = useCallback((sessionId: number): BookingResponse | undefined => {
        return bookings.find(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const hasBookingForSession = useCallback((sessionId: number): boolean => {
        return bookings.some(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const getBookingsBySessionId = useCallback((sessionId: number): BookingResponse[] => {
        return bookings.filter(booking => booking.sessionId === sessionId);
    }, [bookings]);

    const getBookingForSeat = useCallback((sessionId: number, seatId: number): BookingResponse | undefined => {
        return bookings.find(booking => {
            if (booking.sessionId !== sessionId) return false;
            return booking.bookedSeats.some(seat => seat.seatId === seatId);
        });
    }, [bookings]);

    const isSeatBookedInSession = useCallback((sessionId: number, seatId: number): boolean => {
        return bookings.some(booking => {
            if (booking.sessionId !== sessionId) return false;
            return booking.bookedSeats.some(seat => seat.seatId === seatId);
        });
    }, [bookings]);

    const getBookedSeatsForSession = useCallback((sessionId: number): number[] => {
        const sessionBookings = getBookingsBySessionId(sessionId);
        const seatIds: number[] = [];
        sessionBookings.forEach(booking => {
            booking.bookedSeats.forEach(seat => {
                seatIds.push(seat.seatId);
            });
        });
        return seatIds;
    }, [getBookingsBySessionId]);

    const getTotalSpent = useCallback((): number => {
        return bookings.reduce((total, booking) => {
            if (booking.status === 'CONFIRMED') {
                const price = parseFloat(booking.finalPrice) || 0;
                return total + price;
            }
            return total;
        }, 0);
    }, [bookings]);

    const filterByStatus = useCallback((status: BookingStatus): BookingResponse[] => {
        return bookings.filter(booking => booking.status === status);
    }, [bookings]);

    const refresh = useCallback(() => {
        if (pageData) {
            getUserBookings(currentStatus, pageData.number, pageData.size);
        }
    }, [pageData, currentStatus]);

    const isCancelling = useCallback((bookingId: number) => {
        return cancellingId === bookingId;
    }, [cancellingId]);

    const isExpiringSoon = useCallback((expiresAt: string, thresholdMinutes: number = 15): boolean => {
        const now = new Date();
        const expiry = new Date(expiresAt);
        const diffMs = expiry.getTime() - now.getTime();
        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        return diffMinutes > 0 && diffMinutes <= thresholdMinutes;
    }, []);

    const isSessionSoon = useCallback((sessionTime: string, thresholdHours: number = 1): boolean => {
        const now = new Date();
        const session = new Date(sessionTime);
        const diffMs = session.getTime() - now.getTime();
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        return diffHours > 0 && diffHours <= thresholdHours;
    }, []);

    const getBookingTimeLeft = useCallback((expiresAt: string): string => {
        const now = new Date();
        const expiry = new Date(expiresAt);
        const diffMs = expiry.getTime() - now.getTime();
        if (diffMs <= 0) return 'Expired';
        const diffMinutes = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMinutes / 60);
        if (diffHours > 0) {
            return `${diffHours}h ${diffMinutes % 60}m`;
        }
        return `${diffMinutes}m`;
    }, []);

    const loadPage = useCallback(async (page: number = 0, size?: number) => {
        const pageSize = size || pageData?.size || 20;
        return await getUserBookings(currentStatus, page, pageSize);
    }, [currentStatus, pageData]);

    const nextPage = useCallback(async () => {
        if (pageData && !pageData.last) {
            return await loadPage(pageData.number + 1);
        }
        return null;
    }, [pageData]);

    const prevPage = useCallback(async () => {
        if (pageData && !pageData.first) {
            return await loadPage(pageData.number - 1);
        }
        return null;
    }, [pageData]);

    return {
        bookings,
        pageData,
        loading: createHook.loading || getByIdHook.loading || apiHook.loading || cancelHook.loading,
        cancellingId,
        currentStatus,
        create,
        getById,
        getUserBookings,
        cancel,
        calculateTotalPrice,
        calculateFinalPrice,
        validateSeats,
        isActiveBooking,
        canCancelBooking,
        getBookingBySessionId,
        hasBookingForSession,
        getBookingsBySessionId,
        getBookingForSeat,
        isSeatBookedInSession,
        getBookedSeatsForSession,
        getTotalSpent,
        filterByStatus,
        refresh,
        isCancelling,
        isExpiringSoon,
        isSessionSoon,
        getBookingTimeLeft,
        loadPage,
        nextPage,
        prevPage,
        currentPage: pageData?.number || 0,
        totalPages: pageData?.totalPages || 0,
        totalElements: pageData?.totalElements || 0,
        pageSize: pageData?.size || 0,
        isEmpty: pageData?.empty || false,
        isFirstPage: pageData?.first || true,
        isLastPage: pageData?.last || true,
    };
};