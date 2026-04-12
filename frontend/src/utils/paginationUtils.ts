import type { PageResponse, SearchParams } from '@/types/pagination';

export const DEFAULT_PAGE = 0;
export const DEFAULT_PAGE_SIZE = 12;
export const DEFAULT_PAGE_SIZE_SMALL = 8;
export const DEFAULT_PAGE_SIZE_MEDIUM = 16;
export const DEFAULT_PAGE_SIZE_LARGE = 24;
export const DEFAULT_PAGE_SIZE_ADMIN = 20;
export const DEFAULT_SORT = 'id,desc';

export const getDefaultPageSize = (context: 'grid' | 'list' | 'table' | 'admin' | 'small' | 'medium' | 'large' = 'grid'): number => {
    switch (context) {
        case 'small':
            return DEFAULT_PAGE_SIZE_SMALL;
        case 'medium':
            return DEFAULT_PAGE_SIZE_MEDIUM;
        case 'large':
            return DEFAULT_PAGE_SIZE_LARGE;
        case 'admin':
        case 'table':
            return DEFAULT_PAGE_SIZE_ADMIN;
        case 'grid':
            return DEFAULT_PAGE_SIZE;
        case 'list':
            return DEFAULT_PAGE_SIZE_MEDIUM;
        default:
            return DEFAULT_PAGE_SIZE;
    }
};

export const createSearchParams = (params: SearchParams, context?: string): URLSearchParams => {
    const searchParams = new URLSearchParams();

    const page = params.page !== undefined ? Math.max(0, params.page) : DEFAULT_PAGE;
    searchParams.append('page', page.toString());

    const defaultSize = context ? getDefaultPageSize(context as any) : DEFAULT_PAGE_SIZE;
    const size = params.size !== undefined ? Math.max(1, params.size) : defaultSize;
    searchParams.append('size', size.toString());

    if (params.sort) {
        searchParams.append('sort', params.sort);
    }

    if (params.query) {
        searchParams.append('query', params.query);
    }

    Object.keys(params).forEach(key => {
        if (!['page', 'size', 'sort', 'query'].includes(key)) {
            const value = params[key];
            if (value !== undefined && value !== null && value !== '') {
                if (Array.isArray(value)) {
                    value.forEach(v => searchParams.append(key, v.toString()));
                } else {
                    searchParams.append(key, value.toString());
                }
            }
        }
    });

    return searchParams;
};

export const buildPagedUrl = (baseUrl: string, params?: SearchParams, context?: string): string => {
    const searchParams = createSearchParams(params || {}, context);
    const queryString = searchParams.toString();
    return queryString ? `${baseUrl}?${queryString}` : baseUrl;
};

export const buildFilteredUrl = (baseUrl: string, params?: SearchParams, filter?: Record<string, any>, context?: string): string => {
    const searchParams = createSearchParams(params || {}, context);

    if (filter) {
        Object.entries(filter).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                searchParams.append(key, value.toString());
            }
        });
    }

    const queryString = searchParams.toString();
    return queryString ? `${baseUrl}?${queryString}` : baseUrl;
};

export const getPaginationInfo = <T>(pageResponse: PageResponse<T>) => {
    const currentPage = pageResponse.number;
    const totalPages = pageResponse.totalPages;
    const pageSize = pageResponse.size;
    const totalElements = pageResponse.totalElements;
    const startElement = currentPage * pageSize + 1;
    const endElement = Math.min((currentPage + 1) * pageSize, totalElements);

    return {
        currentPage,
        totalPages,
        pageSize,
        totalElements,
        startElement,
        endElement,
        hasPrevious: pageResponse.hasPrevious !== undefined ? pageResponse.hasPrevious : currentPage > 0,
        hasNext: pageResponse.hasNext !== undefined ? pageResponse.hasNext : currentPage < totalPages - 1,
        isFirst: pageResponse.first,
        isLast: pageResponse.last,
        isEmpty: pageResponse.empty,
        sort: pageResponse.sort
    };
};

export const createSortString = (property: string, direction: 'asc' | 'desc' = 'asc'): string => {
    return `${property},${direction}`;
};

export const parseSortString = (sortString: string): { property: string, direction: 'asc' | 'desc' } => {
    const [property, direction] = sortString.split(',');
    return {
        property,
        direction: (direction?.toLowerCase() as 'asc' | 'desc') || 'asc'
    };
};