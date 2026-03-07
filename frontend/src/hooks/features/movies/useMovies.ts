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
    const adminCurrentApi = useApi<PageResponse<MovieCardResponse>>();
    const adminUpcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const adminArchivedApi = useApi<PageResponse<MovieCardResponse>>();
    const publicCurrentApi = useApi<PageResponse<MovieCardResponse>>();
    const publicUpcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const searchApi = useApi<MovieSessionSearchResponse[]>();
    const mutationApi = useApi<MovieDetailResponse>();

    const rawLoading = adminCurrentApi.loading || adminUpcomingApi.loading ||
        adminArchivedApi.loading || publicCurrentApi.loading || publicUpcomingApi.loading ||
        movieDetailApi.loading || searchApi.loading || mutationApi.loading;

    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });

    const error = !!(adminCurrentApi.error || adminUpcomingApi.error || adminArchivedApi.error ||
        publicCurrentApi.error || publicUpcomingApi.error || movieDetailApi.error ||
        searchApi.error || mutationApi.error);

    const getAdminCurrent = useCallback(async (params?: MovieFilterParams, skipCache: boolean = false) => {
        const cacheKey = `admin_current_${JSON.stringify(params)}`;
        if (skipCache) {
            adminCurrentApi.invalidateCache(cacheKey);
        }
        const response = await adminCurrentApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'CURRENT' }),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminCurrentApi]);

    const getAdminUpcoming = useCallback(async (params?: MovieFilterParams, skipCache: boolean = false) => {
        const cacheKey = `admin_upcoming_${JSON.stringify(params)}`;
        if (skipCache) {
            adminUpcomingApi.invalidateCache(cacheKey);
        }
        const response = await adminUpcomingApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'UPCOMING' }),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminUpcomingApi]);

    const getAdminArchived = useCallback(async (params?: MovieFilterParams, skipCache: boolean = false) => {
        const cacheKey = `admin_archived_${JSON.stringify(params)}`;
        if (skipCache) {
            adminArchivedApi.invalidateCache(cacheKey);
        }
        const response = await adminArchivedApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'ARCHIVED' }),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminArchivedApi]);

    const getPublicCurrent = useCallback(async (params?: SearchParams, skipCache: boolean = false) => {
        const cacheKey = `public_current_${JSON.stringify(params)}`;
        if (skipCache) {
            publicCurrentApi.invalidateCache(cacheKey);
        }
        const response = await publicCurrentApi.execute(
            () => movieApi.public.getCurrentlyShowing(params),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [publicCurrentApi]);

    const getPublicUpcoming = useCallback(async (params?: SearchParams, skipCache: boolean = false) => {
        const cacheKey = `public_upcoming_${JSON.stringify(params)}`;
        if (skipCache) {
            publicUpcomingApi.invalidateCache(cacheKey);
        }
        const response = await publicUpcomingApi.execute(
            () => movieApi.public.getUpcoming(params),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [publicUpcomingApi]);

    const getBySlug = useCallback(async (slug: string, isAdmin: boolean = false, skipCache: boolean = false) => {
        const cacheKey = isAdmin ? `admin_movie_slug_${slug}` : `movie_slug_${slug}`;
        if (skipCache) {
            movieDetailApi.invalidateCache(cacheKey);
        }
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

    const getAdminById = useCallback(async (id: number, skipCache: boolean = false) => {
        const cacheKey = `admin_movie_${id}`;
        if (skipCache) {
            movieDetailApi.invalidateCache(cacheKey);
        }
        const response = await movieDetailApi.execute(
            () => movieApi.admin.getMovieById(id),
            {
                cacheKey,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [movieDetailApi]);

    const searchMoviesForSession = useCallback(async (search?: string, skipCache: boolean = false) => {
        const cacheKey = `session_search_${search}`;
        if (skipCache) {
            searchApi.invalidateCache(cacheKey);
        }
        const response = await searchApi.execute(
            () => movieApi.admin.searchMoviesForSession(search),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [searchApi]);

    const clearCache = useCallback(() => {
        adminCurrentApi.invalidateCache();
        adminUpcomingApi.invalidateCache();
        adminArchivedApi.invalidateCache();
        publicCurrentApi.invalidateCache();
        publicUpcomingApi.invalidateCache();
        movieDetailApi.invalidateCache();
        searchApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [
        adminCurrentApi, adminUpcomingApi, adminArchivedApi,
        publicCurrentApi, publicUpcomingApi,
        movieDetailApi, searchApi, mutationApi
    ]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        const response = await mutationApi.execute(
            () => movieApi.admin.create(request),
            { successMessage: 'Movie created successfully' }
        );
        clearCache();
        return response || null;
    }, [mutationApi, clearCache]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const response = await mutationApi.execute(
            () => movieApi.admin.update(id, request),
            { successMessage: 'Movie updated successfully' }
        );

        adminCurrentApi.invalidateCache();
        adminUpcomingApi.invalidateCache();
        adminArchivedApi.invalidateCache();
        publicCurrentApi.invalidateCache();
        publicUpcomingApi.invalidateCache();
        movieDetailApi.invalidateCache(`admin_movie_${id}`);
        movieDetailApi.invalidateCache(`movie_slug_*`);
        searchApi.invalidateCache();
        mutationApi.invalidateCache();

        return response || null;
    }, [mutationApi, adminCurrentApi, adminUpcomingApi, adminArchivedApi, publicCurrentApi, publicUpcomingApi, movieDetailApi, searchApi]);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => movieApi.admin.delete(id),
            { successMessage: 'Movie deleted successfully' }
        );

        adminCurrentApi.invalidateCache();
        adminUpcomingApi.invalidateCache();
        adminArchivedApi.invalidateCache();
        publicCurrentApi.invalidateCache();
        publicUpcomingApi.invalidateCache();
        movieDetailApi.invalidateCache(`admin_movie_${id}`);
        movieDetailApi.invalidateCache(`movie_slug_*`);
        searchApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [mutationApi, adminCurrentApi, adminUpcomingApi, adminArchivedApi, publicCurrentApi, publicUpcomingApi, movieDetailApi, searchApi]);

    const resetAll = useCallback(() => {
        adminCurrentApi.reset();
        adminUpcomingApi.reset();
        adminArchivedApi.reset();
        publicCurrentApi.reset();
        publicUpcomingApi.reset();
        movieDetailApi.reset();
        searchApi.reset();
        mutationApi.reset();
    }, [
        adminCurrentApi, adminUpcomingApi, adminArchivedApi,
        publicCurrentApi, publicUpcomingApi,
        movieDetailApi, searchApi, mutationApi
    ]);

    return {
        adminCurrent: adminCurrentApi.data?.content || [],
        adminUpcoming: adminUpcomingApi.data?.content || [],
        adminArchived: adminArchivedApi.data?.content || [],
        adminCurrentPagination: adminCurrentApi.data,
        adminUpcomingPagination: adminUpcomingApi.data,
        adminArchivedPagination: adminArchivedApi.data,

        publicCurrent: publicCurrentApi.data?.content || [],
        publicUpcoming: publicUpcomingApi.data?.content || [],
        publicCurrentPagination: publicCurrentApi.data,
        publicUpcomingPagination: publicUpcomingApi.data,

        movie: movieDetailApi.data,
        searchResults: searchApi.data || [],

        loading,
        error,

        getAdminCurrent,
        getAdminUpcoming,
        getAdminArchived,
        getPublicCurrent,
        getPublicUpcoming,
        getBySlug,
        getAdminById,
        searchMoviesForSession,
        create,
        update,
        remove,

        clearCache,
        resetAll,
    };
};