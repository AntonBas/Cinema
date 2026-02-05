import { useState, useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest, GenreStatsResponse } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useGenres = () => {
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [statsGenres, setStatsGenres] = useState<GenreStatsResponse[]>([]);
    const [paginationData, setPaginationData] = useState<PageResponse<GenreStatsResponse> | null>(null);
    const [currentSearch, setCurrentSearch] = useState<string>('');

    const publicGetByIdHook = useApi<GenreResponse>();
    const publicGetPopularHook = useApi<GenreResponse[]>();
    const publicGetByIdsHook = useApi<GenreResponse[]>();
    const adminCreateHook = useApi<GenreResponse>();
    const adminGetByIdHook = useApi<GenreResponse>();
    const adminUpdateHook = useApi<GenreResponse>();
    const adminDeleteHook = useApi<void>();
    const adminGetAllWithStatsHook = useApi<PageResponse<GenreStatsResponse>>();

    const getById = useCallback(async (id: number): Promise<GenreResponse> => {
        return publicGetByIdHook.callApi(async () => {
            return await genreApi.public.getById(id);
        });
    }, [publicGetByIdHook]);

    const getPopular = useCallback(async (query?: string, limit: number = 10): Promise<GenreResponse[]> => {
        return publicGetPopularHook.callApi(async () => {
            return await genreApi.public.getPopular(query, limit);
        });
    }, [publicGetPopularHook]);

    const getByIds = useCallback(async (ids: number[]): Promise<GenreResponse[]> => {
        return publicGetByIdsHook.callApi(async () => {
            return await genreApi.public.getByIds(ids);
        });
    }, [publicGetByIdsHook]);

    const create = useCallback(async (genreData: GenreRequest): Promise<GenreResponse> => {
        return adminCreateHook.callApi(async () => {
            const genre = await genreApi.admin.create(genreData);
            setGenres(prev => [...prev, genre]);
            return genre;
        });
    }, [adminCreateHook]);

    const update = useCallback(async (id: number, genreData: GenreRequest): Promise<GenreResponse> => {
        return adminUpdateHook.callApi(async () => {
            const genre = await genreApi.admin.update(id, genreData);
            setGenres(prev => prev.map(g => g.id === id ? genre : g));
            setStatsGenres(prev => prev.map(sg => sg.id === id ? { ...sg, ...genre } : sg));
            return genre;
        });
    }, [adminUpdateHook]);

    const remove = useCallback(async (id: number): Promise<void> => {
        return adminDeleteHook.callApi(async () => {
            await genreApi.admin.delete(id);
            setGenres(prev => prev.filter(g => g.id !== id));
            setStatsGenres(prev => prev.filter(sg => sg.id !== id));
        });
    }, [adminDeleteHook]);

    const getAdminById = useCallback(async (id: number): Promise<GenreResponse> => {
        return adminGetByIdHook.callApi(async () => {
            return await genreApi.admin.getById(id);
        });
    }, [adminGetByIdHook]);

    const getAllWithStats = useCallback(async (params?: SearchParams): Promise<PageResponse<GenreStatsResponse>> => {
        return adminGetAllWithStatsHook.callApi(async () => {
            const response = await genreApi.admin.getAllWithStats(params);
            setStatsGenres(response.content);
            setPaginationData(response);
            return response;
        });
    }, [adminGetAllWithStatsHook]);

    const searchGenres = useCallback(async (searchTerm: string, page?: number): Promise<PageResponse<GenreStatsResponse>> => {
        return adminGetAllWithStatsHook.callApi(async () => {
            const params: SearchParams = {
                page: page !== undefined ? page : 0,
                size: 20,
                ...(searchTerm.trim() && { search: searchTerm })
            };

            const response = await genreApi.admin.getAllWithStats(params);
            setStatsGenres(response.content);
            setPaginationData(response);
            setCurrentSearch(searchTerm);
            return response;
        });
    }, [adminGetAllWithStatsHook]);

    const refresh = useCallback(() => {
        if (paginationData) {
            getAllWithStats({
                page: paginationData.number,
                size: paginationData.size,
                ...(currentSearch && { search: currentSearch })
            });
        }
    }, [paginationData, getAllWithStats, currentSearch]);

    const nextPage = useCallback(async (): Promise<PageResponse<GenreStatsResponse> | null> => {
        if (!paginationData || paginationData.last) return null;
        return getAllWithStats({
            page: paginationData.number + 1,
            size: paginationData.size,
            ...(currentSearch && { search: currentSearch })
        });
    }, [paginationData, getAllWithStats, currentSearch]);

    const prevPage = useCallback(async (): Promise<PageResponse<GenreStatsResponse> | null> => {
        if (!paginationData || paginationData.first) return null;
        return getAllWithStats({
            page: paginationData.number - 1,
            size: paginationData.size,
            ...(currentSearch && { search: currentSearch })
        });
    }, [paginationData, getAllWithStats, currentSearch]);

    const reset = useCallback(() => {
        setGenres([]);
        setStatsGenres([]);
        setPaginationData(null);
        setCurrentSearch('');
        publicGetByIdHook.reset();
        publicGetPopularHook.reset();
        publicGetByIdsHook.reset();
        adminCreateHook.reset();
        adminGetByIdHook.reset();
        adminUpdateHook.reset();
        adminDeleteHook.reset();
        adminGetAllWithStatsHook.reset();
    }, [publicGetByIdHook, publicGetPopularHook, publicGetByIdsHook, adminCreateHook, adminGetByIdHook, adminUpdateHook, adminDeleteHook, adminGetAllWithStatsHook]);

    return {
        genres,
        statsGenres,
        pagination: paginationData,
        currentSearch,
        loading: {
            getById: publicGetByIdHook.loading,
            getPopular: publicGetPopularHook.loading,
            getByIds: publicGetByIdsHook.loading,
            create: adminCreateHook.loading,
            update: adminUpdateHook.loading,
            remove: adminDeleteHook.loading,
            getAdminById: adminGetByIdHook.loading,
            getAllWithStats: adminGetAllWithStatsHook.loading,
        },
        errors: {
            getById: publicGetByIdHook.error,
            getPopular: publicGetPopularHook.error,
            getByIds: publicGetByIdsHook.error,
            create: adminCreateHook.error,
            update: adminUpdateHook.error,
            remove: adminDeleteHook.error,
            getAdminById: adminGetByIdHook.error,
            getAllWithStats: adminGetAllWithStatsHook.error,
        },
        getById,
        getPopular,
        getByIds,
        create,
        update,
        remove,
        getAdminById,
        getAllWithStats,
        searchGenres,
        refresh,
        nextPage,
        prevPage,
        reset,
        currentPage: paginationData?.number || 0,
        totalPages: paginationData?.totalPages || 0,
        totalElements: paginationData?.totalElements || 0,
        pageSize: paginationData?.size || 0,
        isEmpty: paginationData?.empty || false,
        isFirstPage: paginationData?.first || true,
        isLastPage: paginationData?.last || true,
    };
};