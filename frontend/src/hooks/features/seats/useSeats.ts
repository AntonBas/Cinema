import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useSeats = () => {
    const updateSeatTypeApi = useApi<SeatResponse>();
    const setSeatStatusApi = useApi<SeatResponse>();

    const rawLoading = updateSeatTypeApi.loading || setSeatStatusApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!updateSeatTypeApi.error || !!setSeatStatusApi.error;

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        const response = await updateSeatTypeApi.execute(
            () => seatApi.admin.updateSeatType(hallId, seatId, seatType),
            { successMessage: 'Seat type updated successfully' }
        );
        return response || null;
    }, [updateSeatTypeApi]);

    const setSeatStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        const response = await setSeatStatusApi.execute(
            () => seatApi.admin.setSeatStatus(hallId, seatId, active),
            { successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully' }
        );
        return response || null;
    }, [setSeatStatusApi]);

    const reset = useCallback(() => {
        updateSeatTypeApi.reset();
        setSeatStatusApi.reset();
    }, [updateSeatTypeApi, setSeatStatusApi]);

    return {
        loading,
        error,
        updateSeatType,
        setSeatStatus,
        reset,
        lastResult: updateSeatTypeApi.data || setSeatStatusApi.data,
    };
};