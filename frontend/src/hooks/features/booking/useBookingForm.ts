import { useState, useCallback } from 'react';
import { useBooking } from './useBooking';
import type { BookingResponse, BookingCreateRequest, SeatSelectionRequest } from '@/types/booking';

export const useBookingForm = () => {
    const { create, getAvailableBonusPoints, loading, error, clearError } = useBooking();
    const [success, setSuccess] = useState(false);
    const [availablePoints, setAvailablePoints] = useState<number>(0);

    const handleCreate = useCallback(async (data: BookingCreateRequest): Promise<BookingResponse | null> => {
        clearError();
        setSuccess(false);
        try {
            const booking = await create(data);
            setSuccess(true);
            return booking;
        } catch {
            return null;
        }
    }, [create, clearError]);

    const calculateAvailableBonusPoints = useCallback(async (totalPrice: string): Promise<number> => {
        clearError();
        try {
            const points = await getAvailableBonusPoints(totalPrice);
            setAvailablePoints(points);
            return points;
        } catch {
            setAvailablePoints(0);
            return 0;
        }
    }, [getAvailableBonusPoints, clearError]);

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

    const getDefaultValues = useCallback((): BookingCreateRequest => ({
        sessionId: 0,
        seats: [],
        bonusPointsToUse: 0
    }), []);

    const createSeatSelection = useCallback((seatId: number, ticketTypeId: number): SeatSelectionRequest => ({
        seatId,
        ticketTypeId
    }), []);

    const validateSeats = useCallback((selections: SeatSelectionRequest[]): boolean => {
        if (selections.length === 0) return false;

        const seatIds = selections.map(s => s.seatId);
        const uniqueSeatIds = new Set(seatIds);
        return seatIds.length === uniqueSeatIds.size;
    }, []);

    const reset = useCallback(() => {
        setSuccess(false);
        setAvailablePoints(0);
        clearError();
    }, [clearError]);

    return {
        loading,
        error,
        success,
        availablePoints,
        handleCreate,
        calculateAvailableBonusPoints,
        calculateTotalPrice,
        calculateFinalPrice,
        getDefaultValues,
        createSeatSelection,
        validateSeats,
        reset,
        isSubmitting: loading
    };
};