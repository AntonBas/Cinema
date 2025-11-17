import type { SeatResponse, SeatRowResponse, SeatType } from './seat';

export interface CinemaHall {
    id: number;
    name: string;
    seats: any[];
    capacity: number;
}

export interface CinemaHallResponse {
    id: number;
    name: string;
    capacity: number;
}

export interface CinemaHallRequest {
    name: string;
    rows?: number;
    seatsPerRow?: number;
    defaultSeatType?: SeatType;
}

export interface CinemaHallWithSeatsResponse {
    id: number;
    name: string;
    capacity: number;
    seats: SeatResponse[];
}

export interface HallLayoutResponse {
    hallId: number;
    hallName: string;
    totalRows: number;
    maxSeatsPerRow: number;
    totalSeats: number;
    rows: SeatRowResponse[];
}