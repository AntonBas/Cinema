import { useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieDetailResponse,
    MovieAdminResponse,
    MovieSessionSearchResponse,
    MovieCreateRequest,
    MovieUpdateRequest,
    MovieStatus
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useMovies = () => {
    const getAdminMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const getPublicCurrentApi = useApi<PageResponse<MovieCardResponse>>();
    const getPublicUpcomingApi = useApi<PageResponse<MovieCardResponse>>();
    const getNowShowingHomeApi = useApi<MovieCardResponse[]>();
    const getComingSoonHomeApi = useApi<MovieCardResponse[]>();
    const getLeavingSoonHomeApi = useApi<MovieCardResponse[]>();
    const getMovieDetailApi = useApi<MovieDetailResponse>();
    const getAdminMovieApi = useApi<MovieAdminResponse>();
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const createMovieApi = useApi<MovieAdminResponse>();
    const updateMovieApi = useApi<MovieAdminResponse>();
    const deleteMovieApi = useApi<void>();

    const rawLoading = getAdminMoviesApi.loading || getPublicCurrentApi.loading || getPublicUpcomingApi.loading ||
        getNowShowingHomeApi.loading || getComingSoonHomeApi.loading || getLeavingSoonHomeApi.loading ||
        getMovieDetailApi.loading || getAdminMovieApi.loading || searchMoviesApi.loading ||
        createMovieApi.loading || updateMovieApi.loading || deleteMovieApi.loading;

    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });

    const error = !!(getAdminMoviesApi.error || getPublicCurrentApi.error || getPublicUpcomingApi.error ||
        getNowShowingHomeApi.error || getComingSoonHomeApi.error || getLeavingSoonHomeApi.error ||
        getMovieDetailApi.error || getAdminMovieApi.error || searchMoviesApi.error ||
        createMovieApi.error || updateMovieApi.error || deleteMovieApi.error);

    const getAdminMovies = useCallback(async (params?: SearchParams & { title?: string; status?: MovieStatus }) => {
        const response = await getAdminMoviesApi.execute(
            () => movieApi.admin.getMovies(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAdminMoviesApi]);

    const getPublicCurrent = useCallback(async (params?: SearchParams) => {
        const response = await getPublicCurrentApi.execute(
            () => movieApi.public.getCurrentlyShowing(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPublicCurrentApi]);

    const getPublicUpcoming = useCallback(async (params?: SearchParams) => {
        const response = await getPublicUpcomingApi.execute(
            () => movieApi.public.getUpcoming(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPublicUpcomingApi]);

    const getNowShowingForHome = useCallback(async () => {
        const response = await getNowShowingHomeApi.execute(
            () => movieApi.public.getNowShowingForHome(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getNowShowingHomeApi]);

    const getComingSoonForHome = useCallback(async () => {
        const response = await getComingSoonHomeApi.execute(
            () => movieApi.public.getComingSoonForHome(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getComingSoonHomeApi]);

    const getLeavingSoonForHome = useCallback(async () => {
        const response = await getLeavingSoonHomeApi.execute(
            () => movieApi.public.getLeavingSoonForHome(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getLeavingSoonHomeApi]);

    const getBySlug = useCallback(async (slug: string) => {
        const response = await getMovieDetailApi.execute(
            () => movieApi.public.getBySlug(slug),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getMovieDetailApi]);

    const getAdminById = useCallback(async (id: number) => {
        const response = await getAdminMovieApi.execute(
            () => movieApi.admin.getById(id),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAdminMovieApi]);

    const searchForSession = useCallback(async (search?: string) => {
        const response = await searchMoviesApi.execute(
            () => movieApi.admin.searchForSession(search),
            { showErrorNotification: false }
        );
        return response || null;
    }, [searchMoviesApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        const response = await createMovieApi.execute(
            () => movieApi.admin.create(request),
            { successMessage: `Movie "${request.title}" created successfully` }
        );
        return response || null;
    }, [createMovieApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest, oldTitle?: string) => {
        const response = await updateMovieApi.execute(
            () => movieApi.admin.update(id, request),
            { successMessage: `Movie "${oldTitle || request.title}" updated successfully` }
        );
        return response || null;
    }, [updateMovieApi]);

    const remove = useCallback(async (id: number, movieTitle?: string) => {
        await deleteMovieApi.execute(
            () => movieApi.admin.delete(id),
            { successMessage: `Movie "${movieTitle || id}" deleted successfully` }
        );
    }, [deleteMovieApi]);

    const resetAll = useCallback(() => {
        getAdminMoviesApi.reset();
        getPublicCurrentApi.reset();
        getPublicUpcomingApi.reset();
        getNowShowingHomeApi.reset();
        getComingSoonHomeApi.reset();
        getLeavingSoonHomeApi.reset();
        getMovieDetailApi.reset();
        getAdminMovieApi.reset();
        searchMoviesApi.reset();
        createMovieApi.reset();
        updateMovieApi.reset();
        deleteMovieApi.reset();
    }, [getAdminMoviesApi, getPublicCurrentApi, getPublicUpcomingApi, getNowShowingHomeApi, getComingSoonHomeApi, getLeavingSoonHomeApi, getMovieDetailApi, getAdminMovieApi, searchMoviesApi, createMovieApi, updateMovieApi, deleteMovieApi]);

    return {
        adminMovies: getAdminMoviesApi.data?.content || [],
        adminPagination: getAdminMoviesApi.data,
        publicCurrent: getPublicCurrentApi.data?.content || [],
        publicUpcoming: getPublicUpcomingApi.data?.content || [],
        publicCurrentPagination: getPublicCurrentApi.data,
        publicUpcomingPagination: getPublicUpcomingApi.data,
        nowShowingHome: getNowShowingHomeApi.data || [],
        comingSoonHome: getComingSoonHomeApi.data || [],
        leavingSoonHome: getLeavingSoonHomeApi.data || [],
        movie: getMovieDetailApi.data,
        adminMovie: getAdminMovieApi.data,
        searchResults: searchMoviesApi.data || [],
        loading,
        error,
        getAdminMovies,
        getPublicCurrent,
        getPublicUpcoming,
        getNowShowingForHome,
        getComingSoonForHome,
        getLeavingSoonForHome,
        getBySlug,
        getAdminById,
        searchForSession,
        create,
        update,
        remove,
        resetAll,
    };
};