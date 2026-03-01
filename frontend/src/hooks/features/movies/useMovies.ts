import { useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieDetailResponse,
    MovieSessionSearchResponse,
    MovieCreateRequest,
    MovieUpdateRequest,
    MovieFilterParams
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useMovies = () => {
    const adminMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const publicMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const searchApi = useApi<MovieSessionSearchResponse[]>();
    const mutationApi = useApi<MovieDetailResponse>();

    const rawLoading = adminMoviesApi.loading || publicMoviesApi.loading ||
        movieDetailApi.loading || searchApi.loading || mutationApi.loading;

    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });

    const error = !!(adminMoviesApi.error || publicMoviesApi.error ||
        movieDetailApi.error || searchApi.error || mutationApi.error);

    const getAdminCurrent = useCallback(async (params?: MovieFilterParams) => {
        const response = await adminMoviesApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'CURRENT' }),
            {
                cacheKey: `admin_current_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminMoviesApi]);

    const getAdminUpcoming = useCallback(async (params?: MovieFilterParams) => {
        const response = await adminMoviesApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'UPCOMING' }),
            {
                cacheKey: `admin_upcoming_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminMoviesApi]);

    const getAdminArchived = useCallback(async (params?: MovieFilterParams) => {
        const response = await adminMoviesApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'ARCHIVED' }),
            {
                cacheKey: `admin_archived_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminMoviesApi]);

    const getPublicCurrent = useCallback(async (params?: SearchParams) => {
        const response = await publicMoviesApi.execute(
            () => movieApi.public.getCurrentlyShowing(params),
            {
                cacheKey: `public_current_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [publicMoviesApi]);

    const getPublicUpcoming = useCallback(async (params?: SearchParams) => {
        const response = await publicMoviesApi.execute(
            () => movieApi.public.getUpcoming(params),
            {
                cacheKey: `public_upcoming_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [publicMoviesApi]);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        const cacheKey = isAdmin ? `admin_movie_${id}` : `movie_${id}`;
        const apiCall = isAdmin
            ? () => movieApi.admin.getMovieById(id)
            : () => movieApi.public.getById(id);
        const response = await movieDetailApi.execute(apiCall, {
            cacheKey,
            cacheTime: 10 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [movieDetailApi]);

    const getBySlug = useCallback(async (slug: string, isAdmin: boolean = false) => {
        const cacheKey = isAdmin ? `admin_movie_slug_${slug}` : `movie_slug_${slug}`;
        const apiCall = isAdmin
            ? () => movieApi.admin.getMovieBySlug(slug)
            : () => movieApi.public.getBySlug(slug);
        const response = await movieDetailApi.execute(apiCall, {
            cacheKey,
            cacheTime: 10 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [movieDetailApi]);

    const searchMoviesForSession = useCallback(async (search?: string) => {
        const response = await searchApi.execute(
            () => movieApi.admin.searchMoviesForSession(search),
            {
                cacheKey: `session_search_${search}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [searchApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        const response = await mutationApi.execute(
            () => movieApi.admin.create(request),
            { successMessage: 'Movie created successfully' }
        );
        clearCache();
        return response || null;
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const response = await mutationApi.execute(
            () => movieApi.admin.update(id, request),
            { successMessage: 'Movie updated successfully' }
        );
        clearCache();
        return response || null;
    }, [mutationApi]);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => movieApi.admin.delete(id),
            { successMessage: 'Movie deleted successfully' }
        );
        clearCache();
    }, [mutationApi]);

    const clearCache = useCallback(() => {
        adminMoviesApi.invalidateCache();
        publicMoviesApi.invalidateCache();
        movieDetailApi.invalidateCache();
        searchApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [adminMoviesApi, publicMoviesApi, movieDetailApi, searchApi, mutationApi]);

    const resetAll = useCallback(() => {
        adminMoviesApi.reset();
        publicMoviesApi.reset();
        movieDetailApi.reset();
        searchApi.reset();
        mutationApi.reset();
    }, [adminMoviesApi, publicMoviesApi, movieDetailApi, searchApi, mutationApi]);

    return {
        adminCurrent: adminMoviesApi.data?.content || [],
        adminUpcoming: adminMoviesApi.data?.content || [],
        adminArchived: adminMoviesApi.data?.content || [],
        adminCurrentPagination: adminMoviesApi.data,
        adminUpcomingPagination: adminMoviesApi.data,
        adminArchivedPagination: adminMoviesApi.data,

        publicCurrent: publicMoviesApi.data?.content || [],
        publicUpcoming: publicMoviesApi.data?.content || [],
        publicCurrentPagination: publicMoviesApi.data,
        publicUpcomingPagination: publicMoviesApi.data,

        movie: movieDetailApi.data,
        searchResults: searchApi.data || [],

        loading,
        error,

        getAdminCurrent,
        getAdminUpcoming,
        getAdminArchived,
        getPublicCurrent,
        getPublicUpcoming,
        getById,
        getBySlug,
        searchMoviesForSession,
        create,
        update,
        remove,

        clearCache,
        resetAll,
    };
};