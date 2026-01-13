import type { PageResponse, SearchParams } from '@/types/pagination';

export const DEFAULT_PAGE = 0;
export const DEFAULT_PAGE_SIZE = 10;
export const DEFAULT_SORT = 'id,desc';

export const createSearchParams = (params: SearchParams): URLSearchParams => {
    const searchParams = new URLSearchParams();

    const page = params.page !== undefined ? Math.max(0, params.page) : DEFAULT_PAGE;
    searchParams.append('page', page.toString());

    const size = params.size !== undefined ? Math.max(1, params.size) : DEFAULT_PAGE_SIZE;
    searchParams.append('size', size.toString());

    if (params.sort) {
        searchParams.append('sort', params.sort);
    }

    if (params.query) {
        searchParams.append('query', params.query);
        searchParams.append('search', params.query);
    }

    Object.keys(params).forEach(key => {
        if (!['page', 'size', 'sort', 'query', 'search'].includes(key)) {
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

export const buildPagedUrl = (baseUrl: string, params: SearchParams = {}): string => {
    const searchParams = createSearchParams(params);
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
        hasPrevious: currentPage > 0,
        hasNext: currentPage < totalPages - 1,
        isFirst: pageResponse.first,
        isLast: pageResponse.last,
        isEmpty: pageResponse.empty
    };
};

export const createSortString = (property: string, direction: 'asc' | 'desc' = 'asc'): string => {
    return `${property},${direction}`;
};