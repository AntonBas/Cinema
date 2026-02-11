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
    const actorsApi = useApi<PageResponse<PersonResponse>>();
    const directorsApi = useApi<PageResponse<PersonResponse>>();
    const screenwritersApi = useApi<PageResponse<PersonResponse>>();
    const popularApi = useApi<PageResponse<PersonResponse>>();
    const createApi = useApi<PersonResponse>();
    const quickCreateApi = useApi<PersonResponse>();
    const updateApi = useApi<PersonResponse>();
    const deleteApi = useApi<void>();

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
        return actorsApi.callApi(
            () => personApi.admin.getActors(params),
            {
                cacheKey: `actors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [actorsApi]);

    const getDirectors = useCallback(async (params?: any) => {
        return directorsApi.callApi(
            () => personApi.admin.getDirectors(params),
            {
                cacheKey: `directors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [directorsApi]);

    const getScreenwriters = useCallback(async (params?: any) => {
        return screenwritersApi.callApi(
            () => personApi.admin.getScreenwriters(params),
            {
                cacheKey: `screenwriters_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [screenwritersApi]);

    const getPopular = useCallback(async (params?: any) => {
        return popularApi.callApi(
            () => personApi.admin.getPopular(params),
            {
                cacheKey: `persons_popular_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [popularApi]);

    const create = useCallback(async (request: PersonRequest) => {
        return createApi.callApi(
            () => personApi.admin.create(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                    actorsApi.invalidateCache();
                    directorsApi.invalidateCache();
                    screenwritersApi.invalidateCache();
                    popularApi.invalidateCache();
                },
            }
        );
    }, [createApi, allPersonsApi, actorsApi, directorsApi, screenwritersApi, popularApi]);

    const quickCreate = useCallback(async (request: QuickCreatePersonRequest) => {
        return quickCreateApi.callApi(
            () => personApi.admin.quickCreate(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                    if (request.role === 'ACTOR') actorsApi.invalidateCache();
                    if (request.role === 'DIRECTOR') directorsApi.invalidateCache();
                    if (request.role === 'SCREENWRITER') screenwritersApi.invalidateCache();
                    popularApi.invalidateCache();
                },
            }
        );
    }, [quickCreateApi, allPersonsApi, actorsApi, directorsApi, screenwritersApi, popularApi]);

    const update = useCallback(async (id: number, request: PersonRequest) => {
        return updateApi.callApi(
            () => personApi.admin.update(id, request),
            {
                successMessage: 'Person updated successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                    actorsApi.invalidateCache();
                    directorsApi.invalidateCache();
                    screenwritersApi.invalidateCache();
                    popularApi.invalidateCache();
                },
            }
        );
    }, [updateApi, personByIdApi, allPersonsApi, actorsApi, directorsApi, screenwritersApi, popularApi]);

    const remove = useCallback(async (id: number) => {
        return deleteApi.callApi(
            () => personApi.admin.delete(id),
            {
                successMessage: 'Person deleted successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                    actorsApi.invalidateCache();
                    directorsApi.invalidateCache();
                    screenwritersApi.invalidateCache();
                    popularApi.invalidateCache();
                },
            }
        );
    }, [deleteApi, personByIdApi, allPersonsApi, actorsApi, directorsApi, screenwritersApi, popularApi]);

    const clearCache = useCallback(() => {
        allPersonsApi.invalidateCache();
        personByIdApi.invalidateCache();
        actorsApi.invalidateCache();
        directorsApi.invalidateCache();
        screenwritersApi.invalidateCache();
        popularApi.invalidateCache();
        createApi.invalidateCache();
        quickCreateApi.invalidateCache();
        updateApi.invalidateCache();
        deleteApi.invalidateCache();
    }, [allPersonsApi, personByIdApi, actorsApi, directorsApi, screenwritersApi,
        popularApi, createApi, quickCreateApi, updateApi, deleteApi]);

    return {
        allPersons: allPersonsApi.data?.content || [],
        person: personByIdApi.data,
        actors: actorsApi.data?.content || [],
        directors: directorsApi.data?.content || [],
        screenwriters: screenwritersApi.data?.content || [],
        popular: popularApi.data?.content || [],

        loading: allPersonsApi.state.isLoading || personByIdApi.state.isLoading ||
            actorsApi.state.isLoading || directorsApi.state.isLoading ||
            screenwritersApi.state.isLoading || popularApi.state.isLoading ||
            createApi.state.isLoading || quickCreateApi.state.isLoading ||
            updateApi.state.isLoading || deleteApi.state.isLoading,
        error: allPersonsApi.state.isError || personByIdApi.state.isError ||
            actorsApi.state.isError || directorsApi.state.isError ||
            screenwritersApi.state.isError || popularApi.state.isError ||
            createApi.state.isError || quickCreateApi.state.isError ||
            updateApi.state.isError || deleteApi.state.isError,

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
        resetActors: actorsApi.reset,
        resetDirectors: directorsApi.reset,
        resetScreenwriters: screenwritersApi.reset,
        resetPopular: popularApi.reset,
        refetchAllPersons: allPersonsApi.refetch,
        refetchPerson: personByIdApi.refetch,
        refetchActors: actorsApi.refetch,
        refetchDirectors: directorsApi.refetch,
        refetchScreenwriters: screenwritersApi.refetch,
        refetchPopular: popularApi.refetch,

        pagination: allPersonsApi.data,
        actorsPagination: actorsApi.data,
        directorsPagination: directorsApi.data,
        screenwritersPagination: screenwritersApi.data,
        popularPagination: popularApi.data,
        currentPage: allPersonsApi.data?.number || 0,
        totalPages: allPersonsApi.data?.totalPages || 0,
        totalElements: allPersonsApi.data?.totalElements || 0,
        pageSize: allPersonsApi.data?.size || 10,
        isEmpty: allPersonsApi.data?.empty || false,
    };
};