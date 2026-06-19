import { useCallback, useRef } from 'react';
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
    const adminMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const currentMoviesHomeApi = useApi<MovieCardResponse[]>();
    const upcomingMoviesHomeApi = useApi<MovieCardResponse[]>();
    const leavingSoonHomeApi = useApi<MovieCardResponse[]>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const adminMovieApi = useApi<MovieAdminResponse>();
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const mutationApi = useApi<MovieAdminResponse | void>();

    const adminMoviesApiRef = useRef(adminMoviesApi);
    const currentMoviesHomeApiRef = useRef(currentMoviesHomeApi);
    const upcomingMoviesHomeApiRef = useRef(upcomingMoviesHomeApi);
    const leavingSoonHomeApiRef = useRef(leavingSoonHomeApi);
    const movieDetailApiRef = useRef(movieDetailApi);
    const adminMovieApiRef = useRef(adminMovieApi);
    const searchMoviesApiRef = useRef(searchMoviesApi);
    const mutationApiRef = useRef(mutationApi);

    adminMoviesApiRef.current = adminMoviesApi;
    currentMoviesHomeApiRef.current = currentMoviesHomeApi;
    upcomingMoviesHomeApiRef.current = upcomingMoviesHomeApi;
    leavingSoonHomeApiRef.current = leavingSoonHomeApi;
    movieDetailApiRef.current = movieDetailApi;
    adminMovieApiRef.current = adminMovieApi;
    searchMoviesApiRef.current = searchMoviesApi;
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(
        adminMoviesApi.loading || currentMoviesHomeApi.loading || upcomingMoviesHomeApi.loading ||
        leavingSoonHomeApi.loading || movieDetailApi.loading || adminMovieApi.loading ||
        searchMoviesApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getAdminMovies = useCallback(async (params?: SearchParams & { query?: string; status?: MovieStatus }) => {
        return adminMoviesApiRef.current.execute(() => movieApi.admin.getMovies(params));
    }, []);

    const getCurrentMoviesForHome = useCallback(async () => {
        return currentMoviesHomeApiRef.current.execute(() => movieApi.public.getCurrentMoviesForHome());
    }, []);

    const getUpcomingMoviesForHome = useCallback(async () => {
        return upcomingMoviesHomeApiRef.current.execute(() => movieApi.public.getUpcomingMoviesForHome());
    }, []);

    const getLeavingSoonForHome = useCallback(async () => {
        return leavingSoonHomeApiRef.current.execute(() => movieApi.public.getLeavingSoonForHome());
    }, []);

    const getCurrentlyShowing = useCallback(async (params?: SearchParams) => {
        return adminMoviesApiRef.current.execute(() => movieApi.public.getCurrentlyShowing(params));
    }, []);

    const getUpcoming = useCallback(async (params?: SearchParams) => {
        return adminMoviesApiRef.current.execute(() => movieApi.public.getUpcoming(params));
    }, []);

    const getBySlug = useCallback(async (slug: string) => {
        return movieDetailApiRef.current.execute(() => movieApi.public.getBySlug(slug));
    }, []);

    const getAdminById = useCallback(async (id: number) => {
        return adminMovieApiRef.current.execute(() => movieApi.admin.getById(id));
    }, []);

    const search = useCallback(async (query?: string, date?: string) => {
        return searchMoviesApiRef.current.execute(() => movieApi.public.search(query, date));
    }, []);

    const create = useCallback(async (request: MovieCreateRequest) => {
        return mutationApiRef.current.execute(
            () => movieApi.admin.create(request),
            {
                successMessage: `Movie "${request.title}" created successfully`,
                showErrorNotification: true
            }
        );
    }, []);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const movie = adminMoviesApi.data?.content?.find(m => m.id === id);
        const title = movie?.title || request.title;
        return mutationApiRef.current.execute(
            () => movieApi.admin.update(id, request),
            {
                successMessage: `Movie "${title}" updated successfully`,
                showErrorNotification: true
            }
        );
    }, [adminMoviesApi.data]);

    const remove = useCallback(async (id: number) => {
        const movie = adminMoviesApi.data?.content?.find(m => m.id === id);
        return mutationApiRef.current.execute(
            () => movieApi.admin.delete(id),
            {
                successMessage: `Movie "${movie?.title || id}" deleted successfully`,
                showErrorNotification: true
            }
        );
    }, [adminMoviesApi.data]);

    return {
        adminMovies: adminMoviesApi.data?.content || [],
        adminPagination: adminMoviesApi.data,
        currentMoviesHome: currentMoviesHomeApi.data || [],
        upcomingMoviesHome: upcomingMoviesHomeApi.data || [],
        leavingSoonHome: leavingSoonHomeApi.data || [],
        movieDetail: movieDetailApi.data,
        adminMovie: adminMovieApi.data,
        searchResults: searchMoviesApi.data || [],
        loading,
        adminError: adminMoviesApi.error,
        homeError: currentMoviesHomeApi.error || upcomingMoviesHomeApi.error || leavingSoonHomeApi.error,
        detailError: movieDetailApi.error,
        mutationError: mutationApi.error,
        getAdminMovies,
        getCurrentMoviesForHome,
        getUpcomingMoviesForHome,
        getLeavingSoonForHome,
        getCurrentlyShowing,
        getUpcoming,
        getBySlug,
        getAdminById,
        search,
        create,
        update,
        remove,
    };
};