import { useCallback } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useGenres = () => {
    const getAllGenresApi = useApi<PageResponse<GenreResponse>>();
    const getGenreByIdApi = useApi<GenreResponse>();
    const createGenreApi = useApi<GenreResponse>();
    const updateGenreApi = useApi<GenreResponse>();
    const deleteGenreApi = useApi<void>();

    const rawLoading = getAllGenresApi.loading || getGenreByIdApi.loading ||
        createGenreApi.loading || updateGenreApi.loading || deleteGenreApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAllGenresApi.error || getGenreByIdApi.error ||
        createGenreApi.error || updateGenreApi.error || deleteGenreApi.error);

    const getAll = useCallback(async (params?: SearchParams) => {
        const response = await getAllGenresApi.execute(
            () => genreApi.getAll(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAllGenresApi]);

    const getById = useCallback(async (id: number) => {
        const response = await getGenreByIdApi.execute(
            () => genreApi.getById(id),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getGenreByIdApi]);

    const create = useCallback(async (request: GenreRequest) => {
        const response = await createGenreApi.execute(
            () => genreApi.create(request),
            { successMessage: `Genre "${request.name}" created successfully` }
        );
        return response || null;
    }, [createGenreApi]);

    const update = useCallback(async (id: number, request: GenreRequest, oldName?: string) => {
        const response = await updateGenreApi.execute(
            () => genreApi.update(id, request),
            { successMessage: `Genre "${oldName || request.name}" updated successfully` }
        );
        return response || null;
    }, [updateGenreApi]);

    const remove = useCallback(async (id: number, genreName?: string) => {
        await deleteGenreApi.execute(
            () => genreApi.delete(id),
            { successMessage: `Genre "${genreName || id}" deleted successfully` }
        );
    }, [deleteGenreApi]);

    const resetAll = useCallback(() => {
        getAllGenresApi.reset();
        getGenreByIdApi.reset();
        createGenreApi.reset();
        updateGenreApi.reset();
        deleteGenreApi.reset();
    }, [getAllGenresApi, getGenreByIdApi, createGenreApi, updateGenreApi, deleteGenreApi]);

    return {
        allGenres: getAllGenresApi.data?.content || [],
        genre: getGenreByIdApi.data,
        pagination: getAllGenresApi.data,
        loading,
        error,
        getAll,
        getById,
        create,
        update,
        remove,
        resetAll,
    };
};