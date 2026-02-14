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
    const moviesApi = useApi<PageResponse<MovieCardResponse>>();
    const movieByIdApi = useApi<MovieDetailResponse>();
    const movieBySlugApi = useApi<MovieDetailResponse>();
    const currentlyShowingApi = useApi<PageResponse<MovieCardResponse>>();
    const upcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const archivedApi = useApi<PageResponse<MovieCardResponse>>();
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const createMovieApi = useApi<MovieDetailResponse>();
    const updateMovieApi = useApi<MovieDetailResponse>();
    const deleteMovieApi = useApi<void>();

    const adminMovieByIdApi = useApi<MovieDetailResponse>();
    const adminMovieBySlugApi = useApi<MovieDetailResponse>();

    const getFilteredMovies = useCallback(async (params?: any) => {
        const isAdmin = params?.isAdmin || false;
        const apiCall = isAdmin
            ? () => movieApi.admin.getFilteredMovies(params)
            : () => movieApi.public.getFilteredMovies(params);

        return moviesApi.callApi(apiCall, {
            cacheKey: `movies_filtered_${JSON.stringify(params)}`,
            cacheTime: 5 * 60 * 1000,
            showErrorNotification: false,
        });
    }, [moviesApi]);

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

    const getArchived = useCallback(async (params?: any) => {
        const filterParams = { ...params, archived: true };
        return archivedApi.callApi(
            () => movieApi.admin.getFilteredMovies(filterParams),
            {
                cacheKey: `movies_archived_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [archivedApi]);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        if (isAdmin) {
            return adminMovieByIdApi.callApi(
                () => movieApi.admin.getMovieById(id),
                {
                    cacheKey: `admin_movie_${id}`,
                    cacheTime: 5 * 60 * 1000,
                    showErrorNotification: false,
                }
            );
        }
        return movieByIdApi.callApi(
            () => movieApi.public.getById(id),
            {
                cacheKey: `movie_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieByIdApi, adminMovieByIdApi]);

    const getBySlug = useCallback(async (slug: string, isAdmin: boolean = false) => {
        if (isAdmin) {
            return adminMovieBySlugApi.callApi(
                () => movieApi.admin.getMovieBySlug(slug),
                {
                    cacheKey: `admin_movie_slug_${slug}`,
                    cacheTime: 5 * 60 * 1000,
                    showErrorNotification: false,
                }
            );
        }
        return movieBySlugApi.callApi(
            () => movieApi.public.getBySlug(slug),
            {
                cacheKey: `movie_slug_${slug}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieBySlugApi, adminMovieBySlugApi]);

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
                    moviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    archivedApi.invalidateCache();
                },
            }
        );
    }, [createMovieApi, moviesApi, currentlyShowingApi, upcomingApi, archivedApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        return updateMovieApi.callApi(
            () => movieApi.admin.update(id, request),
            {
                successMessage: 'Movie updated successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    adminMovieByIdApi.invalidateCache(`admin_movie_${id}`);
                    adminMovieBySlugApi.invalidateCache();
                    moviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    archivedApi.invalidateCache();
                },
            }
        );
    }, [updateMovieApi, movieByIdApi, movieBySlugApi, adminMovieByIdApi, adminMovieBySlugApi, moviesApi, currentlyShowingApi, upcomingApi, archivedApi]);

    const remove = useCallback(async (id: number) => {
        return deleteMovieApi.callApi(
            () => movieApi.admin.delete(id),
            {
                successMessage: 'Movie deleted successfully',
                onSuccess: () => {
                    movieByIdApi.invalidateCache(`movie_${id}`);
                    movieBySlugApi.invalidateCache();
                    adminMovieByIdApi.invalidateCache(`admin_movie_${id}`);
                    adminMovieBySlugApi.invalidateCache();
                    moviesApi.invalidateCache();
                    currentlyShowingApi.invalidateCache();
                    upcomingApi.invalidateCache();
                    archivedApi.invalidateCache();
                },
            }
        );
    }, [deleteMovieApi, movieByIdApi, movieBySlugApi, adminMovieByIdApi, adminMovieBySlugApi, moviesApi, currentlyShowingApi, upcomingApi, archivedApi]);

    const clearCache = useCallback(() => {
        moviesApi.invalidateCache();
        movieByIdApi.invalidateCache();
        movieBySlugApi.invalidateCache();
        adminMovieByIdApi.invalidateCache();
        adminMovieBySlugApi.invalidateCache();
        currentlyShowingApi.invalidateCache();
        upcomingApi.invalidateCache();
        archivedApi.invalidateCache();
        searchMoviesApi.invalidateCache();
    }, [moviesApi, movieByIdApi, movieBySlugApi, adminMovieByIdApi, adminMovieBySlugApi, currentlyShowingApi, upcomingApi, archivedApi, searchMoviesApi]);

    const getPosterUrl = useCallback((id: number): string => {
        return movieApi.public.getPosterUrl(id);
    }, []);

    return {
        movies: moviesApi.data?.content || [],
        currentlyShowing: currentlyShowingApi.data?.content || [],
        upcoming: upcomingApi.data?.content || [],
        archived: archivedApi.data?.content || [],
        movie: movieByIdApi.data || movieBySlugApi.data || adminMovieByIdApi.data || adminMovieBySlugApi.data,
        movieBySlug: movieBySlugApi.data || adminMovieBySlugApi.data,
        searchResults: searchMoviesApi.data || [],

        loading: moviesApi.state.isLoading || movieByIdApi.state.isLoading ||
            movieBySlugApi.state.isLoading || adminMovieByIdApi.state.isLoading ||
            adminMovieBySlugApi.state.isLoading || currentlyShowingApi.state.isLoading ||
            upcomingApi.state.isLoading || archivedApi.state.isLoading ||
            searchMoviesApi.state.isLoading || createMovieApi.state.isLoading ||
            updateMovieApi.state.isLoading || deleteMovieApi.state.isLoading,
        error: moviesApi.state.isError || movieByIdApi.state.isError ||
            movieBySlugApi.state.isError || adminMovieByIdApi.state.isError ||
            adminMovieBySlugApi.state.isError || currentlyShowingApi.state.isError ||
            upcomingApi.state.isError || archivedApi.state.isError ||
            searchMoviesApi.state.isError || createMovieApi.state.isError ||
            updateMovieApi.state.isError || deleteMovieApi.state.isError,

        getFilteredMovies,
        getCurrentlyShowing,
        getUpcoming,
        getArchived,
        getById,
        getBySlug,
        searchMoviesForSession,
        create,
        update,
        remove,
        clearCache,
        getPosterUrl,

        resetMovies: moviesApi.reset,
        resetMovie: movieByIdApi.reset,
        resetMovieBySlug: movieBySlugApi.reset,
        resetCurrentlyShowing: currentlyShowingApi.reset,
        resetUpcoming: upcomingApi.reset,
        resetArchived: archivedApi.reset,
        refetchMovies: moviesApi.refetch,
        refetchCurrentlyShowing: currentlyShowingApi.refetch,
        refetchUpcoming: upcomingApi.refetch,
        refetchArchived: archivedApi.refetch,

        pagination: moviesApi.data,
        currentlyShowingPagination: currentlyShowingApi.data,
        upcomingPagination: upcomingApi.data,
        archivedPagination: archivedApi.data,
    };
};