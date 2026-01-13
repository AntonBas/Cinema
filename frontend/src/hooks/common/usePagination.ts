import { useState, useCallback } from "react";
import type { SearchParams } from "@/types/pagination";

interface UsePaginationReturn {
    params: SearchParams;
    setPage: (page: number) => void;
    setSize: (size: number) => void;
    setQuery: (query: string) => void;
    setSort: (sort: string) => void;
    setFilter: (key: string, value: any) => void;
    removeFilter: (key: string) => void;
    reset: () => void;
}

export const usePagination = (
    initialParams: Partial<SearchParams> = {}
): UsePaginationReturn => {
    const [params, setParams] = useState<SearchParams>({
        page: initialParams.page ?? 0,
        size: initialParams.size ?? 10,
        sort: initialParams.sort,
        query: initialParams.query,
        ...initialParams
    });

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

    const setQuery = useCallback((query: string) => {
        setParams(prev => ({
            ...prev,
            query: query.trim() || undefined,
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
            return newParams;
        });
    }, []);

    const reset = useCallback(() => {
        setParams({
            page: 0,
            size: 10
        });
    }, []);

    return {
        params,
        setPage,
        setSize,
        setQuery,
        setSort,
        setFilter,
        removeFilter,
        reset
    };
};