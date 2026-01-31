import { useState, useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';

export const useSeats = () => {
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [seat, setSeat] = useState<SeatResponse | null>(null);

    const getSeatsByHallHook = useApi<SeatResponse[]>();
    const getSeatByIdHook = useApi<SeatResponse>();
    const getSeatByPositionHook = useApi<SeatResponse>();
    const checkSeatAvailabilityHook = useApi<boolean>();
    const countSeatsByHallHook = useApi<number>();
    const getSeatsByTypeHook = useApi<SeatResponse[]>();
    const updateSeatTypeHook = useApi<SeatResponse>();
    const activateSeatHook = useApi<SeatResponse>();
    const deactivateSeatHook = useApi<SeatResponse>();

    const getSeatsByHall = useCallback(async (hallId: number): Promise<SeatResponse[]> => {
        return getSeatsByHallHook.callApi(async () => {
            const data = await seatApi.getSeatsByHall(hallId);
            setSeats(data);
            return data;
        }, { showErrorNotification: false });
    }, [getSeatsByHallHook]);

    const getSeatById = useCallback(async (hallId: number, seatId: number): Promise<SeatResponse> => {
        return getSeatByIdHook.callApi(async () => {
            const data = await seatApi.getSeatById(hallId, seatId);
            setSeat(data);
            return data;
        }, { showErrorNotification: false });
    }, [getSeatByIdHook]);

    const getSeatByPosition = useCallback(async (hallId: number, row: number, number: number): Promise<SeatResponse> => {
        return getSeatByPositionHook.callApi(async () => {
            const data = await seatApi.getSeatByPosition(hallId, row, number);
            setSeat(data);
            return data;
        }, { showErrorNotification: false });
    }, [getSeatByPositionHook]);

    const checkSeatAvailability = useCallback(async (hallId: number, row: number, number: number): Promise<boolean> => {
        return checkSeatAvailabilityHook.callApi(async () => {
            return await seatApi.checkSeatAvailability(hallId, row, number);
        }, { showErrorNotification: false });
    }, [checkSeatAvailabilityHook]);

    const countSeatsByHall = useCallback(async (hallId: number): Promise<number> => {
        return countSeatsByHallHook.callApi(async () => {
            return await seatApi.countSeatsByHall(hallId);
        }, { showErrorNotification: false });
    }, [countSeatsByHallHook]);

    const getSeatsByType = useCallback(async (hallId: number, seatType: SeatType): Promise<SeatResponse[]> => {
        return getSeatsByTypeHook.callApi(async () => {
            const data = await seatApi.getSeatsByType(hallId, seatType);
            setSeats(data);
            return data;
        }, { showErrorNotification: false });
    }, [getSeatsByTypeHook]);

    const getActiveSeatsByHall = useCallback(async (hallId: number): Promise<SeatResponse[]> => {
        return getSeatsByHallHook.callApi(async () => {
            const allSeats = await seatApi.getSeatsByHall(hallId);
            const activeSeats = allSeats.filter(seat => seat.active);
            setSeats(activeSeats);
            return activeSeats;
        }, { showErrorNotification: false });
    }, [getSeatsByHallHook]);

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType): Promise<SeatResponse> => {
        return updateSeatTypeHook.callApi(async () => {
            const updatedSeat = await seatApi.admin.updateSeatType(hallId, seatId, seatType);

            setSeats(prevSeats =>
                prevSeats.map(s =>
                    s.id === seatId ? { ...s, seatType } : s
                )
            );

            if (seat?.id === seatId) {
                setSeat(updatedSeat);
            }

            return updatedSeat;
        }, { showErrorNotification: false });
    }, [updateSeatTypeHook, seat]);

    const activateSeat = useCallback(async (hallId: number, seatId: number): Promise<SeatResponse> => {
        return activateSeatHook.callApi(async () => {
            const updatedSeat = await seatApi.admin.activateSeat(hallId, seatId);

            setSeats(prevSeats =>
                prevSeats.map(s =>
                    s.id === seatId ? { ...s, active: true } : s
                )
            );

            if (seat?.id === seatId) {
                setSeat(updatedSeat);
            }

            return updatedSeat;
        }, { showErrorNotification: false });
    }, [activateSeatHook, seat]);

    const deactivateSeat = useCallback(async (hallId: number, seatId: number): Promise<SeatResponse> => {
        return deactivateSeatHook.callApi(async () => {
            const updatedSeat = await seatApi.admin.deactivateSeat(hallId, seatId);

            setSeats(prevSeats =>
                prevSeats.map(s =>
                    s.id === seatId ? { ...s, active: false } : s
                )
            );

            if (seat?.id === seatId) {
                setSeat(updatedSeat);
            }

            return updatedSeat;
        }, { showErrorNotification: false });
    }, [deactivateSeatHook, seat]);

    const clearSeats = useCallback(() => {
        setSeats([]);
        setSeat(null);
    }, []);

    return {
        seats,
        seat,
        loading: getSeatsByHallHook.loading || getSeatByIdHook.loading || getSeatByPositionHook.loading ||
            checkSeatAvailabilityHook.loading || countSeatsByHallHook.loading || getSeatsByTypeHook.loading ||
            updateSeatTypeHook.loading || activateSeatHook.loading || deactivateSeatHook.loading,
        getSeatsByHall,
        getSeatById,
        getSeatByPosition,
        checkSeatAvailability,
        countSeatsByHall,
        getSeatsByType,
        getActiveSeatsByHall,
        updateSeatType,
        activateSeat,
        deactivateSeat,
        clearSeats,
    };
};