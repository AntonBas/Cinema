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

        return moviesApi.execute(apiCall, {
            cacheKey: `movies_filtered_${JSON.stringify(params)}`,
            cacheTime: 5 * 60 * 1000,
            showErrorNotification: false,
        });
    }, [moviesApi]);

    const getCurrentlyShowing = useCallback(async (params?: any) => {
        return currentlyShowingApi.execute(
            () => movieApi.public.getCurrentlyShowing(params),
            {
                cacheKey: `movies_currently_showing_${JSON.stringify(params)}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [currentlyShowingApi]);

    const getUpcoming = useCallback(async (params?: any) => {
        return upcomingApi.execute(
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
        return archivedApi.execute(
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
            return adminMovieByIdApi.execute(
                () => movieApi.admin.getMovieById(id),
                {
                    cacheKey: `admin_movie_${id}`,
                    cacheTime: 5 * 60 * 1000,
                    showErrorNotification: false,
                }
            );
        }
        return movieByIdApi.execute(
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
            return adminMovieBySlugApi.execute(
                () => movieApi.admin.getMovieBySlug(slug),
                {
                    cacheKey: `admin_movie_slug_${slug}`,
                    cacheTime: 5 * 60 * 1000,
                    showErrorNotification: false,
                }
            );
        }
        return movieBySlugApi.execute(
            () => movieApi.public.getBySlug(slug),
            {
                cacheKey: `movie_slug_${slug}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieBySlugApi, adminMovieBySlugApi]);

    const searchMoviesForSession = useCallback(async (search?: string) => {
        return searchMoviesApi.execute(
            () => movieApi.public.searchMoviesForSession(search),
            {
                cacheKey: `movies_search_${search || 'all'}`,
                cacheTime: 2 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [searchMoviesApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        return createMovieApi.execute(
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
        return updateMovieApi.execute(
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
        return deleteMovieApi.execute(
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
        createMovieApi.invalidateCache();
        updateMovieApi.invalidateCache();
        deleteMovieApi.invalidateCache();
    }, [moviesApi, movieByIdApi, movieBySlugApi, adminMovieByIdApi, adminMovieBySlugApi,
        currentlyShowingApi, upcomingApi, archivedApi, searchMoviesApi,
        createMovieApi, updateMovieApi, deleteMovieApi]);

    const getPosterUrl = useCallback((id: number): string => {
        return movieApi.public.getPosterUrl(id);
    }, []);

    const loading = moviesApi.loading || movieByIdApi.loading ||
        movieBySlugApi.loading || adminMovieByIdApi.loading ||
        adminMovieBySlugApi.loading || currentlyShowingApi.loading ||
        upcomingApi.loading || archivedApi.loading ||
        searchMoviesApi.loading || createMovieApi.loading ||
        updateMovieApi.loading || deleteMovieApi.loading;

    const error = !!(moviesApi.error || movieByIdApi.error ||
        movieBySlugApi.error || adminMovieByIdApi.error ||
        adminMovieBySlugApi.error || currentlyShowingApi.error ||
        upcomingApi.error || archivedApi.error ||
        searchMoviesApi.error || createMovieApi.error ||
        updateMovieApi.error || deleteMovieApi.error);

    return {
        movies: moviesApi.data?.content || [],
        currentlyShowing: currentlyShowingApi.data?.content || [],
        upcoming: upcomingApi.data?.content || [],
        archived: archivedApi.data?.content || [],
        movie: movieByIdApi.data || movieBySlugApi.data || adminMovieByIdApi.data || adminMovieBySlugApi.data,
        movieBySlug: movieBySlugApi.data || adminMovieBySlugApi.data,
        searchResults: searchMoviesApi.data || [],

        loading,
        error,

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

        pagination: moviesApi.data,
        currentlyShowingPagination: currentlyShowingApi.data,
        upcomingPagination: upcomingApi.data,
        archivedPagination: archivedApi.data,
    };
};