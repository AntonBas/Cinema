export interface GenreDto {
    id?: number;
    name: string;
    movies?: number[];
}

export interface GenreFormData {
    name: string;
}