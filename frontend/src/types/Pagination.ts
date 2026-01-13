export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
    currentPage?: number;
}

export interface SearchParams {
    page?: number;
    size?: number;
    sort?: string;
    query?: string;
}