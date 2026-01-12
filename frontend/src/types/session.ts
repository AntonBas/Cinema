export type CinemaSessionStatus = 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';

export const SessionStatusDisplay: Record<CinemaSessionStatus, string> = {
    SCHEDULED: 'Scheduled',
    ONGOING: 'Ongoing',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled'
};

export const SessionStatusColors: Record<CinemaSessionStatus, string> = {
    SCHEDULED: 'info',
    ONGOING: 'success',
    COMPLETED: 'default',
    CANCELLED: 'error'
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
    status: CinemaSessionStatus;
    availableSeats: number;
    movieId: number;
    movieTitle: string;
    moviePosterFileName: string | null;
    movieAgeRating: string | null;
    movieDuration: number;
    hallId: number;
    hallName: string;
    hallCapacity: number;
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
    status?: CinemaSessionStatus;
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

export interface SessionsListResponse {
    content: SessionAdminResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface ScheduleListResponse {
    content: SessionScheduleResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface SessionStats {
    totalSessions: number;
    upcomingSessions: number;
    ongoingSessions: number;
    completedSessions: number;
    cancelledSessions: number;
    totalRevenue: string;
    averageOccupancy: number;
}