import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useGenres = () => {
    const allGenresApi = useApi<PageResponse<GenreResponse>>();
    const genreByIdApi = useApi<GenreResponse>();
    const popularGenresApi = useApi<GenreResponse[]>();

    const getAll = useCallback(async (params?: any) => {
        return allGenresApi.callApi(
            () => genreApi.admin.getAll(params),
            {
                cacheKey: `genres_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allGenresApi]);

    const getPopular = useCallback(async (query?: string, limit: number = 10) => {
        const api = useApi<GenreResponse[]>();
        return api.callApi(
            () => genreApi.public.getPopular(query, limit),
            {
                cacheKey: `genres_popular_${query || 'all'}_${limit}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const getById = useCallback(async (id: number) => {
        return genreByIdApi.callApi(
            () => genreApi.public.getById(id),
            {
                cacheKey: `genre_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [genreByIdApi]);

    const getByIds = useCallback(async (ids: number[]) => {
        const api = useApi<GenreResponse[]>();
        return api.callApi(
            () => genreApi.public.getByIds(ids),
            {
                cacheKey: `genres_ids_${ids.join('_')}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const create = useCallback(async (request: GenreRequest) => {
        const api = useApi<GenreResponse>();
        return api.callApi(
            () => genreApi.admin.create(request),
            {
                successMessage: 'Genre created successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                },
            }
        );
    }, [allGenresApi]);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        const api = useApi<GenreResponse>();
        return api.callApi(
            () => genreApi.admin.update(id, request),
            {
                successMessage: 'Genre updated successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    genreByIdApi.invalidateCache(`genre_${id}`);
                },
            }
        );
    }, [allGenresApi, genreByIdApi]);

    const remove = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => genreApi.admin.delete(id),
            {
                successMessage: 'Genre deleted successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                },
            }
        );
    }, [allGenresApi]);

    const getAdminPopular = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<GenreResponse>>();
        return api.callApi(
            () => genreApi.admin.getPopular(params),
            {
                cacheKey: `genres_admin_popular_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const clearCache = useCallback(() => {
        allGenresApi.invalidateCache();
        genreByIdApi.invalidateCache();
        popularGenresApi.invalidateCache();
    }, [allGenresApi, genreByIdApi, popularGenresApi]);

    return {
        allGenres: allGenresApi.data?.content || [],
        genre: genreByIdApi.data,
        popularGenres: popularGenresApi.data || [],

        loading: allGenresApi.state.isLoading || genreByIdApi.state.isLoading || popularGenresApi.state.isLoading,
        error: allGenresApi.state.isError || genreByIdApi.state.isError || popularGenresApi.state.isError,

        getAll,
        getPopular,
        getById,
        getByIds,
        create,
        update,
        remove,
        getAdminPopular,
        clearCache,

        resetAllGenres: allGenresApi.reset,
        resetGenre: genreByIdApi.reset,
        resetPopularGenres: popularGenresApi.reset,
        refetchAllGenres: allGenresApi.refetch,
        refetchGenre: genreByIdApi.refetch,

        pagination: allGenresApi.data,
        currentPage: allGenresApi.data?.number || 0,
        totalPages: allGenresApi.data?.totalPages || 0,
        totalElements: allGenresApi.data?.totalElements || 0,
        pageSize: allGenresApi.data?.size || 10,
        isEmpty: allGenresApi.data?.empty || false,
    };
};