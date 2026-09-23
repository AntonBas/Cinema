import { createContext, useContext } from "react";
import type {
  CinemaHallResponse,
  HallLayoutResponse,
} from "@/types/cinemaHall";
import type { SeatType } from "@/types/seat";

export interface DraftSeat {
  key: string;
  id: number | null;
  col: number;
  gridRow: number;
  seatType: SeatType;
  active: boolean;
}

export interface HallLayoutContextType {
  currentHall: CinemaHallResponse | null;
  layout: HallLayoutResponse | null;
  seats: DraftSeat[];
  isDirty: boolean;
  saving: boolean;
  loading: boolean;
  openLayout: (hall: CinemaHallResponse) => void;
  closeLayout: () => void;
  addSeat: (col: number, gridRow: number) => void;
  moveSeat: (key: string, col: number, gridRow: number) => void;
  removeSeat: (key: string) => void;
  cycleSeatType: (key: string) => void;
  toggleSeatActive: (key: string) => void;
  saveLayout: () => Promise<boolean>;
}

export const HallLayoutContext = createContext<HallLayoutContextType | null>(
  null,
);

export const useHallLayout = () => {
  const context = useContext(HallLayoutContext);
  if (!context) {
    throw new Error("useHallLayout must be used within HallLayoutProvider");
  }
  return context;
};
