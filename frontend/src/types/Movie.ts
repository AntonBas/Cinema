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

export const getAgeRatingDisplay = (rating: AgeRating): string => {
  const displayMap = {
    [AgeRating.PEGI_3]: '3+',
    [AgeRating.PEGI_7]: '7+',
    [AgeRating.PEGI_12]: '12+',
    [AgeRating.PEGI_16]: '16+',
    [AgeRating.PEGI_18]: '18+'
  };
  return displayMap[rating];
};

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}

export interface SearchParams {
  query?: string;
  page?: number;
  size?: number;
  genre?: string;
  status?: MovieStatus;
}

export interface MovieCreateRequest {
  title: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: string;
  endShowingDate: string;
  ageRating: AgeRating;
  genreIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  castIds: number[];
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
  directorIds: number[];
  screenwriterIds: number[];
  castIds: number[];
  posterFile?: File;
  removePoster?: boolean;
  currentPosterUrl?: string;
}

export interface MovieDto {
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

  genreIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
  castIds: number[];
}

export interface MovieResponse {
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

export interface MovieFormData {
  title: string;
  trailerUrl: string;
  description: string;
  durationMinutes: number;
  releaseDate: Date | null;
  endShowingDate: Date | null;
  ageRating: AgeRating;
  selectedGenres: number[];
  selectedDirectors: number[];
  selectedScreenwriters: number[];
  selectedCast: number[];
  posterFile?: File;
  removePoster?: boolean;
}

export interface MovieFilters {
  title?: string;
  genre?: string;
  status?: MovieStatus;
  ageRating?: AgeRating;
}

export interface MovieListResponse {
  movies: MovieResponse[];
  pagination: PageResponse<MovieResponse>;
}