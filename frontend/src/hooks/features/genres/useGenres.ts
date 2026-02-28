import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useGenres = () => {
    const allGenresApi = useApi<PageResponse<GenreResponse>>();
    const genreByIdApi = useApi<GenreResponse>();
    const searchGenresApi = useApi<GenreResponse[]>();
    const genreByIdsApi = useApi<GenreResponse[]>();
    const createGenreApi = useApi<GenreResponse>();
    const updateGenreApi = useApi<GenreResponse>();
    const deleteGenreApi = useApi<void>();

    const getAll = useCallback(async (params?: any) => {
        const response = await allGenresApi.execute(
            () => genreApi.admin.getAll(params),
            {
                cacheKey: `genres_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [allGenresApi]);

    const getById = useCallback(async (id: number) => {
        const response = await genreByIdApi.execute(
            () => genreApi.admin.getById(id),
            {
                cacheKey: `genre_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [genreByIdApi]);

    const search = useCallback(async (query: string, limit: number = 10) => {
        const response = await searchGenresApi.execute(
            () => genreApi.public.search(query, limit),
            {
                cacheKey: `genres_search_${query}_${limit}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [searchGenresApi]);

    const getByIds = useCallback(async (ids: number[]) => {
        if (!ids.length) return [];
        const response = await genreByIdsApi.execute(
            () => genreApi.public.search('', 100),
            {
                cacheKey: `genres_ids_${ids.join('_')}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [genreByIdsApi]);

    const create = useCallback(async (request: GenreRequest) => {
        const response = await createGenreApi.execute(
            () => genreApi.admin.create(request),
            {
                successMessage: 'Genre created successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    searchGenresApi.invalidateCache();
                },
            }
        );
        return response?.data || null;
    }, [createGenreApi, allGenresApi, searchGenresApi]);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        const response = await updateGenreApi.execute(
            () => genreApi.admin.update(id, request),
            {
                successMessage: 'Genre updated successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    genreByIdApi.invalidateCache(`genre_${id}`);
                    searchGenresApi.invalidateCache();
                },
            }
        );
        return response?.data || null;
    }, [updateGenreApi, allGenresApi, genreByIdApi, searchGenresApi]);

    const remove = useCallback(async (id: number) => {
        await deleteGenreApi.execute(
            () => genreApi.admin.delete(id),
            {
                successMessage: 'Genre deleted successfully',
                onSuccess: () => {
                    allGenresApi.invalidateCache();
                    searchGenresApi.invalidateCache();
                },
            }
        );
    }, [deleteGenreApi, allGenresApi, searchGenresApi]);

    const clearCache = useCallback(() => {
        allGenresApi.invalidateCache();
        genreByIdApi.invalidateCache();
        searchGenresApi.invalidateCache();
        genreByIdsApi.invalidateCache();
        createGenreApi.invalidateCache();
        updateGenreApi.invalidateCache();
        deleteGenreApi.invalidateCache();
    }, [allGenresApi, genreByIdApi, searchGenresApi, genreByIdsApi,
        createGenreApi, updateGenreApi, deleteGenreApi]);

    const loading = allGenresApi.loading || genreByIdApi.loading ||
        searchGenresApi.loading || genreByIdsApi.loading ||
        createGenreApi.loading || updateGenreApi.loading ||
        deleteGenreApi.loading;

    const error = !!(allGenresApi.error || genreByIdApi.error ||
        searchGenresApi.error || genreByIdsApi.error ||
        createGenreApi.error || updateGenreApi.error ||
        deleteGenreApi.error);

    return {
        allGenres: allGenresApi.data?.content || [],
        genre: genreByIdApi.data,
        searchResults: searchGenresApi.data || [],

        pagination: allGenresApi.data,
        currentPage: allGenresApi.data?.number || 0,
        totalPages: allGenresApi.data?.totalPages || 0,
        totalElements: allGenresApi.data?.totalElements || 0,
        pageSize: allGenresApi.data?.size || 20,

        loading,
        error,

        getAll,
        getById,
        search,
        getByIds,
        create,
        update,
        remove,
        clearCache,

        resetAllGenres: allGenresApi.reset,
        resetGenre: genreByIdApi.reset,
        resetSearch: searchGenresApi.reset,
    };
};