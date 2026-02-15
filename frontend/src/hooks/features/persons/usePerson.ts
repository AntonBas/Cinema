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
        return allPersonsApi.execute(
            () => personApi.admin.getAll(params),
            {
                cacheKey: `persons_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [allPersonsApi]);

    const getById = useCallback(async (id: number) => {
        return personByIdApi.execute(
            () => personApi.public.getById(id),
            {
                cacheKey: `person_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [personByIdApi]);

    const getActors = useCallback(async (params?: any) => {
        return actorsApi.execute(
            () => personApi.admin.getActors(params),
            {
                cacheKey: `actors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [actorsApi]);

    const getDirectors = useCallback(async (params?: any) => {
        return directorsApi.execute(
            () => personApi.admin.getDirectors(params),
            {
                cacheKey: `directors_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [directorsApi]);

    const getScreenwriters = useCallback(async (params?: any) => {
        return screenwritersApi.execute(
            () => personApi.admin.getScreenwriters(params),
            {
                cacheKey: `screenwriters_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [screenwritersApi]);

    const getPopular = useCallback(async (params?: any) => {
        return popularApi.execute(
            () => personApi.admin.getPopular(params),
            {
                cacheKey: `persons_popular_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [popularApi]);

    const create = useCallback(async (request: PersonRequest) => {
        return createApi.execute(
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
        return quickCreateApi.execute(
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
        return updateApi.execute(
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
        return deleteApi.execute(
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

    const loading = allPersonsApi.loading || personByIdApi.loading ||
        actorsApi.loading || directorsApi.loading ||
        screenwritersApi.loading || popularApi.loading ||
        createApi.loading || quickCreateApi.loading ||
        updateApi.loading || deleteApi.loading;

    const error = !!(allPersonsApi.error || personByIdApi.error ||
        actorsApi.error || directorsApi.error ||
        screenwritersApi.error || popularApi.error ||
        createApi.error || quickCreateApi.error ||
        updateApi.error || deleteApi.error);

    return {
        allPersons: allPersonsApi.data?.content || [],
        person: personByIdApi.data,
        actors: actorsApi.data?.content || [],
        directors: directorsApi.data?.content || [],
        screenwriters: screenwritersApi.data?.content || [],
        popular: popularApi.data?.content || [],

        loading,
        error,

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