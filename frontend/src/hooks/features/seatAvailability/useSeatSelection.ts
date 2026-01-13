import { useState, useCallback, useEffect } from 'react';
import type { SeatInfo } from '@/types/seatAvailability';
import { useSeatAvailability } from './useSeatAvailability';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId?: number;
    price: number;
}

export const useSeatSelection = (sessionId: number, maxSeats?: number) => {
    const {
        seatData,
        getSeatInfo,
        checkSpecificSeat,
        loading,
        error
    } = useSeatAvailability(sessionId);

    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);
    const [totalPrice, setTotalPrice] = useState(0);

    useEffect(() => {
        const newTotalPrice = selectedSeats.reduce((sum, selectedSeat) => sum + selectedSeat.price, 0);
        setTotalPrice(newTotalPrice);
    }, [selectedSeats]);

    const selectSeat = useCallback(async (seatId: number, ticketTypeId?: number) => {
        if (maxSeats && selectedSeats.length >= maxSeats) {
            throw new Error(`Maximum ${maxSeats} seats allowed`);
        }

        const seatInfo = getSeatInfo(seatId);
        if (!seatInfo) {
            throw new Error('Seat not found');
        }

        if (!seatInfo.available) {
            throw new Error('Seat is not available');
        }

        const isAvailable = await checkSpecificSeat(seatId);
        if (!isAvailable) {
            throw new Error('Seat is no longer available');
        }

        let price = 0;
        if (ticketTypeId) {
            const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
            price = ticketPrice ? parseFloat(ticketPrice.finalPrice) : 0;
        } else {
            price = seatInfo.ticketPrices[0] ? parseFloat(seatInfo.ticketPrices[0].finalPrice) : 0;
        }

        setSelectedSeats(prev => [...prev, { seat: seatInfo, ticketTypeId, price }]);
    }, [getSeatInfo, checkSpecificSeat, selectedSeats.length, maxSeats]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => prev.filter(selected => selected.seat.id !== seatId));
    }, []);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        const seatInfo = getSeatInfo(seatId);
        if (!seatInfo) return;

        const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
        if (!ticketPrice) return;

        const price = parseFloat(ticketPrice.finalPrice);

        setSelectedSeats(prev => prev.map(selected =>
            selected.seat.id === seatId
                ? { ...selected, ticketTypeId, price }
                : selected
        ));
    }, [getSeatInfo]);

    const clearSelection = useCallback(() => {
        setSelectedSeats([]);
    }, []);

    const isSeatSelected = useCallback((seatId: number) => {
        return selectedSeats.some(selected => selected.seat.id === seatId);
    }, [selectedSeats]);

    const validateSelection = useCallback(async () => {
        const validationPromises = selectedSeats.map(async (selectedSeat) => {
            const isAvailable = await checkSpecificSeat(selectedSeat.seat.id);
            return { seatId: selectedSeat.seat.id, isAvailable };
        });

        const results = await Promise.all(validationPromises);
        const unavailableSeats = results.filter(result => !result.isAvailable);

        return {
            isValid: unavailableSeats.length === 0,
            unavailableSeats: unavailableSeats.map(result => result.seatId)
        };
    }, [selectedSeats, checkSpecificSeat]);

    return {
        selectedSeats,
        totalPrice,
        loading,
        error,
        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        validateSelection,
        count: selectedSeats.length,
        hasSelection: selectedSeats.length > 0,
        seatData,
        isAtMax: maxSeats ? selectedSeats.length >= maxSeats : false
    };
};