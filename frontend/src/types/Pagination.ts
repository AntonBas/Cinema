export interface PageResponse<T> {
    content: T[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
}

export interface SearchParams {
    query?: string;
    page?: number;
    size?: number;
}