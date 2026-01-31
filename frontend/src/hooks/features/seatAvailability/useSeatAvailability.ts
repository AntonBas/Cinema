import { useState, useCallback } from 'react';
import type { SeatAvailabilityResponse, SeatInfo } from '@/types/seatAvailability';
import { useApi } from '@/hooks/common/useApi';

export interface SelectedSeat {
    seat: SeatInfo;
    ticketTypeId?: number;
    price: number;
}

export const useSeatAvailability = (sessionId: number) => {
    const [seatData, setSeatData] = useState<SeatAvailabilityResponse | null>(null);
    const [availableSeatsCount, setAvailableSeatsCount] = useState<number | null>(null);
    const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);
    const [totalPrice, setTotalPrice] = useState(0);

    const fetchSeatHook = useApi<SeatAvailabilityResponse>();
    const checkSeatHook = useApi<boolean>();

    const getAuthHeaders = useCallback((): HeadersInit => {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
        };
    }, []);

    const fetchSeatAvailability = useCallback(async (): Promise<SeatAvailabilityResponse> => {
        return fetchSeatHook.callApi(async () => {
            const response = await fetch(`/api/sessions/${sessionId}/seats/availability`, {
                headers: getAuthHeaders(),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw errorData;
            }

            const data = await response.json();
            setSeatData(data);
            setAvailableSeatsCount(data.availableSeats);
            return data;
        }, { showErrorNotification: false });
    }, [sessionId, fetchSeatHook, getAuthHeaders]);

    const checkSpecificSeat = useCallback(async (seatId: number): Promise<boolean> => {
        if (!seatData) return false;

        return checkSeatHook.callApi(async () => {
            const response = await fetch(`/api/sessions/${sessionId}/seats/${seatId}/availability`, {
                headers: getAuthHeaders(),
            });

            if (!response.ok) {
                throw new Error('Seat not available');
            }

            return true;
        }, { showErrorNotification: false }).catch(() => false);
    }, [sessionId, seatData, checkSeatHook, getAuthHeaders]);

    const getSeatInfo = useCallback((seatId: number) => {
        if (!seatData) return null;
        return seatData.seats.find(seat => seat.id === seatId) || null;
    }, [seatData]);

    const filterSeatsByRow = useCallback((row: number) => {
        if (!seatData) return [];
        return seatData.seats.filter(seat => seat.row === row);
    }, [seatData]);

    const getSeatPrice = useCallback((seatId: number, ticketTypeId?: number) => {
        const seatInfo = getSeatInfo(seatId);
        if (!seatInfo || !seatInfo.ticketPrices || seatInfo.ticketPrices.length === 0) return null;

        if (ticketTypeId) {
            const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
            return ticketPrice ? parseFloat(ticketPrice.finalPrice) : null;
        }

        return parseFloat(seatInfo.ticketPrices[0].finalPrice);
    }, [getSeatInfo]);

    const selectSeat = useCallback(async (seatId: number, ticketTypeId?: number, maxSeats?: number) => {
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

        const newTotalPrice = [...selectedSeats, { seat: seatInfo, ticketTypeId: finalTicketTypeId, price }]
            .reduce((sum, selectedSeat) => sum + selectedSeat.price, 0);
        setTotalPrice(newTotalPrice);
    }, [seatData, selectedSeats]);

    const deselectSeat = useCallback((seatId: number) => {
        setSelectedSeats(prev => {
            const newSelectedSeats = prev.filter(selected => selected.seat.id !== seatId);
            const newTotalPrice = newSelectedSeats.reduce((sum, selectedSeat) => sum + selectedSeat.price, 0);
            setTotalPrice(newTotalPrice);
            return newSelectedSeats;
        });
    }, []);

    const updateSeatTicketType = useCallback((seatId: number, ticketTypeId: number) => {
        if (!seatData) return;

        const seatInfo = seatData.seats.find(seat => seat.id === seatId);
        if (!seatInfo || !seatInfo.ticketPrices) return;

        const ticketPrice = seatInfo.ticketPrices.find(tp => tp.ticketTypeId === ticketTypeId);
        if (!ticketPrice) return;

        const price = parseFloat(ticketPrice.finalPrice);

        setSelectedSeats(prev => {
            const updatedSeats = prev.map(selected =>
                selected.seat.id === seatId
                    ? { ...selected, ticketTypeId, price }
                    : selected
            );

            const newTotalPrice = updatedSeats.reduce((sum, selectedSeat) => sum + selectedSeat.price, 0);
            setTotalPrice(newTotalPrice);
            return updatedSeats;
        });
    }, [seatData]);

    const clearSelection = useCallback(() => {
        setSelectedSeats([]);
        setTotalPrice(0);
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
        seatData,
        availableSeatsCount,
        selectedSeats,
        totalPrice,
        loading: fetchSeatHook.loading || checkSeatHook.loading,
        fetchSeatAvailability,
        checkSpecificSeat,
        getSeatInfo,
        filterSeatsByRow,
        getSeatPrice,
        selectSeat,
        deselectSeat,
        updateSeatTicketType,
        clearSelection,
        isSeatSelected,
        validateSelection,
        hasData: !!seatData,
        totalSeats: seatData?.seats.length || 0,
        occupiedSeats: seatData ? seatData.seats.length - seatData.availableSeats : 0,
        seats: seatData?.seats || [],
        hallName: seatData?.hallName,
        movieTitle: seatData?.movieTitle,
        basePrice: seatData?.basePrice,
        count: selectedSeats.length,
        hasSelection: selectedSeats.length > 0,
    };
};