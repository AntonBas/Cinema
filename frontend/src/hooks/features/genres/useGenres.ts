import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface GenreSearchParams extends SearchParams {
    query?: string;
    limit?: number;
}

export const useGenres = () => {
    const genresApi = useApi<PageResponse<GenreResponse>>();
    const genreApiInstance = useApi<GenreResponse>();
    const searchApi = useApi<GenreResponse[]>();
    const mutationApi = useApi<GenreResponse | void>();

    const rawLoading = genresApi.loading || genreApiInstance.loading ||
        searchApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(genresApi.error || genreApiInstance.error ||
        searchApi.error || mutationApi.error);

    const getAll = useCallback(async (params?: SearchParams) => {
        const response = await genresApi.execute(
            () => genreApi.admin.getAll(params),
            {
                cacheKey: `genres_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [genresApi]);

    const getById = useCallback(async (id: number) => {
        const response = await genreApiInstance.execute(
            () => genreApi.admin.getById(id),
            {
                cacheKey: `genre_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [genreApiInstance]);

    const search = useCallback(async (params?: GenreSearchParams) => {
        const { query = '', limit = 10 } = params || {};
        const response = await searchApi.execute(
            () => genreApi.public.search(query, limit),
            {
                cacheKey: `genres_search_${query}_${limit}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [searchApi]);

    const create = useCallback(async (request: GenreRequest) => {
        const response = await mutationApi.execute(
            () => genreApi.admin.create(request),
            {
                successMessage: 'Genre created successfully',
            }
        );
        genresApi.invalidateCache();
        searchApi.invalidateCache();
        return response || null;
    }, [mutationApi, genresApi, searchApi]);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        const response = await mutationApi.execute(
            () => genreApi.admin.update(id, request),
            {
                successMessage: 'Genre updated successfully',
            }
        );
        genresApi.invalidateCache();
        genreApiInstance.invalidateCache(`genre_${id}`);
        searchApi.invalidateCache();
        return response || null;
    }, [mutationApi, genresApi, genreApiInstance, searchApi]);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => genreApi.admin.delete(id),
            {
                successMessage: 'Genre deleted successfully',
            }
        );
        genresApi.invalidateCache();
        genreApiInstance.invalidateCache(`genre_${id}`);
        searchApi.invalidateCache();
    }, [mutationApi, genresApi, genreApiInstance, searchApi]);

    const clearCache = useCallback(() => {
        genresApi.invalidateCache();
        genreApiInstance.invalidateCache();
        searchApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [genresApi, genreApiInstance, searchApi, mutationApi]);

    const resetAll = useCallback(() => {
        genresApi.reset();
        genreApiInstance.reset();
        searchApi.reset();
        mutationApi.reset();
    }, [genresApi, genreApiInstance, searchApi, mutationApi]);

    return {
        allGenres: genresApi.data?.content || [],
        genre: genreApiInstance.data,
        searchResults: searchApi.data || [],

        pagination: genresApi.data,
        currentPage: genresApi.data?.number || 0,
        totalPages: genresApi.data?.totalPages || 0,
        totalElements: genresApi.data?.totalElements || 0,
        pageSize: genresApi.data?.size || 20,

        loading,
        error,

        getAll,
        getById,
        search,
        create,
        update,
        remove,
        clearCache,
        resetAll,
    };
};