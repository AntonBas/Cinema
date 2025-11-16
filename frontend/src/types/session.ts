import type { MovieShortResponse } from '@/types/movie';
import type { CinemaHallResponse } from '@/types/cinemaHall';

export interface SessionResponse {
    id: number;
    startTime: string;
    endTime: string;
    price: number;
    movie: MovieShortResponse;
    hall: CinemaHallResponse;
    available: boolean;
}

export interface SessionRequest {
    startTime: string;
    price: number;
    movieId: number;
    hallId: number;
}

export interface SessionFilters {
    date?: string;
    hallId?: number;
    movieId?: number;
    days?: number;
}