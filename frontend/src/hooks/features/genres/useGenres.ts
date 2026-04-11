import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest, GenreListResponse } from '@/types/genre';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useGenres = () => {
    const genresApi = useApi<PageResponse<GenreListResponse>>();
    const mutationApi = useApi<GenreResponse | void>();

    const loading = useDelayedLoading(
        genresApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getGenreName = useCallback((id: number): string => {
        const genre = genresApi.data?.content?.find(g => g.id === id);
        return genre?.name || String(id);
    }, [genresApi.data]);

    const getAll = useCallback(async (params?: { search?: string }) => {
        return genresApi.execute(() => genreApi.getAll(params));
    }, [genresApi]);

    const create = useCallback(async (request: GenreRequest) => {
        return mutationApi.execute(
            () => genreApi.create(request),
            { successMessage: `Genre "${request.name}" created successfully` }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: GenreRequest) => {
        return mutationApi.execute(
            () => genreApi.update(id, request),
            { successMessage: `Genre "${getGenreName(id)}" updated successfully` }
        );
    }, [mutationApi, getGenreName]);

    const remove = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => genreApi.delete(id),
            { successMessage: `Genre "${getGenreName(id)}" deleted successfully` }
        );
    }, [mutationApi, getGenreName]);

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