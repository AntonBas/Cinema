export interface PageResponse<T> {
    content: T[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface SearchParams {
    page?: number;
    size?: number;
    sort?: string;
    query?: string;
}