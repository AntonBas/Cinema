export interface SessionAdminResponse {
    id: number;
    startTime: string;
    endTime: string | null;
    price: number;
    available: boolean;
    movieId: number;
    movieTitle: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
    ticketsSold: number | null;
    totalRevenue: number | null;
}

export interface SessionScheduleResponse {
    id: number;
    startTime: string;
    endTime: string | null;
    price: number;
    availableSeats: number | null;
    movieId: number;
    movieTitle: string;
    moviePosterFileName: string | null;
    movieAgeRating: string | null;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: string | null;
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

export interface ScheduleDay {
    date: string;
    sessions: SessionScheduleResponse[];
}

export interface ScheduleMovie {
    movieId: number;
    movieTitle: string;
    moviePosterFileName: string | null;
    sessions: SessionScheduleResponse[];
}