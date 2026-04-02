import type { GenreResponse } from './genre';
import type { PersonResponse } from './person';

export type MovieStatus = 'UPCOMING' | 'CURRENT' | 'ARCHIVED' | 'UNKNOWN';

export type AgeRating = 'G' | 'PG' | 'PG_13' | 'R' | 'NC_17';

export const AgeRatingDisplay: Record<AgeRating, string> = {
  G: 'G',
  PG: 'PG',
  PG_13: 'PG-13',
  R: 'R',
  NC_17: 'NC-17'
};

export const MovieStatusDisplay: Record<MovieStatus, string> = {
  UPCOMING: 'Upcoming',
  CURRENT: 'Now Showing',
  ARCHIVED: 'Archived',
  UNKNOWN: 'Unknown'
};

export interface MovieCreateRequest {
  title: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  ageRating: AgeRating;
  genreIds: number[];
  actorIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  posterFile?: File;
}

export interface MovieUpdateRequest {
  title: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  ageRating: AgeRating;
  genreIds: number[];
  actorIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  posterFile?: File;
  removePoster: boolean;
}

export interface MovieCardResponse {
  id: number;
  slug: string;
  title: string;
  posterUrl: string;
  durationMinutes: number;
  ageRating: AgeRating;
  status: MovieStatus;
}

export interface MovieDetailResponse {
  id: number;
  title: string;
  slug: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  ageRating: AgeRating;
  status: MovieStatus;
  posterFileName: string;
  posterUrl: string;
  genres: GenreResponse[];
  actors: PersonResponse[];
  directors: PersonResponse[];
  screenwriters: PersonResponse[];
}

export interface MovieSessionSearchResponse {
  id: number;
  title: string;
  durationMinutes: number;
}