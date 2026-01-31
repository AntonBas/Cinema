export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
    numberOfElements?: number;
}

export interface SearchParams {
    page?: number;
    size?: number;
    sort?: string;
    search?: string;
    [key: string]: any;
}