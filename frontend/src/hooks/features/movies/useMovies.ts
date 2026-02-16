import { useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieDetailResponse,
    MovieSessionSearchResponse,
    MovieCreateRequest,
    MovieUpdateRequest
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useMovies = () => {
    const currentlyShowingApi = useApi<PageResponse<MovieCardResponse>>();
    const upcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const archivedApi = useApi<PageResponse<MovieCardResponse>>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const searchApi = useApi<MovieSessionSearchResponse[]>();
    const createApi = useApi<MovieDetailResponse>();
    const updateApi = useApi<MovieDetailResponse>();
    const deleteApi = useApi<void>();

    const getCurrentlyShowing = useCallback(async (params?: SearchParams, isAdmin: boolean = false) => {
        if (isAdmin) {
            return currentlyShowingApi.execute(
                () => movieApi.admin.getFilteredMovies({ currentlyShowing: true }, params)
            );
        }
        return currentlyShowingApi.execute(
            () => movieApi.public.getCurrentlyShowing(params)
        );
    }, [currentlyShowingApi]);

    const getUpcoming = useCallback(async (params?: SearchParams, isAdmin: boolean = false) => {
        if (isAdmin) {
            return upcomingApi.execute(
                () => movieApi.admin.getFilteredMovies({ upcoming: true }, params)
            );
        }
        return upcomingApi.execute(
            () => movieApi.public.getUpcoming(params)
        );
    }, [upcomingApi]);

    const getArchived = useCallback(async (params?: SearchParams) => {
        return archivedApi.execute(
            () => movieApi.admin.getFilteredMovies({ archived: true }, params)
        );
    }, [archivedApi]);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        const apiCall = isAdmin
            ? () => movieApi.admin.getMovieById(id)
            : () => movieApi.public.getById(id);
        return movieDetailApi.execute(apiCall);
    }, [movieDetailApi]);

    const searchMoviesForSession = useCallback(async (search?: string, isAdmin: boolean = false) => {
        const apiCall = isAdmin
            ? () => movieApi.admin.searchMoviesForSession(search)
            : () => movieApi.public.searchMoviesForSession(search);
        return searchApi.execute(apiCall);
    }, [searchApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        return createApi.execute(
            () => movieApi.admin.create(request)
        );
    }, [createApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        return updateApi.execute(
            () => movieApi.admin.update(id, request)
        );
    }, [updateApi]);

    const remove = useCallback(async (id: number) => {
        return deleteApi.execute(
            () => movieApi.admin.delete(id)
        );
    }, [deleteApi]);

    const clearCache = useCallback(() => {
        currentlyShowingApi.invalidateCache();
        upcomingApi.invalidateCache();
        archivedApi.invalidateCache();
        movieDetailApi.invalidateCache();
        searchApi.invalidateCache();
        createApi.invalidateCache();
        updateApi.invalidateCache();
        deleteApi.invalidateCache();
    }, [currentlyShowingApi, upcomingApi, archivedApi, movieDetailApi, searchApi, createApi, updateApi, deleteApi]);

    const getPosterUrl = useCallback((id: number): string => {
        return movieApi.public.getPosterUrl(id);
    }, []);

    const loading = currentlyShowingApi.loading || upcomingApi.loading ||
        archivedApi.loading || movieDetailApi.loading || searchApi.loading ||
        createApi.loading || updateApi.loading || deleteApi.loading;

    const error = !!(currentlyShowingApi.error || upcomingApi.error ||
        archivedApi.error || movieDetailApi.error || searchApi.error ||
        createApi.error || updateApi.error || deleteApi.error);

    return {
        currentlyShowing: currentlyShowingApi.data?.content || [],
        upcoming: upcomingApi.data?.content || [],
        archived: archivedApi.data?.content || [],
        currentlyShowingPagination: currentlyShowingApi.data,
        upcomingPagination: upcomingApi.data,
        archivedPagination: archivedApi.data,
        movie: movieDetailApi.data,
        searchResults: searchApi.data || [],

        loading,
        error,

        getCurrentlyShowing,
        getUpcoming,
        getArchived,
        getById,
        searchMoviesForSession,
        create,
        update,
        remove,
        clearCache,
        getPosterUrl,

        reset: currentlyShowingApi.reset,
    };
};