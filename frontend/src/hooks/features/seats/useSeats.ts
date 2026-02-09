import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';

export const useSeats = () => {
    const seatsByHallApi = useApi<SeatResponse[]>();
    const seatByIdApi = useApi<SeatResponse>();
    const seatByPositionApi = useApi<SeatResponse>();
    const checkAvailabilityApi = useApi<boolean>();
    const countSeatsApi = useApi<number>();

    const getSeatsByHall = useCallback(async (hallId: number) => {
        return seatsByHallApi.callApi(
            () => seatApi.getSeatsByHall(hallId),
            {
                cacheKey: `seats_hall_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatsByHallApi]);

    const getSeatById = useCallback(async (hallId: number, seatId: number) => {
        return seatByIdApi.callApi(
            () => seatApi.getSeatById(hallId, seatId),
            {
                cacheKey: `seat_${hallId}_${seatId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatByIdApi]);

    const getSeatByPosition = useCallback(async (hallId: number, row: number, number: number) => {
        return seatByPositionApi.callApi(
            () => seatApi.getSeatByPosition(hallId, row, number),
            {
                cacheKey: `seat_position_${hallId}_${row}_${number}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatByPositionApi]);

    const checkSeatAvailability = useCallback(async (hallId: number, row: number, number: number) => {
        return checkAvailabilityApi.callApi(
            () => seatApi.checkSeatAvailability(hallId, row, number),
            {
                cacheKey: `seat_availability_${hallId}_${row}_${number}`,
                cacheTime: 10 * 1000,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, [checkAvailabilityApi]);

    const countSeatsByHall = useCallback(async (hallId: number) => {
        return countSeatsApi.callApi(
            () => seatApi.countSeatsByHall(hallId),
            {
                cacheKey: `seats_count_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [countSeatsApi]);

    const getSeatsByType = useCallback(async (hallId: number, seatType: SeatType) => {
        const api = useApi<SeatResponse[]>();
        return api.callApi(
            () => seatApi.getSeatsByType(hallId, seatType),
            {
                cacheKey: `seats_type_${hallId}_${seatType}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const getActiveSeats = useCallback(async (hallId: number) => {
        const api = useApi<SeatResponse[]>();
        return api.callApi(
            () => seatApi.getActiveSeats(hallId),
            {
                cacheKey: `active_seats_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const getDistinctRows = useCallback(async (hallId: number) => {
        const api = useApi<number[]>();
        return api.callApi(
            () => seatApi.getDistinctRows(hallId),
            {
                cacheKey: `rows_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        const api = useApi<SeatResponse>();
        return api.callApi(
            () => seatApi.admin.updateSeatType(hallId, seatId, seatType),
            {
                successMessage: 'Seat type updated successfully',
                onSuccess: () => {
                    seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
                    seatByIdApi.invalidateCache(`seat_${hallId}_${seatId}`);
                },
            }
        );
    }, [seatsByHallApi, seatByIdApi]);

    const setSeatStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        const api = useApi<SeatResponse>();
        return api.callApi(
            () => seatApi.admin.setSeatStatus(hallId, seatId, active),
            {
                successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully',
                onSuccess: () => {
                    seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
                    seatByIdApi.invalidateCache(`seat_${hallId}_${seatId}`);
                },
            }
        );
    }, [seatsByHallApi, seatByIdApi]);

    const clearSeatsCache = useCallback((hallId?: number) => {
        if (hallId) {
            seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
        } else {
            seatsByHallApi.invalidateCache();
            seatByIdApi.invalidateCache();
            seatByPositionApi.invalidateCache();
            checkAvailabilityApi.invalidateCache();
            countSeatsApi.invalidateCache();
        }
    }, [seatsByHallApi, seatByIdApi, seatByPositionApi, checkAvailabilityApi, countSeatsApi]);

    return {
        seats: seatsByHallApi.data || [],
        seat: seatByIdApi.data || seatByPositionApi.data,

        loading: seatsByHallApi.state.isLoading || seatByIdApi.state.isLoading || seatByPositionApi.state.isLoading,
        error: seatsByHallApi.state.isError || seatByIdApi.state.isError || seatByPositionApi.state.isError,

        getSeatsByHall,
        getSeatById,
        getSeatByPosition,
        checkSeatAvailability,
        countSeatsByHall,
        getSeatsByType,
        getActiveSeats,
        getDistinctRows,
        updateSeatType,
        setSeatStatus,
        clearSeatsCache,

        resetSeats: seatsByHallApi.reset,
        resetSeat: seatByIdApi.reset,
        refetchSeats: seatsByHallApi.refetch,
    };
};