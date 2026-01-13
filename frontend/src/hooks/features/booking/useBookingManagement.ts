import { useState, useCallback } from 'react';
import { useBooking } from './useBooking';
import type { BookingResponse } from '@/types/booking';

export const useBookingManagement = () => {
    const { cancel, loading, error, clearError } = useBooking();
    const [cancellingId, setCancellingId] = useState<number | null>(null);

    const handleCancel = useCallback(async (bookingId: number): Promise<boolean> => {
        setCancellingId(bookingId);
        clearError();
        try {
            await cancel(bookingId);
            return true;
        } catch {
            return false;
        } finally {
            setCancellingId(null);
        }
    }, [cancel, clearError]);

    const isBookingCancellable = useCallback((booking: BookingResponse): boolean => {
        return booking.status === 'PENDING' || booking.status === 'CONFIRMED';
    }, []);

    const isCancelling = useCallback((bookingId: number) => {
        return cancellingId === bookingId;
    }, [cancellingId]);

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

    const canCancelBasedOnSessionTime = useCallback((booking: BookingResponse): boolean => {
        const sessionTime = new Date(booking.sessionTime);
        const now = new Date();
        const diffHours = (sessionTime.getTime() - now.getTime()) / (1000 * 60 * 60);

        return diffHours > 1 && isBookingCancellable(booking);
    }, [isBookingCancellable]);

    const getSeatSummary = useCallback((seats: Array<{ row: number; seatNumber: number; ticketTypeName: string }>): string => {
        if (seats.length === 0) return 'No seats selected';
        if (seats.length === 1) {
            const seat = seats[0];
            return `Row ${seat.row}, Seat ${seat.seatNumber} (${seat.ticketTypeName})`;
        }
        return `${seats.length} seats`;
    }, []);

    const getBookingSummary = useCallback((booking: BookingResponse): string => {
        const sessionDate = new Date(booking.sessionTime);
        const dateStr = sessionDate.toLocaleDateString();
        const timeStr = sessionDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

        return `${booking.movieTitle} - ${dateStr} ${timeStr} (${booking.bookedSeats.length} seat${booking.bookedSeats.length !== 1 ? 's' : ''})`;
    }, []);

    const getBookingSessionInfo = useCallback((booking: BookingResponse): { date: string; time: string } => {
        const sessionDate = new Date(booking.sessionTime);
        return {
            date: sessionDate.toLocaleDateString(),
            time: sessionDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        };
    }, []);

    return {
        loading,
        error,
        cancellingId,
        handleCancel,
        isBookingCancellable,
        canCancelBasedOnSessionTime,
        isCancelling,
        getBookingTimeLeft,
        isExpiringSoon,
        isSessionSoon,
        getSeatSummary,
        getBookingSummary,
        getBookingSessionInfo,
        clearError
    };
};