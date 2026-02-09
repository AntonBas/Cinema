import { useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type {
    PersonResponse,
    PersonRequest,
    QuickCreatePersonRequest
} from '@/types/person';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const usePerson = () => {
    const allPersonsApi = useApi<PageResponse<PersonResponse>>();
    const personByIdApi = useApi<PersonResponse>();

    const getAll = useCallback(async (params?: any) => {
        return allPersonsApi.callApi(
            () => personApi.admin.getAll(params),
            {
                cacheKey: `persons_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allPersonsApi]);

    const getById = useCallback(async (id: number) => {
        return personByIdApi.callApi(
            () => personApi.public.getById(id),
            {
                cacheKey: `person_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [personByIdApi]);

    const getActors = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<PersonResponse>>();
        return api.callApi(
            () => personApi.admin.getActors(params),
            {
                cacheKey: `actors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const getDirectors = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<PersonResponse>>();
        return api.callApi(
            () => personApi.admin.getDirectors(params),
            {
                cacheKey: `directors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const getScreenwriters = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<PersonResponse>>();
        return api.callApi(
            () => personApi.admin.getScreenwriters(params),
            {
                cacheKey: `screenwriters_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const getPopular = useCallback(async (params?: any) => {
        const api = useApi<PageResponse<PersonResponse>>();
        return api.callApi(
            () => personApi.admin.getPopular(params),
            {
                cacheKey: `persons_popular_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, []);

    const create = useCallback(async (request: PersonRequest) => {
        const api = useApi<PersonResponse>();
        return api.callApi(
            () => personApi.admin.create(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                },
            }
        );
    }, [allPersonsApi]);

    const quickCreate = useCallback(async (request: QuickCreatePersonRequest) => {
        const api = useApi<PersonResponse>();
        return api.callApi(
            () => personApi.admin.quickCreate(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                },
            }
        );
    }, [allPersonsApi]);

    const update = useCallback(async (id: number, request: PersonRequest) => {
        const api = useApi<PersonResponse>();
        return api.callApi(
            () => personApi.admin.update(id, request),
            {
                successMessage: 'Person updated successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                },
            }
        );
    }, [personByIdApi, allPersonsApi]);

    const remove = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => personApi.admin.delete(id),
            {
                successMessage: 'Person deleted successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                },
            }
        );
    }, [personByIdApi, allPersonsApi]);

    const clearCache = useCallback(() => {
        allPersonsApi.invalidateCache();
        personByIdApi.invalidateCache();
    }, [allPersonsApi, personByIdApi]);

    return {
        allPersons: allPersonsApi.data?.content || [],
        person: personByIdApi.data,

        loading: allPersonsApi.state.isLoading || personByIdApi.state.isLoading,
        error: allPersonsApi.state.isError || personByIdApi.state.isError,

        getAll,
        getById,
        getActors,
        getDirectors,
        getScreenwriters,
        getPopular,
        create,
        quickCreate,
        update,
        remove,
        clearCache,

        resetAllPersons: allPersonsApi.reset,
        resetPerson: personByIdApi.reset,
        refetchAllPersons: allPersonsApi.refetch,
        refetchPerson: personByIdApi.refetch,

        pagination: allPersonsApi.data,
        currentPage: allPersonsApi.data?.number || 0,
        totalPages: allPersonsApi.data?.totalPages || 0,
        totalElements: allPersonsApi.data?.totalElements || 0,
        pageSize: allPersonsApi.data?.size || 10,
        isEmpty: allPersonsApi.data?.empty || false,
    };
};