export enum MovieStatus {
  UPCOMING = 'UPCOMING',
  CURRENT = 'CURRENT',
  ARCHIVED = 'ARCHIVED'
}

export enum AgeRating {
  PEGI_3 = 'PEGI_3',
  PEGI_7 = 'PEGI_7',
  PEGI_12 = 'PEGI_12',
  PEGI_16 = 'PEGI_16',
  PEGI_18 = 'PEGI_18'
}

export interface MovieDto {
  id?: number;
  title: string;
  slug: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  status: MovieStatus;
  posterFileName?: string;
  ageRating: AgeRating;
  castIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  genreIds: number[];
  posterFile?: File;
}

export interface MovieCreateRequest {
  title: string;
  slug: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  status: MovieStatus;
  ageRating: AgeRating;
  genreIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  castIds: number[];
  posterFile?: File;
}

export interface MovieFormData {
  title: string;
  slug: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  status: MovieStatus;
  ageRating: AgeRating;
  genreIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  castIds: number[];
  posterFile?: File;
}