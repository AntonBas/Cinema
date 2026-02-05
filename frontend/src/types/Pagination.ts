export interface PageResponse<T> {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    empty: boolean;
    hasNext: boolean;
    hasPrevious: boolean;
    numberOfElements?: number;
    sort?: SortInfo[];
}

export interface SortInfo {
    property: string;
    direction: string;
    ascending: boolean;
}

export interface SearchParams {
    page?: number;
    size?: number;
    sort?: string;
    search?: string;
    [key: string]: any;
}