export enum SessionStatus {
    SCHEDULED = 'SCHEDULED',
    ONGOING = 'ONGOING',
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED'
}

export interface SessionAdminResponse {
    id: number;
    startTime: string;
    endTime: string;
    basePrice: number;
    status: SessionStatus;
    movieId: number;
    movieTitle: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
    ticketsSold: number;
    totalRevenue: number;
}

export interface SessionScheduleResponse {
    id: number;
    startTime: string;
    endTime: string;
    basePrice: number;
    status: SessionStatus;
    availableSeats: number;
    movieId: number;
    movieTitle: string;
    moviePosterFileName: string | null;
    movieAgeRating: string | null;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: string;
}

export interface SessionCreateRequest {
    startTime: string;
    basePrice: number;
    movieId: number;
    hallId: number;
}

export interface SessionUpdateRequest {
    startTime?: string;
    basePrice?: number;
    movieId?: number;
    hallId?: number;
}

export interface SessionFilters {
    date?: string;
    hallId?: number;
    movieId?: number;
    days?: number;
}

export interface SessionFilter {
    search?: string;
    movieId?: number;
    hallId?: number;
    status?: SessionStatus;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
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