import { useState, useCallback, useMemo } from "react";
import type { SearchParams } from "@/types/pagination";

interface UsePaginationReturn {
    params: SearchParams;
    setPage: (page: number) => void;
    setSize: (size: number) => void;
    setSearch: (search: string) => void;
    setSort: (sort: string) => void;
    setFilter: (key: string, value: any) => void;
    removeFilter: (key: string) => void;
    reset: () => void;
    goToNextPage: () => void;
    goToPrevPage: () => void;
    goToFirstPage: () => void;
}

export const usePagination = (
    initialParams: Partial<SearchParams> = {},
    defaultPageSize: number = 12
): UsePaginationReturn => {
    const [params, setParams] = useState<SearchParams>(() => ({
        page: initialParams.page ?? 0,
        size: initialParams.size ?? defaultPageSize,
        sort: initialParams.sort,
        search: initialParams.search,
        ...initialParams
    }));

    const setPage = useCallback((page: number) => {
        setParams(prev => ({
            ...prev,
            page: Math.max(0, page)
        }));
    }, []);

    const setSize = useCallback((size: number) => {
        setParams(prev => ({
            ...prev,
            size: Math.max(1, size),
            page: 0
        }));
    }, []);

    const setSearch = useCallback((search: string) => {
        setParams(prev => ({
            ...prev,
            search: search.trim() || undefined,
            page: 0
        }));
    }, []);

    const setSort = useCallback((sort: string) => {
        setParams(prev => ({
            ...prev,
            sort: sort || undefined
        }));
    }, []);

    const setFilter = useCallback((key: string, value: any) => {
        setParams(prev => ({
            ...prev,
            [key]: value,
            page: 0
        }));
    }, []);

    const removeFilter = useCallback((key: string) => {
        setParams(prev => {
            const newParams = { ...prev };
            delete newParams[key];
            return { ...newParams, page: 0 };
        });
    }, []);

    const reset = useCallback(() => {
        setParams({
            page: 0,
            size: defaultPageSize
        });
    }, [defaultPageSize]);

    const goToNextPage = useCallback(() => {
        setParams(prev => ({
            ...prev,
            page: (prev.page ?? 0) + 1
        }));
    }, []);

    const goToPrevPage = useCallback(() => {
        setParams(prev => ({
            ...prev,
            page: Math.max(0, (prev.page ?? 0) - 1)
        }));
    }, []);

    const goToFirstPage = useCallback(() => {
        setPage(0);
    }, [setPage]);

    const stableParams = useMemo(() => params, [
        params.page,
        params.size,
        params.sort,
        params.search
    ]);

    return {
        params: stableParams,
        setPage,
        setSize,
        setSearch,
        setSort,
        setFilter,
        removeFilter,
        reset,
        goToNextPage,
        goToPrevPage,
        goToFirstPage
    };
};