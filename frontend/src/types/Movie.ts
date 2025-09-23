export enum MovieStatus {
  ACTIVE = 'ACTIVE',
  UPCOMING = 'UPCOMING',
  ARCHIVED = 'ARCHIVED'
}

export enum AgeRating {
  G = 'G',
  PG = 'PG',
  PG13 = 'PG13',
  R = 'R',
  NC17 = 'NC17'
}

export interface MovieDto {
  id?: number;
  title: string;
  slug: string;
  trailer: string;
  description: string;
  production: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  status: MovieStatus;
  posterImagePath: string;
  ageRating: AgeRating;
  sessionIds?: number[];
  castIds?: number[];
  directorIds?: number[];
  screenwriterIds?: number[];
  genreIds?: number[];
  releaseYear?: number;
  isCurrentlyShowing?: boolean;
  isUpcoming?: boolean;
}

export interface MovieFormData {
  title: string;
  slug: string;
  trailer: string;
  description: string;
  production: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  status: MovieStatus;
  ageRating: AgeRating;
  posterFile?: File;
  genreIds?: number[];
  directorIds?: number[];
  screenwriterIds?: number[];
  castIds?: number[];
}