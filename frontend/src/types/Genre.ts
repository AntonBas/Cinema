export interface GenreRequest {
    name: string;
}

export interface GenreResponse {
    id: number;
    name: string;
    movieCount: number;
}

export interface GenreInfoResponse {
    id: number;
    name: string;
}