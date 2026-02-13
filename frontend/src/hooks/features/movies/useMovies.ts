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
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const createMovieApi = useApi<MovieDetailResponse>();
    const updateMovieApi = useApi<MovieDetailResponse>();
    const deleteMovieApi = useApi<void>();
    const adminMoviesApi = useApi<PageResponse<MovieCardResponse>>();

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
        return searchMoviesApi.callApi(
            () => movieApi.public.searchMoviesForSession(search),
            {
                cacheKey: `movies_search_${search || 'all'}`,
                cacheTime: 2 * 60 * 1000,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, [searchMoviesApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        return createMovieApi.callApi(
            () => movieApi.admin.create(request),
            {
                successMessage: 'Movie created successfully',
                onSuccess: () => {
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    adminMoviesApi.invalidateCache();
                },
            }
        );
    }, [createMovieApi, allMoviesApi, currentlyShowingApi, upcomingApi, adminMoviesApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        return updateMovieApi.callApi(
            () => movieApi.admin.update(id, request),
            {
                successMessage: 'Movie updated successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    adminMoviesApi.invalidateCache();
                },
            }
        );
    }, [updateMovieApi, movieByIdApi, movieBySlugApi, allMoviesApi, currentlyShowingApi, upcomingApi, adminMoviesApi]);

    const remove = useCallback(async (id: number) => {
        return deleteMovieApi.callApi(
            () => movieApi.admin.delete(id),
            {
                successMessage: 'Movie deleted successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    allMoviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    adminMoviesApi.invalidateCache();
                },
            }
        );
    }, [deleteMovieApi, movieByIdApi, movieBySlugApi, allMoviesApi, currentlyShowingApi, upcomingApi, adminMoviesApi]);

    const getAdminMovies = useCallback(async (params?: any) => {
        return adminMoviesApi.callApi(
            () => movieApi.admin.getMovies(params),
            {
                cacheKey: `movies_admin_${JSON.stringify(params)}`,
                cacheTime: 2 * 60 * 1000,
            }
        );
    }, [adminMoviesApi]);

    const clearCache = useCallback(() => {
        allMoviesApi.invalidateCache();
        movieByIdApi.invalidateCache();
        movieBySlugApi.invalidateCache();
        currentlyShowingApi.invalidateCache();
        upcomingApi.invalidateCache();
        searchMoviesApi.invalidateCache();
        adminMoviesApi.invalidateCache();
    }, [allMoviesApi, movieByIdApi, movieBySlugApi, currentlyShowingApi, upcomingApi, searchMoviesApi, adminMoviesApi]);

    const getPosterUrl = useCallback((id: number): string => {
        return movieApi.public.getPosterUrl(id);
    }, []);

    return {
        allMovies: allMoviesApi.data?.content || [],
        currentlyShowing: currentlyShowingApi.data?.content || [],
        upcoming: upcomingApi.data?.content || [],
        movie: movieByIdApi.data || movieBySlugApi.data,
        movieBySlug: movieBySlugApi.data,
        searchResults: searchMoviesApi.data || [],
        adminMovies: adminMoviesApi.data?.content || [],

        loading: allMoviesApi.state.isLoading || movieByIdApi.state.isLoading ||
            movieBySlugApi.state.isLoading || currentlyShowingApi.state.isLoading ||
            upcomingApi.state.isLoading || searchMoviesApi.state.isLoading ||
            createMovieApi.state.isLoading || updateMovieApi.state.isLoading ||
            deleteMovieApi.state.isLoading || adminMoviesApi.state.isLoading,
        error: allMoviesApi.state.isError || movieByIdApi.state.isError ||
            movieBySlugApi.state.isError || currentlyShowingApi.state.isError ||
            upcomingApi.state.isError || searchMoviesApi.state.isError ||
            createMovieApi.state.isError || updateMovieApi.state.isError ||
            deleteMovieApi.state.isError || adminMoviesApi.state.isError,

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
        adminMoviesPagination: adminMoviesApi.data,
    };
};