import { useState, useCallback, useMemo } from 'react';
import { seatReservationApi } from '@/api/seatReservationApi';
import type { SeatReservationResponse, SeatInfo, TicketPriceInfo } from '@/types/seatReservation';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { useNotification } from '@/hooks/common/useNotification';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId: number;
    price: number;
    ticketTypeName: string;
}

export const useSeatReservation = (sessionId: number, maxSeats?: number) => {
    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);
    const [loadingSeats, setLoadingSeats] = useState<number[]>([]);

    const { showNotification } = useNotification();
    const seatApi = useApi<SeatReservationResponse>();
    const holdApi = useApi<void>();

    const loading = useDelayedLoading(seatApi.loading || holdApi.loading, { delay: 150, minDisplayTime: 300 });
    const error = !!seatApi.error || !!holdApi.error;

    const getSeatAvailability = useCallback(async () => {
        const response = await seatApi.execute(
            () => seatReservationApi.getSeatAvailability(sessionId),
            {
                cacheKey: `seat_availability_${sessionId}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [seatApi, sessionId]);

    const invalidateCache = useCallback(() => {
        seatApi.invalidateCache(`seat_availability_${sessionId}`);
    }, [seatApi, sessionId]);

    const updateSeatLocally = useCallback((seatId: number, updates: Partial<SeatInfo>) => {
        if (!seatApi.data) return;

        const updatedSeats = seatApi.data.seats.map(seat =>
            seat.id === seatId ? { ...seat, ...updates } : seat
        );

        const newAvailableSeats = updatedSeats.filter(seat => seat.available).length;

        seatApi.setData({
            ...seatApi.data,
            seats: updatedSeats,
            availableSeats: newAvailableSeats
        });
    }, [seatApi]);

    const temporaryHoldSeat = useCallback(async (seatId: number) => {
        setLoadingSeats(prev => [...prev, seatId]);

        let success = false;

        try {
            const response = await holdApi.execute(
                () => seatReservationApi.temporaryHoldSeat(sessionId, seatId),
                { showErrorNotification: false }
            );
            success = response !== null;
        } catch (error: any) {
            if (error?.response?.status === 409) {
                showNotification('This seat is already reserved', 'warning');
            } else {
                showNotification(error?.message || 'Failed to reserve seat', 'error');
            }
        }

        if (success) {
            updateSeatLocally(seatId, { available: false, temporarilyReserved: true });
        }

        setLoadingSeats(prev => prev.filter(id => id !== seatId));
        return success;
    }, [holdApi, sessionId, updateSeatLocally, showNotification]);

    const cancelTemporaryHold = useCallback(async (seatId: number) => {
        setLoadingSeats(prev => [...prev, seatId]);

        let success = false;

        try {
            const response = await holdApi.execute(
                () => seatReservationApi.cancelTemporaryHold(sessionId, seatId),
                { showErrorNotification: false }
            );
            success = response !== null;
        } catch (error: any) {
            console.error('Failed to cancel hold:', error);
        }

        if (success) {
            updateSeatLocally(seatId, { available: true, temporarilyReserved: false });
        }

        setLoadingSeats(prev => prev.filter(id => id !== seatId));
        return success;
    }, [holdApi, sessionId, updateSeatLocally]);

    const getTicketPrice = useCallback((seat: SeatInfo, ticketTypeId?: number): TicketPriceInfo | null => {
        if (!seat.ticketPrices?.length) return null;
        if (ticketTypeId) {
            return seat.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId) || null;
        }
        return seat.ticketPrices[0] || null;
    }, []);

    const selectSeat = useCallback(async (seat: SeatInfo, ticketTypeId?: number) => {
        if (maxSeats && selectedSeats.length >= maxSeats) {
            showNotification(`Maximum ${maxSeats} seats allowed`, 'error');
            return;
        }

        if (!seat.available || seat.temporarilyReserved) {
            showNotification('This seat is not available', 'error');
            return;
        }

        const ticketPrice = getTicketPrice(seat, ticketTypeId);
        if (!ticketPrice) {
            showNotification('No ticket price available for this seat', 'error');
            return;
        }

        const success = await temporaryHoldSeat(seat.id);
        if (success) {
            setSelectedSeats(prev => [...prev, {
                seat,
                ticketTypeId: ticketPrice.ticketTypeId,
                price: parseFloat(ticketPrice.finalPrice),
                ticketTypeName: ticketPrice.ticketTypeName,
            }]);
        }
    }, [maxSeats, selectedSeats.length, getTicketPrice, temporaryHoldSeat, showNotification]);

    const deselectSeat = useCallback(async (seatId: number) => {
        const success = await cancelTemporaryHold(seatId);
        if (success) {
            setSelectedSeats(prev => prev.filter(s => s.seat.id !== seatId));
        }
    }, [cancelTemporaryHold]);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        const seat = seatApi.data?.seats.find(s => s.id === seatId);
        if (!seat) return;

        const ticketPrice = getTicketPrice(seat, ticketTypeId);
        if (!ticketPrice) return;

        setSelectedSeats(prev =>
            prev.map(selected =>
                selected.seat.id === seatId
                    ? { ...selected, ticketTypeId, price: parseFloat(ticketPrice.finalPrice), ticketTypeName: ticketPrice.ticketTypeName }
                    : selected
            )
        );
    }, [seatApi.data, getTicketPrice]);

    const clearSelection = useCallback(() => {
        selectedSeats.forEach(seat => {
            cancelTemporaryHold(seat.seat.id);
        });
        setSelectedSeats([]);
    }, [selectedSeats, cancelTemporaryHold]);

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
        data: seatApi.data,
        loading,
        error,
        selectedSeats,
        totalPrice,
        totalSelected: selectedSeats.length,
        loadingSeats,
        getSeatAvailability,
        invalidateCache,
        reset: seatApi.reset,
        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        getSelectedSeat,
        availableSeatsCount: seatApi.data?.availableSeats ?? 0,
        seats: seatApi.data?.seats || [],
        hallName: seatApi.data?.hallName,
        movieTitle: seatApi.data?.movieTitle,
        basePrice: seatApi.data?.basePrice,
        hasData: !!seatApi.data,
        isSoldOut: (seatApi.data?.availableSeats ?? 0) === 0,
    };
};