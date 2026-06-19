export interface GenreRequest {
  name: string;
}

export interface GenreResponse {
  id: number;
  name: string;
}

export interface GenreListResponse {
  id: number;
  name: string;
  movieCount: number;
}
