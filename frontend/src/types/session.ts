export type CinemaSessionStatus = 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export const SessionStatusDisplay: Record<CinemaSessionStatus, string> = {
    SCHEDULED: 'Scheduled',
    ONGOING: 'Ongoing',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled'
} as const;

export interface SessionCreateRequest {
    startTime: string;
    basePrice: string;
    movieId: number;
    hallId: number;
}

export interface SessionUpdateRequest {
    startTime?: string;
    basePrice?: string;
    movieId?: number;
    hallId?: number;
}

export interface SessionFilterRequest {
    status?: CinemaSessionStatus;
    dateFrom?: string;
    dateTo?: string;
    hallId?: number;
    movieTitle?: string;
}

export interface SessionAdminResponse {
    id: number;
    startTime: string;
    endTime: string;
    basePrice: string;
    status: CinemaSessionStatus;
    movieId: number;
    movieTitle: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
    ticketsSold: number;
    totalRevenue: string;
}

export interface SessionScheduleResponse {
    id: number;
    startTime: string;
    endTime: string;
    basePrice: string;
    availableSeats: number;
    movieId: number;
    movieTitle: string;
    movieAgeRating: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
}