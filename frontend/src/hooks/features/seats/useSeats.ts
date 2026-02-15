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
    const seatsByTypeApi = useApi<SeatResponse[]>();
    const activeSeatsApi = useApi<SeatResponse[]>();
    const distinctRowsApi = useApi<number[]>();
    const updateSeatTypeApi = useApi<SeatResponse>();
    const setSeatStatusApi = useApi<SeatResponse>();

    const getSeatsByHall = useCallback(async (hallId: number) => {
        return seatsByHallApi.execute(
            () => seatApi.getSeatsByHall(hallId),
            {
                cacheKey: `seats_hall_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatsByHallApi]);

    const getSeatById = useCallback(async (hallId: number, seatId: number) => {
        return seatByIdApi.execute(
            () => seatApi.getSeatById(hallId, seatId),
            {
                cacheKey: `seat_${hallId}_${seatId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatByIdApi]);

    const getSeatByPosition = useCallback(async (hallId: number, row: number, number: number) => {
        return seatByPositionApi.execute(
            () => seatApi.getSeatByPosition(hallId, row, number),
            {
                cacheKey: `seat_position_${hallId}_${row}_${number}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatByPositionApi]);

    const checkSeatAvailability = useCallback(async (hallId: number, row: number, number: number) => {
        return checkAvailabilityApi.execute(
            () => seatApi.checkSeatAvailability(hallId, row, number),
            {
                cacheKey: `seat_availability_${hallId}_${row}_${number}`,
                cacheTime: 10 * 1000,
                showErrorNotification: false,
            }
        );
    }, [checkAvailabilityApi]);

    const countSeatsByHall = useCallback(async (hallId: number) => {
        return countSeatsApi.execute(
            () => seatApi.countSeatsByHall(hallId),
            {
                cacheKey: `seats_count_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [countSeatsApi]);

    const getSeatsByType = useCallback(async (hallId: number, seatType: SeatType) => {
        return seatsByTypeApi.execute(
            () => seatApi.getSeatsByType(hallId, seatType),
            {
                cacheKey: `seats_type_${hallId}_${seatType}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [seatsByTypeApi]);

    const getActiveSeats = useCallback(async (hallId: number) => {
        return activeSeatsApi.execute(
            () => seatApi.getActiveSeats(hallId),
            {
                cacheKey: `active_seats_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [activeSeatsApi]);

    const getDistinctRows = useCallback(async (hallId: number) => {
        return distinctRowsApi.execute(
            () => seatApi.getDistinctRows(hallId),
            {
                cacheKey: `rows_${hallId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [distinctRowsApi]);

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        return updateSeatTypeApi.execute(
            () => seatApi.admin.updateSeatType(hallId, seatId, seatType),
            {
                successMessage: 'Seat type updated successfully',
                onSuccess: () => {
                    seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
                    seatByIdApi.invalidateCache(`seat_${hallId}_${seatId}`);
                    seatsByTypeApi.invalidateCache();
                    activeSeatsApi.invalidateCache();
                },
            }
        );
    }, [updateSeatTypeApi, seatsByHallApi, seatByIdApi, seatsByTypeApi, activeSeatsApi]);

    const setSeatStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        return setSeatStatusApi.execute(
            () => seatApi.admin.setSeatStatus(hallId, seatId, active),
            {
                successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully',
                onSuccess: () => {
                    seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
                    seatByIdApi.invalidateCache(`seat_${hallId}_${seatId}`);
                    activeSeatsApi.invalidateCache();
                },
            }
        );
    }, [setSeatStatusApi, seatsByHallApi, seatByIdApi, activeSeatsApi]);

    const clearSeatsCache = useCallback((hallId?: number) => {
        if (hallId) {
            seatsByHallApi.invalidateCache(`seats_hall_${hallId}`);
        } else {
            seatsByHallApi.invalidateCache();
            seatByIdApi.invalidateCache();
            seatByPositionApi.invalidateCache();
            checkAvailabilityApi.invalidateCache();
            countSeatsApi.invalidateCache();
            seatsByTypeApi.invalidateCache();
            activeSeatsApi.invalidateCache();
            distinctRowsApi.invalidateCache();
            updateSeatTypeApi.invalidateCache();
            setSeatStatusApi.invalidateCache();
        }
    }, [seatsByHallApi, seatByIdApi, seatByPositionApi, checkAvailabilityApi,
        countSeatsApi, seatsByTypeApi, activeSeatsApi, distinctRowsApi,
        updateSeatTypeApi, setSeatStatusApi]);

    const loading = seatsByHallApi.loading || seatByIdApi.loading ||
        seatByPositionApi.loading || checkAvailabilityApi.loading ||
        countSeatsApi.loading || seatsByTypeApi.loading ||
        activeSeatsApi.loading || distinctRowsApi.loading ||
        updateSeatTypeApi.loading || setSeatStatusApi.loading;

    const error = !!(seatsByHallApi.error || seatByIdApi.error ||
        seatByPositionApi.error || checkAvailabilityApi.error ||
        countSeatsApi.error || seatsByTypeApi.error ||
        activeSeatsApi.error || distinctRowsApi.error ||
        updateSeatTypeApi.error || setSeatStatusApi.error);

    return {
        seats: seatsByHallApi.data || [],
        seat: seatByIdApi.data || seatByPositionApi.data,
        seatsByType: seatsByTypeApi.data || [],
        activeSeats: activeSeatsApi.data || [],
        rows: distinctRowsApi.data || [],
        seatAvailability: checkAvailabilityApi.data,
        seatsCount: countSeatsApi.data,

        loading,
        error,

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
        resetSeatsByType: seatsByTypeApi.reset,
        resetActiveSeats: activeSeatsApi.reset,
    };
};