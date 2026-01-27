import { useState, useCallback, useEffect } from 'react';
import type { SeatInfo } from '@/types/seatAvailability';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId?: number;
    price: number;
}

interface UseSeatSelectionProps {
    seatData: {
        seats: SeatInfo[];
        availableSeats: number;
        movieTitle: string;
        hallName: string;
        basePrice: string;
        sessionId: number;
    } | null;
    checkSpecificSeat: (seatId: number) => Promise<boolean>;
    maxSeats?: number;
}

export const useSeatSelection = ({ seatData, checkSpecificSeat, maxSeats }: UseSeatSelectionProps) => {
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

        if (!seatData) {
            throw new Error('Seat data not loaded');
        }

        const seatInfo = seatData.seats.find(seat => seat.id === seatId);
        if (!seatInfo) {
            throw new Error('Seat not found');
        }

        if (!seatInfo.available) {
            throw new Error('Seat is not available');
        }

        if (seatInfo.temporarilyReserved) {
            throw new Error('Seat is temporarily reserved');
        }

        let price = 0;
        let finalTicketTypeId = ticketTypeId;

        if (!seatInfo.ticketPrices || seatInfo.ticketPrices.length === 0) {
            throw new Error('No ticket prices available for this seat');
        }

        if (finalTicketTypeId) {
            const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === finalTicketTypeId);
            if (!ticketPrice) {
                throw new Error('Invalid ticket type');
            }
            price = parseFloat(ticketPrice.finalPrice);
        } else {
            finalTicketTypeId = seatInfo.ticketPrices[0].ticketTypeId;
            price = parseFloat(seatInfo.ticketPrices[0].finalPrice);
        }

        setSelectedSeats(prev => [...prev, {
            seat: seatInfo,
            ticketTypeId: finalTicketTypeId,
            price
        }]);
    }, [seatData, selectedSeats.length, maxSeats]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => prev.filter(selected => selected.seat.id !== seatId));
    }, []);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        if (!seatData) return;

        const seatInfo = seatData.seats.find(seat => seat.id === seatId);
        if (!seatInfo || !seatInfo.ticketPrices) return;

        const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
        if (!ticketPrice) return;

        const price = parseFloat(ticketPrice.finalPrice);

        setSelectedSeats(prev => prev.map(selected =>
            selected.seat.id === seatId
                ? { ...selected, ticketTypeId, price }
                : selected
        ));
    }, [seatData]);

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
        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        validateSelection,
        count: selectedSeats.length,
        hasSelection: selectedSeats.length > 0,
        isAtMax: maxSeats ? selectedSeats.length >= maxSeats : false
    };
};