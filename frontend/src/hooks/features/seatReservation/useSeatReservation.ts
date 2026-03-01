import { useState, useCallback, useMemo } from 'react';
import { seatAvailabilityApi } from '@/api/seatReservationApi';
import type { SeatReservationResponse, SeatInfo, TicketPriceInfo } from '@/types/seatReservation';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId: number;
    price: number;
    ticketTypeName: string;
}

export const useSeatAvailability = (sessionId: number, maxSeats?: number) => {
    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);

    const seatApi = useApi<SeatReservationResponse>();

    const rawLoading = seatApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!seatApi.error;

    const getSeatAvailability = useCallback(async () => {
        const response = await seatApi.execute(
            () => seatAvailabilityApi.getSeatAvailability(sessionId),
            {
                cacheKey: `seat_availability_${sessionId}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [seatApi, sessionId]);

    const refreshSeatAvailability = useCallback(async () => {
        seatApi.invalidateCache(`seat_availability_${sessionId}`);
        return getSeatAvailability();
    }, [seatApi, getSeatAvailability, sessionId]);

    const invalidateCache = useCallback(() => {
        seatApi.invalidateCache(`seat_availability_${sessionId}`);
    }, [seatApi, sessionId]);

    const getTicketPrice = useCallback((seat: SeatInfo, ticketTypeId?: number): TicketPriceInfo | null => {
        if (!seat.ticketPrices?.length) return null;

        if (ticketTypeId) {
            return seat.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId) || null;
        }

        return seat.ticketPrices[0] || null;
    }, []);

    const selectSeat = useCallback((seat: SeatInfo, ticketTypeId?: number) => {
        setSelectedSeats(prev => {
            if (maxSeats && prev.length >= maxSeats) {
                throw new Error(`Maximum ${maxSeats} seats allowed`);
            }

            const ticketPrice = getTicketPrice(seat, ticketTypeId);

            if (!ticketPrice) {
                throw new Error('No ticket price available for this seat');
            }

            const selectedSeat: SelectedSeat = {
                seat,
                ticketTypeId: ticketPrice.ticketTypeId,
                price: parseFloat(ticketPrice.finalPrice),
                ticketTypeName: ticketPrice.ticketTypeName,
            };

            return [...prev, selectedSeat];
        });
    }, [maxSeats, getTicketPrice]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => prev.filter(s => s.seat.id !== seatId));
    }, []);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        const seat = seatApi.data?.seats.find(s => s.id === seatId);
        if (!seat) return;

        const ticketPrice = getTicketPrice(seat, ticketTypeId);
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
    }, [seatApi.data, getTicketPrice]);

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

    const availableSeatsCount = seatApi.data?.availableSeats ?? 0;
    const isSoldOut = availableSeatsCount === 0;

    return {
        data: seatApi.data,
        loading,
        error,
        isSuccess: !!seatApi.data,

        selectedSeats,
        totalPrice,
        totalSelected: selectedSeats.length,

        getSeatAvailability,
        refreshSeatAvailability,
        invalidateCache,
        reset: seatApi.reset,

        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        getSelectedSeat,

        availableSeatsCount,
        seats: seatApi.data?.seats || [],
        hallName: seatApi.data?.hallName,
        movieTitle: seatApi.data?.movieTitle,
        basePrice: seatApi.data?.basePrice,
        hasData: !!seatApi.data,
        isSoldOut,
    };
};