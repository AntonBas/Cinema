import { createContext, useContext } from 'react';
import type { CinemaHallResponse, HallLayoutResponse } from '@/types/cinemaHall';
import type { SeatType } from '@/types/seat';

export interface HallLayoutContextType {
    currentHall: CinemaHallResponse | null;
    layout: HallLayoutResponse | null;
    loading: boolean;
    openLayout: (hall: CinemaHallResponse) => void;
    closeLayout: () => void;
    updateSeatType: (seatId: number, type: SeatType) => Promise<void>;
    toggleSeatStatus: (seatId: number) => Promise<void>;
}

export const HallLayoutContext = createContext<HallLayoutContextType | null>(null);

export const useHallLayout = () => {
    const context = useContext(HallLayoutContext);
    if (!context) {
        throw new Error('useHallLayout must be used within HallLayoutProvider');
    }
    return context;
};
