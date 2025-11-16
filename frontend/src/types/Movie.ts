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
  genreIds: number[];
  actorIds: number[];
  directorIds: number[];
  screenwriterIds: number[];
}

export interface MovieShortResponse {
  id: number;
  title: string;
  durationMinutes: number;
  posterFileName?: string;
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
  selectedActors: number[];
  selectedDirectors: number[];
  selectedScreenwriters: number[];
  posterFile?: File;
  removePoster?: boolean;
}