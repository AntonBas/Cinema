export interface GenreRequest {
    name: string;
}

export interface GenreResponse {
    id: number;
    name: string;
    movieCount: number;
}