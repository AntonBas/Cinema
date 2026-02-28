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
    const createApi = useApi<PersonResponse>();
    const quickCreateApi = useApi<PersonResponse>();
    const updateApi = useApi<PersonResponse>();
    const deleteApi = useApi<void>();

    const getAll = useCallback(async (params?: any) => {
        const response = await allPersonsApi.execute(
            () => personApi.admin.getAll(params),
            {
                cacheKey: `persons_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [allPersonsApi]);

    const getById = useCallback(async (id: number) => {
        const response = await personByIdApi.execute(
            () => personApi.public.getById(id),
            {
                cacheKey: `person_${id}`,
                cacheTime: 10 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [personByIdApi]);

    const create = useCallback(async (request: PersonRequest) => {
        const response = await createApi.execute(
            () => personApi.admin.create(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                },
            }
        );
        return response?.data || null;
    }, [createApi, allPersonsApi]);

    const quickCreate = useCallback(async (request: QuickCreatePersonRequest) => {
        const response = await quickCreateApi.execute(
            () => personApi.admin.quickCreate(request),
            {
                successMessage: 'Person created successfully',
                onSuccess: () => {
                    allPersonsApi.invalidateCache();
                },
            }
        );
        return response?.data || null;
    }, [quickCreateApi, allPersonsApi]);

    const update = useCallback(async (id: number, request: PersonRequest) => {
        const response = await updateApi.execute(
            () => personApi.admin.update(id, request),
            {
                successMessage: 'Person updated successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                },
            }
        );
        return response?.data || null;
    }, [updateApi, personByIdApi, allPersonsApi]);

    const remove = useCallback(async (id: number) => {
        await deleteApi.execute(
            () => personApi.admin.delete(id),
            {
                successMessage: 'Person deleted successfully',
                onSuccess: () => {
                    personByIdApi.invalidateCache(`person_${id}`);
                    allPersonsApi.invalidateCache();
                },
            }
        );
    }, [deleteApi, personByIdApi, allPersonsApi]);

    const clearCache = useCallback(() => {
        allPersonsApi.invalidateCache();
        personByIdApi.invalidateCache();
        createApi.invalidateCache();
        quickCreateApi.invalidateCache();
        updateApi.invalidateCache();
        deleteApi.invalidateCache();
    }, [allPersonsApi, personByIdApi, createApi, quickCreateApi, updateApi, deleteApi]);

    const loading = allPersonsApi.loading || personByIdApi.loading ||
        createApi.loading || quickCreateApi.loading ||
        updateApi.loading || deleteApi.loading;

    const error = !!(allPersonsApi.error || personByIdApi.error ||
        createApi.error || quickCreateApi.error ||
        updateApi.error || deleteApi.error);

    return {
        allPersons: allPersonsApi.data?.content || [],
        person: personByIdApi.data,
        loading,
        error,
        getAll,
        getById,
        create,
        quickCreate,
        update,
        remove,
        clearCache,
        resetAllPersons: allPersonsApi.reset,
        resetPerson: personByIdApi.reset,
        pagination: allPersonsApi.data,
        currentPage: allPersonsApi.data?.number || 0,
        totalPages: allPersonsApi.data?.totalPages || 0,
        totalElements: allPersonsApi.data?.totalElements || 0,
        pageSize: allPersonsApi.data?.size || 10,
        isEmpty: allPersonsApi.data?.empty || false,
    };
};