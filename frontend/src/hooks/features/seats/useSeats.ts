import { useCallback, useRef } from 'react';
import { seatApi } from '@/api/seatApi';
import type { SeatResponse, SeatType } from '@/types/seat';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useSeats = () => {
    const mutationApi = useApi<SeatResponse>();
    const mutationApiRef = useRef(mutationApi);
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(mutationApi.loading, { delay: 150, minDisplayTime: 300 });

    const updateSeatType = useCallback(async (hallId: number, seatId: number, seatType: SeatType) => {
        return mutationApiRef.current.execute(
            () => seatApi.updateSeatType(hallId, seatId, seatType),
            { successMessage: 'Seat type updated successfully' }
        );
    }, []);

    const setSeatActiveStatus = useCallback(async (hallId: number, seatId: number, active: boolean) => {
        return mutationApiRef.current.execute(
            () => seatApi.setSeatActiveStatus(hallId, seatId, active),
            { successMessage: active ? 'Seat activated successfully' : 'Seat deactivated successfully' }
        );
    }, []);

    return {
        loading,
        error: mutationApi.error,
        updateSeatType,
        setSeatActiveStatus,
        reset: mutationApi.reset,
    };
};