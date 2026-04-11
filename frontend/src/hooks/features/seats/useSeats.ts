import { useCallback } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useSeats = () => {
    const mutationApi = useApi<SeatResponse>();

    const loading = useDelayedLoading(mutationApi.loading, { delay: 150, minDisplayTime: 300 });

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        return mutationApi.execute(
            () => seatApi.updateSeatType(hallId, seatId, seatType),
            { successMessage: 'Seat type updated successfully' }
        );
    }, [mutationApi]);

    const setSeatActiveStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        return mutationApi.execute(
            () => seatApi.setSeatActiveStatus(hallId, seatId, active),
            { successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully' }
        );
    }, [mutationApi]);

    return {
        loading,
        error: mutationApi.error,
        updateSeatType,
        setSeatActiveStatus,
        reset: mutationApi.reset,
    };
};