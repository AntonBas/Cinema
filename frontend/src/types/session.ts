export type CinemaSessionStatus = 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export const SessionStatusDisplay: Record<CinemaSessionStatus, string> = {
    SCHEDULED: 'Scheduled',
    ONGOING: 'Ongoing',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled'
};

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
    movieId?: number;
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

export interface SessionAdminProjection {
    id: number;
    startTime: string;
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
    endTime: string;
}

export interface SessionScheduleResponse {
    id: number;
    startTime: string;
    endTime: string;
    basePrice: string;
    status: CinemaSessionStatus;
    availableSeats: number;
    movieId: number;
    movieTitle: string;
    moviePosterFileName?: string;
    movieAgeRating: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
}

export interface SessionScheduleProjection {
    id: number;
    startTime: string;
    basePrice: string;
    status: CinemaSessionStatus;
    movieId: number;
    movieTitle: string;
    moviePosterFileName?: string;
    movieAgeRating: string;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
    bookedSeatsCount: number;
    availableSeats: number;
    endTime: string;
}

export interface SessionDetailResponse {
    id: number;
    startTime: string;
    basePrice: string;
    status: CinemaSessionStatus;
    movieId: number;
    movieTitle: string;
    movieDescription: string;
    movieDuration: number;
    movieAgeRating: string;
    movieTrailerUrl: string;
    hallId: number;
    hallName: string;
    hallCapacity: number;
    availableSeats: number;
    bookedSeats: number;
    occupancyRate: number;
    timeUntilStart: string;
    isAvailableForBooking: boolean;
}

export interface ScheduleDay {
    date: string;
    sessions: SessionScheduleResponse[];
}

export interface ScheduleMovie {
    movieId: number;
    movieTitle: string;
    moviePosterFileName?: string;
    sessions: SessionScheduleResponse[];
}