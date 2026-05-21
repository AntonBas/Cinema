export type CinemaSessionStatus =
  | "SCHEDULED"
  | "ONGOING"
  | "COMPLETED"
  | "CANCELLED";

export const SessionStatusDisplay: Record<CinemaSessionStatus, string> = {
  SCHEDULED: "Scheduled",
  ONGOING: "Ongoing",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
} as const;

export interface SessionRequest {
  startTime: string;
  basePrice: number;
  movieId: number;
  hallId: number;
}

export interface SessionFilterRequest {
  status?: CinemaSessionStatus;
  dateFrom?: string;
  dateTo?: string;
  hallId?: number;
  movieTitle?: string;
}

export interface SessionResponse {
  id: number;
  startTime: string;
  endTime: string;
  basePrice: number;
  status: CinemaSessionStatus;
  movieId: number;
  movieTitle: string;
  movieDuration: number;
  hallId: number;
  hallName: string;
}

export interface SessionAdminResponse {
  id: number;
  startTime: string;
  endTime: string;
  basePrice: number;
  status: CinemaSessionStatus;
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
  availableSeats: number;
  movieId: number;
  movieTitle: string;
  moviePosterFileName: string;
  movieAgeRating: string;
  movieDuration: number;
  hallId: number;
  hallName: string;
  hallCapacity: number;
}

export interface SessionMovieInfoResponse {
  id: number;
  startTime: string;
  endTime: string;
  basePrice: number;
  availableSeats: number;
  hallName: string;
}
