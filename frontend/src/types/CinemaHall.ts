import type { SeatRowResponse } from './seat';
import { SeatType } from './seat';

export interface CinemaHallListResponse {
    id: number;
    name: string;
    capacity: number;
}

export interface CinemaHallResponse {
    id: number;
    name: string;
    rows: number;
    seatsPerRow: number;
    defaultSeatType: SeatType;
    coupleRows: number[];
    capacity: number;
}

export interface CinemaHallRequest {
    name: string;
    rows: number;
    seatsPerRow: number;
    defaultSeatType?: SeatType;
    coupleRows?: number[];
}

export interface HallLayoutResponse {
    hallId: number;
    hallName: string;
    totalRows: number;
    maxSeatsPerRow: number;
    totalSeats: number;
    rows: SeatRowResponse[];
}