import type { SeatRowResponse, SeatType } from "./seat";

export interface CinemaHallListResponse {
  id: number;
  name: string;
  capacity: number;
}

export interface CinemaHallResponse {
  id: number;
  name: string;
  capacity: number;
}

export interface CinemaHallRequest {
  name: string;
}

export interface HallLayoutResponse {
  hallId: number;
  hallName: string;
  totalRows: number;
  maxSeatsPerRow: number;
  totalSeats: number;
  rows: SeatRowResponse[];
}

export interface SeatLayoutItem {
  id: number | null;
  row: number;
  number: number;
  seatType: SeatType;
  x: number;
  y: number;
  active: boolean;
}

export interface HallLayoutRequest {
  seats: SeatLayoutItem[];
}
