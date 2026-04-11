import type { GenreResponse } from './genre';
import type { PersonResponse } from './person';
import type { SessionMovieInfoResponse } from './session';

export type MovieStatus = 'UPCOMING' | 'CURRENT' | 'ARCHIVED' | 'UNKNOWN';

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
  posterFile: File;
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
  sessions: SessionMovieInfoResponse[];
}

export interface MovieAdminResponse {
  id: number;
  title: string;
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