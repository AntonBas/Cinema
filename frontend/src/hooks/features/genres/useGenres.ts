import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useGenres = () => {
    const allGenresApi = useApi<PageResponse<GenreResponse>>();
    const genreByIdApi = useApi<GenreResponse>();
    const popularGenresApi = useApi<GenreResponse[]>();
    const publicPopularGenresApi = useApi<GenreResponse[]>();
    const genreByIdsApi = useApi<GenreResponse[]>();
    const createGenreApi = useApi<GenreResponse>();
    const updateGenreApi = useApi<GenreResponse>();
    const deleteGenreApi = useApi<void>();
    const adminPopularGenresApi = useApi<PageResponse<GenreResponse>>();

    const getAll = useCallback(async (params?: any) => {
        return allGenresApi.execute(
            () => genreApi.admin.getAll(params),
            {
                cacheKey: `genres_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allGenresApi]);

    const getPopular = useCallback(async (query?: string, limit: number = 10) => {
        return publicPopularGenresApi.execute(
            () => genreApi.public.getPopular(query, limit),
            {
                cacheKey: `genres_popular_${query || 'all'}_${limit}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [publicPopularGenresApi]);

    const getById = useCallback(async (id: number) => {
        return genreByIdApi.execute(
            () => genreApi.public.getById(id),
            {
                cacheKey: `genre_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [genreByIdApi]);

    const getByIds = useCallback(async (ids: number[]) => {
        return genreByIdsApi.execute(
            () => genreApi.public.getByIds(ids),
            {
                cacheKey: `genres_ids_${ids.join('_')}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [genreByIdsApi]);

    const create = useCallback(async (request: GenreRequest) => {
        return createGenreApi.execute(
            () => genreApi.admin.create(request),
            {
                successMessage: 'Genre created successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    publicPopularGenresApi.invalidateCache();
                },
            }
        );
    }, [createGenreApi, allGenresApi, publicPopularGenresApi]);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        return updateGenreApi.execute(
            () => genreApi.admin.update(id, request),
            {
                successMessage: 'Genre updated successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    genreByIdApi.invalidateCache(`genre_${id}`);
                    publicPopularGenresApi.invalidateCache();
                },
            }
        );
    }, [updateGenreApi, allGenresApi, genreByIdApi, publicPopularGenresApi]);

    const remove = useCallback(async (id: number) => {
        return deleteGenreApi.execute(
            () => genreApi.admin.delete(id),
            {
                successMessage: 'Genre deleted successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    publicPopularGenresApi.invalidateCache();
                },
            }
        );
    }, [deleteGenreApi, allGenresApi, publicPopularGenresApi]);

    const getAdminPopular = useCallback(async (params?: any) => {
        return adminPopularGenresApi.execute(
            () => genreApi.admin.getPopular(params),
            {
                cacheKey: `genres_admin_popular_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, [adminPopularGenresApi]);

    const clearCache = useCallback(() => {
        allGenresApi.invalidateCache();
        genreByIdApi.invalidateCache();
        popularGenresApi.invalidateCache();
        publicPopularGenresApi.invalidateCache();
        genreByIdsApi.invalidateCache();
        createGenreApi.invalidateCache();
        updateGenreApi.invalidateCache();
        deleteGenreApi.invalidateCache();
        adminPopularGenresApi.invalidateCache();
    }, [allGenresApi, genreByIdApi, popularGenresApi, publicPopularGenresApi,
        genreByIdsApi, createGenreApi, updateGenreApi, deleteGenreApi, adminPopularGenresApi]);

    const loading = allGenresApi.loading || genreByIdApi.loading ||
        publicPopularGenresApi.loading || genreByIdsApi.loading ||
        createGenreApi.loading || updateGenreApi.loading ||
        deleteGenreApi.loading || adminPopularGenresApi.loading;

    const error = !!(allGenresApi.error || genreByIdApi.error ||
        publicPopularGenresApi.error || genreByIdsApi.error ||
        createGenreApi.error || updateGenreApi.error ||
        deleteGenreApi.error || adminPopularGenresApi.error);

    return {
        allGenres: allGenresApi.data?.content || [],
        genre: genreByIdApi.data,
        popularGenres: publicPopularGenresApi.data || [],
        adminPopularGenres: adminPopularGenresApi.data?.content || [],

        loading,
        error,

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
        resetPopularGenres: publicPopularGenresApi.reset,

        pagination: allGenresApi.data,
        currentPage: allGenresApi.data?.number || 0,
        totalPages: allGenresApi.data?.totalPages || 0,
        totalElements: allGenresApi.data?.totalElements || 0,
        pageSize: allGenresApi.data?.size || 10,
        isEmpty: allGenresApi.data?.empty || false,
    };
};