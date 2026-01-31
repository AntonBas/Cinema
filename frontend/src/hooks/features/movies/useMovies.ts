import { useState, useCallback } from 'react';
import { movieApi } from '@/api/movieApi';
import type {
    MovieCardResponse,
    MovieDetailResponse,
    MovieSessionSearchResponse,
    MovieStatus,
    MovieCreateRequest,
    MovieUpdateRequest
} from '@/types/movie';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useMovies = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [movie, setMovie] = useState<MovieDetailResponse | null>(null);
    const [sessionMovies, setSessionMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [paginationData, setPaginationData] = useState<PageResponse<MovieCardResponse> | null>(null);

    const getByIdHook = useApi<MovieDetailResponse>();
    const getBySlugHook = useApi<MovieDetailResponse>();
    const getAllPaginatedHook = useApi<PageResponse<MovieCardResponse>>();
    const getCurrentlyShowingHook = useApi<MovieCardResponse[]>();
    const getCurrentlyShowingPaginatedHook = useApi<PageResponse<MovieCardResponse>>();
    const getUpcomingHook = useApi<MovieCardResponse[]>();
    const getUpcomingPaginatedHook = useApi<PageResponse<MovieCardResponse>>();
    const searchHook = useApi<PageResponse<MovieCardResponse>>();
    const searchActiveMoviesHook = useApi<MovieSessionSearchResponse[]>();
    const searchForSessionHook = useApi<MovieSessionSearchResponse[]>();
    const createHook = useApi<MovieDetailResponse>();
    const updateHook = useApi<MovieDetailResponse>();
    const removeHook = useApi<void>();
    const getArchivedMoviesHook = useApi<PageResponse<MovieCardResponse>>();
    const getByStatusHook = useApi<PageResponse<MovieCardResponse>>();
    const searchAdminHook = useApi<PageResponse<MovieCardResponse>>();

    const getById = useCallback(async (id: number): Promise<MovieDetailResponse> => {
        return getByIdHook.callApi(async () => {
            const data = await movieApi.public.getById(id);
            setMovie(data);
            return data;
        });
    }, [getByIdHook]);

    const getBySlug = useCallback(async (slug: string): Promise<MovieDetailResponse> => {
        return getBySlugHook.callApi(async () => {
            const data = await movieApi.public.getBySlug(slug);
            setMovie(data);
            return data;
        });
    }, [getBySlugHook]);

    const getAllPaginated = useCallback(async (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return getAllPaginatedHook.callApi(async () => {
            const response = await movieApi.public.getMoviesPaginated(page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [getAllPaginatedHook]);

    const getCurrentlyShowing = useCallback(async (): Promise<MovieCardResponse[]> => {
        return getCurrentlyShowingHook.callApi(async () => {
            return await movieApi.public.getCurrentlyShowing();
        });
    }, [getCurrentlyShowingHook]);

    const getCurrentlyShowingPaginated = useCallback(async (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return getCurrentlyShowingPaginatedHook.callApi(async () => {
            const response = await movieApi.public.getCurrentlyShowingPaginated(page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [getCurrentlyShowingPaginatedHook]);

    const getUpcoming = useCallback(async (): Promise<MovieCardResponse[]> => {
        return getUpcomingHook.callApi(async () => {
            return await movieApi.public.getUpcoming();
        });
    }, [getUpcomingHook]);

    const getUpcomingPaginated = useCallback(async (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return getUpcomingPaginatedHook.callApi(async () => {
            const response = await movieApi.public.getUpcomingPaginated(page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [getUpcomingPaginatedHook]);

    const search = useCallback(async (params: SearchParams & { status?: MovieStatus } = {}): Promise<PageResponse<MovieCardResponse>> => {
        return searchHook.callApi(async () => {
            const response = await movieApi.public.getFilteredMovies(
                params.search,
                params.status,
                params.page,
                params.size || 20
            );
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [searchHook]);

    const searchActiveMovies = useCallback(async (search?: string): Promise<MovieSessionSearchResponse[]> => {
        return searchActiveMoviesHook.callApi(async () => {
            const response = await movieApi.admin.searchActiveMovies(search);
            setSessionMovies(response);
            return response;
        });
    }, [searchActiveMoviesHook]);

    const searchForSession = useCallback(async (sessionDate: string, search?: string): Promise<MovieSessionSearchResponse[]> => {
        return searchForSessionHook.callApi(async () => {
            const response = await movieApi.admin.searchForSession(sessionDate, search);
            setSessionMovies(response);
            return response;
        });
    }, [searchForSessionHook]);

    const create = useCallback(async (movieData: MovieCreateRequest): Promise<MovieDetailResponse> => {
        return createHook.callApi(async () => {
            const response = await movieApi.admin.create(movieData);
            setMovie(response);
            return response;
        });
    }, [createHook]);

    const update = useCallback(async (id: number, movieData: MovieUpdateRequest): Promise<MovieDetailResponse> => {
        return updateHook.callApi(async () => {
            const response = await movieApi.admin.update(id, movieData);
            setMovie(response);
            setMovies(prev => prev.map(m => m.id === id ? response : m));
            return response;
        });
    }, [updateHook]);

    const remove = useCallback(async (id: number): Promise<void> => {
        return removeHook.callApi(async () => {
            await movieApi.admin.delete(id);
            setMovies(prev => prev.filter(m => m.id !== id));
            if (movie?.id === id) setMovie(null);
        });
    }, [removeHook, movie]);

    const getArchivedMovies = useCallback(async (page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return getArchivedMoviesHook.callApi(async () => {
            const response = await movieApi.admin.getArchivedMovies(page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [getArchivedMoviesHook]);

    const getByStatus = useCallback(async (status: MovieStatus, page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return getByStatusHook.callApi(async () => {
            const response = await movieApi.admin.getByStatus(status, page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [getByStatusHook]);

    const searchAdmin = useCallback(async (search?: string, status?: MovieStatus, page?: number, size: number = 12): Promise<PageResponse<MovieCardResponse>> => {
        return searchAdminHook.callApi(async () => {
            const response = await movieApi.admin.search(search, status, page, size);
            setMovies(response.content);
            setPaginationData(response);
            return response;
        });
    }, [searchAdminHook]);

    const clearMovies = useCallback(() => {
        setMovies([]);
        setSessionMovies([]);
    }, []);

    const refresh = useCallback(() => {
        if (paginationData) {
            search({ page: paginationData.number, size: paginationData.size });
        }
    }, [paginationData, search]);

    const nextPage = useCallback(async (): Promise<PageResponse<MovieCardResponse> | null> => {
        if (!paginationData || paginationData.last) return null;
        return search({ page: paginationData.number + 1, size: paginationData.size });
    }, [paginationData, search]);

    const prevPage = useCallback(async (): Promise<PageResponse<MovieCardResponse> | null> => {
        if (!paginationData || paginationData.first) return null;
        return search({ page: paginationData.number - 1, size: paginationData.size });
    }, [paginationData, search]);

    return {
        movies,
        movie,
        sessionMovies,
        pagination: paginationData,
        loading: getByIdHook.loading || getBySlugHook.loading || getAllPaginatedHook.loading ||
            getCurrentlyShowingHook.loading || getCurrentlyShowingPaginatedHook.loading ||
            getUpcomingHook.loading || getUpcomingPaginatedHook.loading || searchHook.loading ||
            searchActiveMoviesHook.loading || searchForSessionHook.loading || createHook.loading ||
            updateHook.loading || removeHook.loading || getArchivedMoviesHook.loading ||
            getByStatusHook.loading || searchAdminHook.loading,
        getById,
        getBySlug,
        getAllPaginated,
        getCurrentlyShowing,
        getCurrentlyShowingPaginated,
        getUpcoming,
        getUpcomingPaginated,
        search,
        searchActiveMovies,
        searchForSession,
        create,
        update,
        remove,
        getArchivedMovies,
        getByStatus,
        searchAdmin,
        clearMovies,
        refresh,
        nextPage,
        prevPage,
        currentPage: paginationData?.number || 0,
        totalPages: paginationData?.totalPages || 0,
        totalElements: paginationData?.totalElements || 0,
        pageSize: paginationData?.size || 0,
        isEmpty: paginationData?.empty || false,
        isFirstPage: paginationData?.first || true,
        isLastPage: paginationData?.last || true,
    };
};