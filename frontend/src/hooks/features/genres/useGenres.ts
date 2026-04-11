import { useCallback, useRef } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest, GenreListResponse } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useGenres = () => {
    const genresApi = useApi<PageResponse<GenreListResponse>>();
    const mutationApi = useApi<GenreResponse | void>();

    const genresApiRef = useRef(genresApi);
    const mutationApiRef = useRef(mutationApi);

    genresApiRef.current = genresApi;
    mutationApiRef.current = mutationApi;

    const loading = useDelayedLoading(
        genresApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getGenreName = useCallback((id: number): string => {
        const genre = genresApi.data?.content?.find(g => g.id === id);
        return genre?.name || String(id);
    }, [genresApi.data]);

    const getAll = useCallback(async (params?: { search?: string }) => {
        return genresApiRef.current.execute(() => genreApi.getAll(params));
    }, []);

    const create = useCallback(async (request: GenreRequest) => {
        return mutationApiRef.current.execute(
            () => genreApi.create(request),
            { successMessage: `Genre "${request.name}" created successfully` }
        );
    }, []);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        return mutationApiRef.current.execute(
            () => genreApi.update(id, request),
            { successMessage: `Genre "${getGenreName(id)}" updated successfully` }
        );
    }, [getGenreName]);

    const remove = useCallback(async (id: number) => {
        return mutationApiRef.current.execute(
            () => genreApi.delete(id),
            { successMessage: `Genre "${getGenreName(id)}" deleted successfully` }
        );
    }, [getGenreName]);

    return {
        genres: genresApi.data?.content || [],
        pagination: genresApi.data,
        loading,
        genresError: genresApi.error,
        mutationError: mutationApi.error,
        getAll,
        create,
        update,
        remove,
    };
};