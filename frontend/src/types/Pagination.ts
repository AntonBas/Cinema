export interface PageResponse<T> {
    content: T[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    pageSize: number;
}

export interface SearchParams {
    page?: number;
    size?: number;
    sort?: string;
    query?: string;
}