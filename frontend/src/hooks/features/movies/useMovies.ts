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
    const adminMoviesApi = useApi<PageResponse<MovieCardResponse>>();
    const homeMoviesApi = useApi<MovieCardResponse[]>();
    const movieDetailApi = useApi<MovieDetailResponse>();
    const adminMovieApi = useApi<MovieAdminResponse>();
    const searchMoviesApi = useApi<MovieSessionSearchResponse[]>();
    const mutationApi = useApi<MovieAdminResponse | void>();

    const loading = useDelayedLoading(
        adminMoviesApi.loading || homeMoviesApi.loading || movieDetailApi.loading ||
        adminMovieApi.loading || searchMoviesApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getAdminMovies = useCallback(async (params?: SearchParams & { query?: string; status?: MovieStatus }) => {
        return adminMoviesApi.execute(() => movieApi.admin.getMovies(params));
    }, [adminMoviesApi]);

    const getCurrentMoviesForHome = useCallback(async () => {
        return homeMoviesApi.execute(() => movieApi.public.getCurrentMoviesForHome());
    }, [homeMoviesApi]);

    const getUpcomingMoviesForHome = useCallback(async () => {
        return homeMoviesApi.execute(() => movieApi.public.getUpcomingMoviesForHome());
    }, [homeMoviesApi]);

    const getLeavingSoonForHome = useCallback(async () => {
        return homeMoviesApi.execute(() => movieApi.public.getLeavingSoonForHome());
    }, [homeMoviesApi]);

    const getCurrentlyShowing = useCallback(async (params?: SearchParams) => {
        return adminMoviesApi.execute(() => movieApi.public.getCurrentlyShowing(params));
    }, [adminMoviesApi]);

    const getUpcoming = useCallback(async (params?: SearchParams) => {
        return adminMoviesApi.execute(() => movieApi.public.getUpcoming(params));
    }, [adminMoviesApi]);

    const getBySlug = useCallback(async (slug: string) => {
        return movieDetailApi.execute(() => movieApi.public.getBySlug(slug));
    }, [movieDetailApi]);

    const getAdminById = useCallback(async (id: number) => {
        return adminMovieApi.execute(() => movieApi.admin.getById(id));
    }, [adminMovieApi]);

    const search = useCallback(async (query?: string) => {
        return searchMoviesApi.execute(() => movieApi.admin.search(query));
    }, [searchMoviesApi]);

    const create = useCallback(async (request: MovieCreateRequest) => {
        return mutationApi.execute(
            () => movieApi.admin.create(request),
            { successMessage: `Movie "${request.title}" created successfully` }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: MovieUpdateRequest) => {
        const movie = adminMoviesApi.data?.content?.find(m => m.id === id);
        const title = movie?.title || request.title;
        return mutationApi.execute(
            () => movieApi.admin.update(id, request),
            { successMessage: `Movie "${title}" updated successfully` }
        );
    }, [mutationApi, adminMoviesApi.data]);

    const remove = useCallback(async (id: number) => {
        const movie = adminMoviesApi.data?.content?.find(m => m.id === id);
        return mutationApi.execute(
            () => movieApi.admin.delete(id),
            { successMessage: `Movie "${movie?.title || id}" deleted successfully` }
        );
    }, [mutationApi, adminMoviesApi.data]);

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