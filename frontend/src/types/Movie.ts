import type { GenreResponse } from './genre';
import type { PersonResponse } from './person';

export type MovieStatus = 'UPCOMING' | 'CURRENT' | 'ARCHIVED';
export type AgeRating = 'PEGI_3' | 'PEGI_7' | 'PEGI_12' | 'PEGI_16' | 'PEGI_18';

export const AgeRatingDisplay: Record<AgeRating, string> = {
  PEGI_3: '3+',
  PEGI_7: '7+',
  PEGI_12: '12+',
  PEGI_16: '16+',
  PEGI_18: '18+'
};

export const AgeRatingDescription: Record<AgeRating, string> = {
  PEGI_3: 'Suitable for all ages – no restrictions',
  PEGI_7: 'May contain mild violence/fear scenes for young children',
  PEGI_12: 'Recommended for viewers aged 12 and older',
  PEGI_16: 'Suitable only for teens aged 16+',
  PEGI_18: 'Adults only (18+) – restricted content'
};

export const AgeRatingMinAge: Record<AgeRating, number> = {
  PEGI_3: 3,
  PEGI_7: 7,
  PEGI_12: 12,
  PEGI_16: 16,
  PEGI_18: 18
};

export const MovieStatusDisplay: Record<MovieStatus, string> = {
  UPCOMING: 'Upcoming',
  CURRENT: 'Now Showing',
  ARCHIVED: 'Archived'
};

export const getAgeRatingDisplay = (rating: AgeRating): string => {
  return AgeRatingDisplay[rating];
};

export const getAgeRatingDescription = (rating: AgeRating): string => {
  return AgeRatingDescription[rating];
};

export const getMinAge = (rating: AgeRating): number => {
  return AgeRatingMinAge[rating];
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
  removePoster?: boolean;
}

export interface MovieCardResponse {
  id: number;
  title: string;
  slug: string;
  posterUrl: string;
  durationMinutes: number;
  ageRating: AgeRating;
  releaseDate: string;
  status: MovieStatus;
  currentlyShowing: boolean;
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
  posterFileName?: string;
  posterUrl: string;
  currentlyShowing: boolean;
  upcoming: boolean;
  archived: boolean;
  active: boolean;
  genres?: GenreResponse[];
  actors?: PersonResponse[];
  directors?: PersonResponse[];
  screenwriters?: PersonResponse[];
}

export interface MovieSessionSearchResponse {
  id: number;
  title: string;
  releaseYear: number;
  durationMinutes: number;
}