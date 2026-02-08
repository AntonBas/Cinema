export interface GenreRequest {
    name: string;
}

export interface GenreResponse {
    id: number;
    name: string;
    movieCount: number;
}

export interface GenreProjection {
    id: number;
    name: string;
    movieCount: number;
}