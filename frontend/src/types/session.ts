export interface MovieSimpleDto {
    id: number;
    title: string;
    durationMinutes: number;
    posterFileName?: string;
}

export interface CinemaHallDto {
    id: number;
    name: string;
    capacity: number;
}

export interface SessionDto {
    id: number;
    startTime: string;
    endTime: string;
    price: number;
    movie: MovieSimpleDto;
    hall: CinemaHallDto;
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