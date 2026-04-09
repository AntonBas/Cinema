import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useSeats = () => {
    const updateSeatTypeApi = useApi<SeatResponse>();
    const setSeatActiveStatusApi = useApi<SeatResponse>();

    const rawLoading = updateSeatTypeApi.loading || setSeatActiveStatusApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!updateSeatTypeApi.error || !!setSeatActiveStatusApi.error;

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        const response = await updateSeatTypeApi.execute(
            () => seatApi.updateSeatType(hallId, seatId, seatType),
            { successMessage: 'Seat type updated successfully' }
        );
        return response || null;
    }, [updateSeatTypeApi]);

    const setSeatActiveStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        const response = await setSeatActiveStatusApi.execute(
            () => seatApi.setSeatActiveStatus(hallId, seatId, active),
            { successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully' }
        );
        return response || null;
    }, [setSeatActiveStatusApi]);

    const reset = useCallback(() => {
        updateSeatTypeApi.reset();
        setSeatActiveStatusApi.reset();
    }, [updateSeatTypeApi, setSeatActiveStatusApi]);

    return {
        loading,
        error,
        updateSeatType,
        setSeatActiveStatus,
        reset,
        lastResult: updateSeatTypeApi.data || setSeatActiveStatusApi.data,
    };
};