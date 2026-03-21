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
    const [seatData, setSeatData] = useState<SeatReservationResponse | null>(null);
    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);
    const [loadingSeats, setLoadingSeats] = useState<number[]>([]);

    const { showNotification } = useNotification();

    const seatApi = useApi<SeatReservationResponse>();
    const holdApi = useApi<void>();

    const rawLoading = seatApi.loading || holdApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
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
        if (response) {
            setSeatData(response);
        }
        return response || null;
    }, [seatApi, sessionId]);

    const invalidateCache = useCallback(() => {
        seatApi.invalidateCache(`seat_availability_${sessionId}`);
    }, [seatApi, sessionId]);

    const temporaryHoldSeat = useCallback(async (seatId: number) => {
        setLoadingSeats(prev => [...prev, seatId]);

        let success = false;

        try {
            const response = await holdApi.execute(
                () => seatReservationApi.temporaryHoldSeat(sessionId, seatId),
                {
                    showErrorNotification: false,
                }
            );
            success = response !== null;
        } catch (error: any) {
            if (error?.response?.status === 409) {
                showNotification('This seat is already reserved', 'warning');
            } else {
                showNotification(error?.message || 'Failed to reserve seat', 'error');
            }
        }

        if (success && seatData) {
            const updatedSeats = seatData.seats.map(seat => {
                if (seat.id === seatId) {
                    return {
                        ...seat,
                        available: false,
                        temporarilyReserved: true
                    };
                }
                return seat;
            });

            const newAvailableSeats = updatedSeats.filter(seat => seat.available).length;

            setSeatData({
                ...seatData,
                seats: updatedSeats,
                availableSeats: newAvailableSeats
            });
        }

        setLoadingSeats(prev => prev.filter(id => id !== seatId));

        return success;
    }, [holdApi, sessionId, seatData, showNotification]);

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
            const selectedSeat: SelectedSeat = {
                seat,
                ticketTypeId: ticketPrice.ticketTypeId,
                price: parseFloat(ticketPrice.finalPrice),
                ticketTypeName: ticketPrice.ticketTypeName,
            };

            setSelectedSeats(prev => [...prev, selectedSeat]);
        }
    }, [maxSeats, selectedSeats.length, getTicketPrice, temporaryHoldSeat, showNotification]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => prev.filter(s => s.seat.id !== seatId));
        if (seatData) {
            const updatedSeats = seatData.seats.map(seat => {
                if (seat.id === seatId) {
                    return {
                        ...seat,
                        available: true,
                        temporarilyReserved: false
                    };
                }
                return seat;
            });

            const newAvailableSeats = updatedSeats.filter(seat => seat.available).length;

            setSeatData({
                ...seatData,
                seats: updatedSeats,
                availableSeats: newAvailableSeats
            });
        }
    }, [seatData]);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        const seat = seatData?.seats.find(s => s.id === seatId);
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
    }, [seatData, getTicketPrice]);

    const clearSelection = useCallback(() => {
        if (seatData) {
            const resetSeats = seatData.seats.map(seat => {
                if (selectedSeats.some(s => s.seat.id === seat.id)) {
                    return {
                        ...seat,
                        available: true,
                        temporarilyReserved: false
                    };
                }
                return seat;
            });

            const newAvailableSeats = resetSeats.filter(seat => seat.available).length;

            setSeatData({
                ...seatData,
                seats: resetSeats,
                availableSeats: newAvailableSeats
            });
        }
        setSelectedSeats([]);
    }, [seatData, selectedSeats]);

    const isSeatSelected = useCallback((seatId: number) => {
        return selectedSeats.some(s => s.seat.id === seatId);
    }, [selectedSeats]);

    const getSelectedSeat = useCallback((seatId: number) => {
        return selectedSeats.find(s => s.seat.id === seatId);
    }, [selectedSeats]);

    const totalPrice = useMemo(() => {
        return selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
    }, [selectedSeats]);

    const availableSeatsCount = seatData?.availableSeats ?? 0;
    const isSoldOut = availableSeatsCount === 0;

    return {
        data: seatData,
        loading,
        error,
        isSuccess: !!seatData,

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

        availableSeatsCount,
        seats: seatData?.seats || [],
        hallName: seatData?.hallName,
        movieTitle: seatData?.movieTitle,
        basePrice: seatData?.basePrice,
        hasData: !!seatData,
        isSoldOut,
    };
};