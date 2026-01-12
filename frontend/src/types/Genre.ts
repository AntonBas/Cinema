export interface GenreRequest {
    name: string;
}

export interface GenreResponse {
    id: number;
    name: string;
}

export interface GenreStatsResponse extends GenreResponse {
    moviesCount?: number;
    popularity?: number;
}

export interface GenresListResponse {
    content: GenreResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface GenreSearchRequest {
    name?: string;
    page?: number;
    size?: number;
}

export const PopularGenres = [
    'Action',
    'Comedy',
    'Drama',
    'Thriller',
    'Horror',
    'Science Fiction',
    'Fantasy',
    'Romance'
] as const;