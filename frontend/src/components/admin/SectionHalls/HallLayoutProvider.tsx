import React, { useState, useCallback } from 'react';
import type { CinemaHallResponse, HallLayoutResponse } from '@/types/cinemaHall';
import type { SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSeats } from '@/hooks/features/seats/useSeats';
import { HallLayoutContext } from './HallLayoutContext';

export const HallLayoutProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [currentHall, setCurrentHall] = useState<CinemaHallResponse | null>(null);
    const [layout, setLayout] = useState<HallLayoutResponse | null>(null);

    const { getHallLayout } = useCinemaHalls();
    const { setSeatActiveStatus, updateSeatType } = useSeats();

    const openLayout = useCallback(async (hall: CinemaHallResponse) => {
        setCurrentHall(hall);
        const layoutData = await getHallLayout(hall.id);
        setLayout(layoutData);
    }, [getHallLayout]);

    const closeLayout = useCallback(() => {
        setCurrentHall(null);
        setLayout(null);
    }, []);

    const handleUpdateSeatType = useCallback(async (seatId: number, type: SeatType) => {
        if (!currentHall) return;
        await updateSeatType(currentHall.id, seatId, type);
        const updatedLayout = await getHallLayout(currentHall.id);
        setLayout(updatedLayout);
    }, [currentHall, updateSeatType, getHallLayout]);

    const handleToggleSeatStatus = useCallback(async (seatId: number) => {
        if (!currentHall || !layout) return;
        const seat = layout.rows.flatMap(r => r.seats).find(s => s.id === seatId);
        if (!seat) return;
        await setSeatActiveStatus(currentHall.id, seatId, !seat.active);
        const updatedLayout = await getHallLayout(currentHall.id);
        setLayout(updatedLayout);
    }, [currentHall, layout, setSeatActiveStatus, getHallLayout]);

    const loading = !!(currentHall && !layout);

    return (
        <HallLayoutContext.Provider
            value={{
                currentHall,
                layout,
                loading,
                openLayout,
                closeLayout,
                updateSeatType: handleUpdateSeatType,
                toggleSeatStatus: handleToggleSeatStatus
            }}
        >
            {children}
        </HallLayoutContext.Provider>
    );
};
