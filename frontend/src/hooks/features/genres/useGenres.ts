import { useState, useCallback, useRef } from 'react';
import { genreApi } from '@/api/genreApi';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useGenres = () => {
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [paginationData, setPaginationData] = useState<PageResponse<GenreResponse> | null>(null);

    const apiHookRef = useRef(useApi<PageResponse<GenreResponse>>());
    const apiHook = apiHookRef.current;

    const getAllHook = useApi<GenreResponse[]>();
    const getByIdHook = useApi<GenreResponse>();
    const getForSelectHook = useApi<GenreResponse[]>();
    const createHook = useApi<GenreResponse>();
    const updateHook = useApi<GenreResponse>();
    const removeHook = useApi<void>();
    const getAdminByIdHook = useApi<GenreResponse>();

    const getAll = useCallback(async (): Promise<GenreResponse[]> => {
        return getAllHook.callApi(async () => {
            const data = await genreApi.public.getAll();
            setGenres(data);
            return data;
        });
    }, [getAllHook]);

    const getById = useCallback(async (id: number): Promise<GenreResponse> => {
        return getByIdHook.callApi(async () => {
            return await genreApi.public.getById(id);
        });
    }, [getByIdHook]);

    const getAllPaginated = useCallback(async (params?: SearchParams): Promise<PageResponse<GenreResponse>> => {
        return apiHook.callApi(async () => {
            const response = await genreApi.public.getAllPaginated(params);
            setGenres(response.content);
            setPaginationData(response);
            return response;
        });
    }, [apiHook]);

    const search = useCallback(async (params?: SearchParams): Promise<PageResponse<GenreResponse>> => {
        return apiHook.callApi(async () => {
            const response = await genreApi.public.search(params);
            setGenres(response.content);
            setPaginationData(response);
            return response;
        });
    }, [apiHook]);

    const getForSelect = useCallback(async (): Promise<GenreResponse[]> => {
        return getForSelectHook.callApi(async () => {
            return await genreApi.public.getForSelect();
        });
    }, [getForSelectHook]);

    const create = useCallback(async (genreData: GenreRequest): Promise<GenreResponse> => {
        return createHook.callApi(async () => {
            const genre = await genreApi.admin.create(genreData);
            setGenres(prev => [...prev, genre]);
            return genre;
        });
    }, [createHook]);

    const update = useCallback(async (id: number, genreData: GenreRequest): Promise<GenreResponse> => {
        return updateHook.callApi(async () => {
            const genre = await genreApi.admin.update(id, genreData);
            setGenres(prev => prev.map(g => g.id === id ? genre : g));
            return genre;
        });
    }, [updateHook]);

    const remove = useCallback(async (id: number): Promise<void> => {
        return removeHook.callApi(async () => {
            await genreApi.admin.delete(id);
            setGenres(prev => prev.filter(g => g.id !== id));
        });
    }, [removeHook]);

    const getAdminById = useCallback(async (id: number): Promise<GenreResponse> => {
        return getAdminByIdHook.callApi(async () => {
            return await genreApi.admin.getById(id);
        });
    }, [getAdminByIdHook]);

    const refresh = useCallback(() => {
        if (paginationData) {
            getAllPaginated({ page: paginationData.number, size: paginationData.size });
        } else {
            getAll();
        }
    }, [paginationData, getAllPaginated, getAll]);

    const nextPage = useCallback(async (): Promise<PageResponse<GenreResponse> | null> => {
        if (!paginationData || paginationData.last) return null;
        return getAllPaginated({ page: paginationData.number + 1, size: paginationData.size });
    }, [paginationData, getAllPaginated]);

    const prevPage = useCallback(async (): Promise<PageResponse<GenreResponse> | null> => {
        if (!paginationData || paginationData.first) return null;
        return getAllPaginated({ page: paginationData.number - 1, size: paginationData.size });
    }, [paginationData, getAllPaginated]);

    return {
        genres,
        pagination: paginationData,
        loading: getAllHook.loading || getByIdHook.loading || apiHook.loading ||
            getForSelectHook.loading || createHook.loading ||
            updateHook.loading || removeHook.loading || getAdminByIdHook.loading,
        getAll,
        getById,
        getAllPaginated,
        search,
        getForSelect,
        create,
        update,
        remove,
        getAdminById,
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