import type { SeatDto, SeatRowDto } from './seat';

export interface CinemaHall {
    id: number;
    name: string;
    seats: any[];
    capacity: number;
}

export interface CinemaHallDto {
    id: number;
    name: string;
    capacity: number;
}

export interface CinemaHallRequest {
    name: string;
}

export interface CinemaHallWithSeatsDto {
    id: number;
    name: string;
    capacity: number;
    seats: SeatDto[];
}

export interface HallLayoutDto {
    hallId: number;
    hallName: string;
    totalRows: number;
    maxSeatsPerRow: number;
    totalSeats: number;
    rows: SeatRowDto[];
}
