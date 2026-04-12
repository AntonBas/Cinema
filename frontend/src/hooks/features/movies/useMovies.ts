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
    const homeMoviesApi = useApi<MovieCardResponse[]>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const adminMovieApi = useApi<MovieAdminResponse>();
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const mutationApi = useApi<MovieAdminResponse | void>();

    const adminMoviesApiRef = useRef(adminMoviesApi);
    const homeMoviesApiRef = useRef(homeMoviesApi);
    const movieDetailApiRef = useRef(movieDetailApi);
    const adminMovieApiRef = useRef(adminMovieApi);
    const searchMoviesApiRef = useRef(searchMoviesApi);
    const mutationApiRef = useRef(mutationApi);

    adminMoviesApiRef.current = adminMoviesApi;
    homeMoviesApiRef.current = homeMoviesApi;
    movieDetailApiRef.current = movieDetailApi;
    adminMovieApiRef.current = adminMovieApi;
    searchMoviesApiRef.current = searchMoviesApi;
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(
        adminMoviesApi.loading || homeMoviesApi.loading || movieDetailApi.loading ||
        adminMovieApi.loading || searchMoviesApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getAdminMovies = useCallback(async (params?: SearchParams & { query?: string; status?: MovieStatus }) => {
        return adminMoviesApiRef.current.execute(() => movieApi.admin.getMovies(params));
    }, []);

    const getCurrentMoviesForHome = useCallback(async () => {
        return homeMoviesApiRef.current.execute(() => movieApi.public.getCurrentMoviesForHome());
    }, []);

    const getUpcomingMoviesForHome = useCallback(async () => {
        return homeMoviesApiRef.current.execute(() => movieApi.public.getUpcomingMoviesForHome());
    }, []);

    const getLeavingSoonForHome = useCallback(async () => {
        return homeMoviesApiRef.current.execute(() => movieApi.public.getLeavingSoonForHome());
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

    const search = useCallback(async (query?: string) => {
        return searchMoviesApiRef.current.execute(() => movieApi.admin.search(query));
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
        currentMoviesHome: homeMoviesApi.data || [],
        upcomingMoviesHome: homeMoviesApi.data || [],
        leavingSoonHome: homeMoviesApi.data || [],
        movieDetail: movieDetailApi.data,
        adminMovie: adminMovieApi.data,
        searchResults: searchMoviesApi.data || [],
        loading,
        adminError: adminMoviesApi.error,
        homeError: homeMoviesApi.error,
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