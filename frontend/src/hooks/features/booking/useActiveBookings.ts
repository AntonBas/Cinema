import { useState, useCallback, useEffect } from 'react';
import { useBookingList } from './useBookingList';
import type { BookingResponse } from '@/types/booking';

export const useActiveBookings = (options?: { autoFetch?: boolean; checkExpiry?: boolean }) => {
    const { autoFetch = true, checkExpiry = true } = options || {};
    const { bookings, loading, error, refresh, isActiveBooking } = useBookingList({
        autoFetch
    });

    const [activeBookings, setActiveBookings] = useState<BookingResponse[]>([]);
    const [expiringBookings, setExpiringBookings] = useState<BookingResponse[]>([]);

    const updateActiveBookings = useCallback(() => {
        const active = bookings.filter(booking => isActiveBooking(booking));
        setActiveBookings(active);

        if (checkExpiry) {
            const now = new Date();
            const expiring = active.filter(booking => {
                const expiresAt = new Date(booking.expiresAt);
                const diffMs = expiresAt.getTime() - now.getTime();
                const diffMinutes = Math.floor(diffMs / (1000 * 60));
                return diffMinutes > 0 && diffMinutes <= 30;
            });
            setExpiringBookings(expiring);
        }
    }, [bookings, isActiveBooking, checkExpiry]);

    useEffect(() => {
        updateActiveBookings();
    }, [updateActiveBookings]);

    const getUpcomingBookings = useCallback((): BookingResponse[] => {
        const now = new Date();
        return activeBookings.filter(booking => {
            const sessionTime = new Date(booking.sessionTime);
            return sessionTime > now;
        }).sort((a, b) => {
            const timeA = new Date(a.sessionTime).getTime();
            const timeB = new Date(b.sessionTime).getTime();
            return timeA - timeB;
        });
    }, [activeBookings]);

    const getPastBookings = useCallback((): BookingResponse[] => {
        const now = new Date();
        return activeBookings.filter(booking => {
            const sessionTime = new Date(booking.sessionTime);
            return sessionTime <= now;
        }).sort((a, b) => {
            const timeA = new Date(a.sessionTime).getTime();
            const timeB = new Date(b.sessionTime).getTime();
            return timeB - timeA;
        });
    }, [activeBookings]);

    const getNextBooking = useCallback((): BookingResponse | undefined => {
        const upcoming = getUpcomingBookings();
        return upcoming[0];
    }, [getUpcomingBookings]);

    const getBookingBySessionId = useCallback((sessionId: number): BookingResponse | undefined => {
        return activeBookings.find(booking => booking.sessionId === sessionId);
    }, [activeBookings]);

    const getBookingsBySessionId = useCallback((sessionId: number): BookingResponse[] => {
        return activeBookings.filter(booking => booking.sessionId === sessionId);
    }, [activeBookings]);

    const hasBookingForSession = useCallback((sessionId: number): boolean => {
        return activeBookings.some(booking => booking.sessionId === sessionId);
    }, [activeBookings]);

    const hasActiveBookings = useCallback((): boolean => {
        return activeBookings.length > 0;
    }, [activeBookings]);

    const hasExpiringBookings = useCallback((): boolean => {
        return expiringBookings.length > 0;
    }, [expiringBookings]);

    const getTotalActiveSpent = useCallback((): number => {
        return activeBookings.reduce((total, booking) => {
            if (booking.status === 'CONFIRMED') {
                const price = parseFloat(booking.finalPrice) || 0;
                return total + price;
            }
            return total;
        }, 0);
    }, [activeBookings]);

    const getBookingForSeat = useCallback((sessionId: number, seatId: number): BookingResponse | undefined => {
        return activeBookings.find(booking => {
            if (booking.sessionId !== sessionId) return false;
            return booking.bookedSeats.some(seat => seat.seatId === seatId);
        });
    }, [activeBookings]);

    const isSeatBookedInSession = useCallback((sessionId: number, seatId: number): boolean => {
        return activeBookings.some(booking => {
            if (booking.sessionId !== sessionId) return false;
            return booking.bookedSeats.some(seat => seat.seatId === seatId);
        });
    }, [activeBookings]);

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

    return {
        activeBookings,
        expiringBookings,
        loading,
        error,
        refresh,
        getUpcomingBookings,
        getPastBookings,
        getNextBooking,
        getBookingBySessionId,
        getBookingsBySessionId,
        hasBookingForSession,
        getBookingForSeat,
        isSeatBookedInSession,
        getBookedSeatsForSession,
        hasActiveBookings,
        hasExpiringBookings,
        getTotalActiveSpent,
        totalActive: activeBookings.length,
        totalExpiring: expiringBookings.length,
        totalSpent: getTotalActiveSpent()
    };
};