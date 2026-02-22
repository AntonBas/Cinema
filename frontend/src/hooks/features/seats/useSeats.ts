import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';

export const useSeats = () => {
    const updateSeatTypeApi = useApi<SeatResponse>();
    const setSeatStatusApi = useApi<SeatResponse>();

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        return updateSeatTypeApi.execute(
            () => seatApi.admin.updateSeatType(hallId, seatId, seatType),
            {
                successMessage: 'Seat type updated successfully',
                showErrorNotification: true,
            }
        );
    }, [updateSeatTypeApi]);

    const setSeatStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        return setSeatStatusApi.execute(
            () => seatApi.admin.setSeatStatus(hallId, seatId, active),
            {
                successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully',
                showErrorNotification: true,
            }
        );
    }, [setSeatStatusApi]);

    const loading = updateSeatTypeApi.loading || setSeatStatusApi.loading;
    const error = !!(updateSeatTypeApi.error || setSeatStatusApi.error);

    return {
        loading,
        error,
        updateSeatType,
        setSeatStatus,
        resetUpdateSeatType: updateSeatTypeApi.reset,
        resetSetSeatStatus: setSeatStatusApi.reset,
    };
};