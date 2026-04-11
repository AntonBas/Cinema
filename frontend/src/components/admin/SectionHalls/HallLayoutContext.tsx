import React, { createContext, useContext, useState, useCallback } from 'react';
import type { CinemaHallResponse, HallLayoutResponse } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSeats } from '@/hooks/features/seats/useSeats';

interface HallLayoutContextType {
    currentHall: CinemaHallResponse | null;
    layout: HallLayoutResponse | null;
    loading: boolean;
    openLayout: (hall: CinemaHallResponse) => void;
    closeLayout: () => void;
    updateSeatType: (seatId: number, type: SeatType) => Promise<void>;
    toggleSeatStatus: (seatId: number) => Promise<void>;
}

const HallLayoutContext = createContext<HallLayoutContextType | null>(null);

export const useHallLayout = () => {
    const context = useContext(HallLayoutContext);
    if (!context) {
        throw new Error('useHallLayout must be used within HallLayoutProvider');
    }
    return context;
};

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