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
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useMovies = () => {
    const adminCurrentApi = useApi<PageResponse<MovieCardResponse>>();
    const adminUpcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const adminArchivedApi = useApi<PageResponse<MovieCardResponse>>();
    const publicCurrentApi = useApi<PageResponse<MovieCardResponse>>();
    const publicUpcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const searchApi = useApi<MovieSessionSearchResponse[]>();
    const createApi = useApi<MovieDetailResponse>();
    const updateApi = useApi<MovieDetailResponse>();
    const deleteApi = useApi<void>();

    const rawLoading = adminCurrentApi.loading || adminUpcomingApi.loading ||
        adminArchivedApi.loading || publicCurrentApi.loading || publicUpcomingApi.loading ||
        movieDetailApi.loading || searchApi.loading || createApi.loading ||
        updateApi.loading || deleteApi.loading;

    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });

    const getAdminCurrent = useCallback(async (params?: { title?: string; page?: number; size?: number; sort?: string }) => {
        const result = await adminCurrentApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'CURRENT' }),
            {
                cacheKey: `admin_current_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return result;
    }, [adminCurrentApi]);

    const getAdminUpcoming = useCallback(async (params?: { title?: string; page?: number; size?: number; sort?: string }) => {
        const result = await adminUpcomingApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'UPCOMING' }),
            {
                cacheKey: `admin_upcoming_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return result;
    }, [adminUpcomingApi]);

    const getAdminArchived = useCallback(async (params?: { title?: string; page?: number; size?: number; sort?: string }) => {
        const result = await adminArchivedApi.execute(
            () => movieApi.admin.getMovies({ ...params, status: 'ARCHIVED' }),
            {
                cacheKey: `admin_archived_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return result;
    }, [adminArchivedApi]);

    const getPublicCurrent = useCallback(async (params?: SearchParams) => {
        const result = await publicCurrentApi.execute(
            () => movieApi.public.getCurrentlyShowing(params),
            {
                cacheKey: `public_current_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return result;
    }, [publicCurrentApi]);

    const getPublicUpcoming = useCallback(async (params?: SearchParams) => {
        const result = await publicUpcomingApi.execute(
            () => movieApi.public.getUpcoming(params),
            {
                cacheKey: `public_upcoming_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return result;
    }, [publicUpcomingApi]);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        const cacheKey = isAdmin ? `admin_movie_${id}` : `movie_${id}`;
        const apiCall = isAdmin
            ? () => movieApi.admin.getMovieById(id)
            : () => movieApi.public.getById(id);
        return movieDetailApi.execute(apiCall, {
            cacheKey,
            cacheTime: 10 * 60 * 1000,
            showErrorNotification: false,
        });
    }, [movieDetailApi]);

    const getBySlug = useCallback(async (slug: string) => {
        return movieDetailApi.execute(
            () => movieApi.public.getBySlug(slug),
            {
                cacheKey: `movie_slug_${slug}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [movieDetailApi]);

    const searchMoviesForSession = useCallback(async (search?: string) => {
        return searchApi.execute(
            () => movieApi.admin.searchMoviesForSession(search),
            {
                cacheKey: `session_search_${search}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [searchApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        const result = await createApi.execute(
            () => movieApi.admin.create(request),
            { successMessage: 'Movie created successfully' }
        );
        clearCache();
        return result;
    }, [createApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const result = await updateApi.execute(
            () => movieApi.admin.update(id, request),
            { successMessage: 'Movie updated successfully' }
        );
        clearCache();
        return result;
    }, [updateApi]);

    const remove = useCallback(async (id: number) => {
        const result = await deleteApi.execute(
            () => movieApi.admin.delete(id),
            { successMessage: 'Movie deleted successfully' }
        );
        clearCache();
        return result;
    }, [deleteApi]);

    const clearCache = useCallback(() => {
        adminCurrentApi.invalidateCache();
        adminUpcomingApi.invalidateCache();
        adminArchivedApi.invalidateCache();
        publicCurrentApi.invalidateCache();
        publicUpcomingApi.invalidateCache();
        movieDetailApi.invalidateCache();
        searchApi.invalidateCache();
        createApi.invalidateCache();
        updateApi.invalidateCache();
        deleteApi.invalidateCache();
    }, [adminCurrentApi, adminUpcomingApi, adminArchivedApi, publicCurrentApi, publicUpcomingApi, movieDetailApi, searchApi, createApi, updateApi, deleteApi]);

    const error = !!(adminCurrentApi.error || adminUpcomingApi.error ||
        adminArchivedApi.error || publicCurrentApi.error || publicUpcomingApi.error ||
        movieDetailApi.error || searchApi.error || createApi.error ||
        updateApi.error || deleteApi.error);

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
        getById,
        getBySlug,
        searchMoviesForSession,
        create,
        update,
        remove,
        clearCache,
        reset: adminCurrentApi.reset,
    };
};