import { useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieDetailResponse,
    MovieSessionSearchResponse,
    MovieCreateRequest,
    MovieUpdateRequest
} from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useMovies = () => {
    const allMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const movieByIdApi = useApi<MovieDetailResponse>();
    const movieBySlugApi = useApi<MovieDetailResponse>();
    const currentlyShowingApi = useApi<PageResponse<MovieCardResponse>>();
    const upcomingApi = useApi<PageResponse<MovieCardResponse>>();

    const getAll = useCallback(async (params?: any) => {
        return allMoviesApi.callApi(
            () => movieApi.public.getMovies(params),
            {
                cacheKey: `movies_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allMoviesApi]);

    const getCurrentlyShowing = useCallback(async (params?: any) => {
        return currentlyShowingApi.callApi(
            () => movieApi.public.getCurrentlyShowing(params),
            {
                cacheKey: `movies_currently_showing_${JSON.stringify(params)}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [currentlyShowingApi]);

    const getUpcoming = useCallback(async (params?: any) => {
        return upcomingApi.callApi(
            () => movieApi.public.getUpcoming(params),
            {
                cacheKey: `movies_upcoming_${JSON.stringify(params)}`,
                cacheTime: 15 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [upcomingApi]);

    const getById = useCallback(async (id: number) => {
        return movieByIdApi.callApi(
            () => movieApi.public.getById(id),
            {
                cacheKey: `movie_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieByIdApi]);

    const getBySlug = useCallback(async (slug: string) => {
        return movieBySlugApi.callApi(
            () => movieApi.public.getBySlug(slug),
            {
                cacheKey: `movie_slug_${slug}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieBySlugApi]);

    const searchMoviesForSession = useCallback(async (search?: string) => {
        const api = useApi<MovieSessionSearchResponse[]>();
        return api.callApi(
            () => movieApi.public.searchMoviesForSession(search),
            {
                cacheKey: `movies_search_${search || 'all'}`,
                cacheTime: 2 * 60 * 1000,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, []);

    const create = useCallback(async (request: MovieCreateRequest) => {
        const api = useApi<MovieDetailResponse>();
        return api.callApi(
            () => movieApi.admin.create(request),
            {
                successMessage: 'Movie created successfully',
                onSuccess: () => {
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                },
            }
        );
    }, [allMoviesApi, currentlyShowingApi, upcomingApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const api = useApi<MovieDetailResponse>();
        return api.callApi(
            () => movieApi.admin.update(id, request),
            {
                successMessage: 'Movie updated successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                },
            }
        );
    }, [movieByIdApi, movieBySlugApi, allMoviesApi, currentlyShowingApi, upcomingApi]);

    const remove = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => movieApi.admin.delete(id),
            {
                successMessage: 'Movie deleted successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                },
            }
        );
    }, [movieByIdApi, movieBySlugApi, allMoviesApi, currentlyShowingApi, upcomingApi]);

    const getAdminMovies = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<MovieCardResponse>>();
        return api.callApi(
            () => movieApi.admin.getMovies(params),
            {
                cacheKey: `movies_admin_${JSON.stringify(params)}`,
                cacheTime: 2 * 60 * 1000,
            }
        );
    }, []);

    const clearCache = useCallback(() => {
        allMoviesApi.invalidateCache();
        movieByIdApi.invalidateCache();
        movieBySlugApi.invalidateCache();
        currentlyShowingApi.invalidateCache();
        upcomingApi.invalidateCache();
    }, [allMoviesApi, movieByIdApi, movieBySlugApi, currentlyShowingApi, upcomingApi]);

    const getPosterUrl = useCallback((id: number): string => {
        return movieApi.public.getPosterUrl(id);
    }, []);

    return {
        allMovies: allMoviesApi.data?.content || [],
        currentlyShowing: currentlyShowingApi.data?.content || [],
        upcoming: upcomingApi.data?.content || [],
        movie: movieByIdApi.data || movieBySlugApi.data,
        movieBySlug: movieBySlugApi.data,

        loading: allMoviesApi.state.isLoading || movieByIdApi.state.isLoading ||
            movieBySlugApi.state.isLoading || currentlyShowingApi.state.isLoading ||
            upcomingApi.state.isLoading,
        error: allMoviesApi.state.isError || movieByIdApi.state.isError ||
            movieBySlugApi.state.isError || currentlyShowingApi.state.isError ||
            upcomingApi.state.isError,

        getAll,
        getCurrentlyShowing,
        getUpcoming,
        getById,
        getBySlug,
        searchMoviesForSession,
        create,
        update,
        remove,
        getAdminMovies,
        clearCache,
        getPosterUrl,

        resetAllMovies: allMoviesApi.reset,
        resetMovie: movieByIdApi.reset,
        resetMovieBySlug: movieBySlugApi.reset,
        resetCurrentlyShowing: currentlyShowingApi.reset,
        resetUpcoming: upcomingApi.reset,
        refetchAllMovies: allMoviesApi.refetch,
        refetchCurrentlyShowing: currentlyShowingApi.refetch,
        refetchUpcoming: upcomingApi.refetch,

        pagination: allMoviesApi.data,
        currentlyShowingPagination: currentlyShowingApi.data,
        upcomingPagination: upcomingApi.data,
    };
};