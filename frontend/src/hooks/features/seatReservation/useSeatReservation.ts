import { useState, useCallback, useMemo } from 'react';
import { seatAvailabilityApi } from '@/api/seatReservationApi';
import type { SeatReservationResponse, SeatInfo } from '@/types/seatReservation';
import { useApi } from '@/hooks/common/useApi';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId?: number;
    price: number;
    ticketTypeName?: string;
}

export const useSeatAvailability = (sessionId: number, maxSeats?: number) => {
    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);

    const seatAvailabilityApiHook = useApi<SeatReservationResponse>();

    const getSeatAvailability = useCallback(async () => {
        const response = await seatAvailabilityApiHook.execute(
            () => seatAvailabilityApi.getSeatAvailability(sessionId),
            {
                cacheKey: `seat_availability_${sessionId}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [seatAvailabilityApiHook, sessionId]);

    const refreshSeatAvailability = useCallback(async () => {
        seatAvailabilityApiHook.invalidateCache(`seat_availability_${sessionId}`);
        const response = await seatAvailabilityApiHook.execute(
            () => seatAvailabilityApi.getSeatAvailability(sessionId),
            {
                cacheKey: `seat_availability_${sessionId}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [seatAvailabilityApiHook, sessionId]);

    const invalidateSeatCache = useCallback(() => {
        seatAvailabilityApiHook.invalidateCache(`seat_availability_${sessionId}`);
    }, [seatAvailabilityApiHook, sessionId]);

    const selectSeat = useCallback((seat: SeatInfo, ticketTypeId?: number) => {
        setSelectedSeats(prev => {
            if (maxSeats && prev.length >= maxSeats) {
                throw new Error(`Maximum ${maxSeats} seats allowed`);
            }

            const ticketPrice = seat.ticketPrices?.find(tp =>
                ticketTypeId ? tp.ticketTypeId === ticketTypeId : true
            );

            if (!ticketPrice) {
                throw new Error('No ticket price available');
            }

            const selectedSeat: SelectedSeat = {
                seat,
                ticketTypeId: ticketPrice.ticketTypeId,
                price: parseFloat(ticketPrice.finalPrice),
                ticketTypeName: ticketPrice.ticketTypeName,
            };

            return [...prev, selectedSeat];
        });
    }, [maxSeats]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => prev.filter(s => s.seat.id !== seatId));
    }, []);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        const seat = seatAvailabilityApiHook.data?.seats.find(s => s.id === seatId);
        if (!seat || !seat.ticketPrices) return;

        const ticketPrice = seat.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
        if (!ticketPrice) return;

        setSelectedSeats(prev =>
            prev.map(selected =>
                selected.seat.id === seatId
                    ? {
                        ...selected,
                        ticketTypeId,
                        price: parseFloat(ticketPrice.finalPrice),
                        ticketTypeName: ticketPrice.ticketTypeName,
                    }
                    : selected
            )
        );
    }, [seatAvailabilityApiHook.data]);

    const clearSelection = useCallback(() => {
        setSelectedSeats([]);
    }, []);

    const isSeatSelected = useCallback((seatId: number) => {
        return selectedSeats.some(s => s.seat.id === seatId);
    }, [selectedSeats]);

    const getSelectedSeat = useCallback((seatId: number) => {
        return selectedSeats.find(s => s.seat.id === seatId);
    }, [selectedSeats]);

    const totalPrice = useMemo(() => {
        return selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
    }, [selectedSeats]);

    return {
        data: seatAvailabilityApiHook.data,
        loading: seatAvailabilityApiHook.loading,
        error: seatAvailabilityApiHook.error,
        isSuccess: !!seatAvailabilityApiHook.data,

        selectedSeats,
        totalPrice,
        totalSelected: selectedSeats.length,

        getSeatAvailability,
        refreshSeatAvailability,
        invalidateSeatCache,
        reset: seatAvailabilityApiHook.reset,

        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        getSelectedSeat,

        availableSeatsCount: seatAvailabilityApiHook.data?.availableSeats ?? 0,
        seats: seatAvailabilityApiHook.data?.seats || [],
        hallName: seatAvailabilityApiHook.data?.hallName,
        movieTitle: seatAvailabilityApiHook.data?.movieTitle,
        basePrice: seatAvailabilityApiHook.data?.basePrice,
        hasData: !!seatAvailabilityApiHook.data,
        isSoldOut: (seatAvailabilityApiHook.data?.availableSeats ?? 0) === 0,
    };
};