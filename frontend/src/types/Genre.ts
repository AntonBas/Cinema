export interface GenreDto {
    id?: number;
    name: string;
    movies?: number[];
}

export interface GenreRequest {
    name: string;
}

export interface SearchParams {
    query?: string;
    page?: number;
    size?: number;
}