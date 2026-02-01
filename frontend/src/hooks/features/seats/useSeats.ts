import { useState, useCallback, useRef } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';

export const useSeats = () => {
    const [seats, setSeats] = useState<SeatResponse[]>([]);
    const [seat, setSeat] = useState<SeatResponse | null>(null);

    const apiHookRef = useRef(useApi<SeatResponse[]>());
    const apiHook = apiHookRef.current;

    const getSeatByIdHook = useApi<SeatResponse>();
    const getSeatByPositionHook = useApi<SeatResponse>();
    const checkSeatAvailabilityHook = useApi<boolean>();
    const countSeatsByHallHook = useApi<number>();
    const updateSeatTypeHook = useApi<SeatResponse>();
    const activateSeatHook = useApi<SeatResponse>();
    const deactivateSeatHook = useApi<SeatResponse>();

    const getSeatsByHall = useCallback(async (hallId: number): Promise<SeatResponse[]> => {
        return apiHook.callApi(async () => {
            const data = await seatApi.getSeatsByHall(hallId);
            setSeats(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

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
        return apiHook.callApi(async () => {
            const data = await seatApi.getSeatsByType(hallId, seatType);
            setSeats(data);
            return data;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const getActiveSeatsByHall = useCallback(async (hallId: number): Promise<SeatResponse[]> => {
        return apiHook.callApi(async () => {
            const allSeats = await seatApi.getSeatsByHall(hallId);
            const activeSeats = allSeats.filter(s => s.active);
            setSeats(activeSeats);
            return activeSeats;
        }, { showErrorNotification: false });
    }, [apiHook]);

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
        loading: apiHook.loading || getSeatByIdHook.loading || getSeatByPositionHook.loading ||
            checkSeatAvailabilityHook.loading || countSeatsByHallHook.loading ||
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