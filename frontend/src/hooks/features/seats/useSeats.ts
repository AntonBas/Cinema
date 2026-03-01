import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useSeats = () => {
    const mutationApi = useApi<SeatResponse>();

    const rawLoading = mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!mutationApi.error;

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        const response = await mutationApi.execute(
            () => seatApi.admin.updateSeatType(hallId, seatId, seatType),
            {
                successMessage: 'Seat type updated successfully',
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [mutationApi]);

    const setSeatStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        const response = await mutationApi.execute(
            () => seatApi.admin.setSeatStatus(hallId, seatId, active),
            {
                successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully',
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [mutationApi]);

    const clearCache = useCallback(() => {
        mutationApi.invalidateCache();
    }, [mutationApi]);

    const reset = useCallback(() => {
        mutationApi.reset();
    }, [mutationApi]);

    return {
        loading,
        error,

        updateSeatType,
        setSeatStatus,

        clearCache,
        reset,

        lastResult: mutationApi.data,
    };
};