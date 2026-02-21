import { useCallback, useState, useEffect } from 'react';
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

    const [debouncedLoading, setDebouncedLoading] = useState(false);

    const rawLoading = currentlyShowingApi.loading || upcomingApi.loading ||
        archivedApi.loading || movieDetailApi.loading || searchApi.loading ||
        createApi.loading || updateApi.loading || deleteApi.loading;

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedLoading(rawLoading);
        }, 50);
        return () => clearTimeout(timer);
    }, [rawLoading]);

    const getCurrentlyShowing = useCallback(async (params?: SearchParams) => {
        return currentlyShowingApi.execute(
            () => movieApi.public.getCurrentlyShowing(params)
        );
    }, [currentlyShowingApi]);

    const getUpcoming = useCallback(async (params?: SearchParams) => {
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

    const getBySlug = useCallback(async (slug: string) => {
        return movieDetailApi.execute(
            () => movieApi.public.getBySlug(slug)
        );
    }, [movieDetailApi]);

    const searchMoviesForSession = useCallback(async (search?: string) => {
        return searchApi.execute(
            () => movieApi.public.searchMoviesForSession(search)
        );
    }, [searchApi]);

    const adminSearchMoviesForSession = useCallback(async (search?: string) => {
        return searchApi.execute(
            () => movieApi.admin.searchMoviesForSession(search)
        );
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
        loading: debouncedLoading,
        error,
        getCurrentlyShowing,
        getUpcoming,
        getArchived,
        getById,
        getBySlug,
        searchMoviesForSession,
        adminSearchMoviesForSession,
        create,
        update,
        remove,
        clearCache,
        getPosterUrl,
        reset: currentlyShowingApi.reset,
    };
};