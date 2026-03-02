import { useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type {
    PersonResponse,
    PersonRequest,
    QuickCreatePersonRequest,
    PersonRole
} from '@/types/person';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface PersonSearchParams extends SearchParams {
    name?: string;
    role?: PersonRole;
}

export const usePerson = () => {
    const personsApi = useApi<PageResponse<PersonResponse>>();
    const personApiInstance = useApi<PersonResponse>();
    const mutationApi = useApi<PersonResponse | void>();

    const rawLoading = personsApi.loading || personApiInstance.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(personsApi.error || personApiInstance.error || mutationApi.error);

    const getAll = useCallback(async (params?: PersonSearchParams) => {
        const response = await personsApi.execute(
            () => personApi.admin.getAll(params),
            {
                cacheKey: `persons_all_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response;
    }, []);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        const cacheKey = isAdmin ? `admin_person_${id}` : `person_${id}`;
        const apiCall = isAdmin
            ? () => personApi.admin.getById(id)
            : () => personApi.public.getById(id);
        const response = await personApiInstance.execute(apiCall, {
            cacheKey,
            cacheTime: 10 * 60 * 1000,
            showErrorNotification: false,
        });
        return response;
    }, []);

    const create = useCallback(async (request: PersonRequest) => {
        const response = await mutationApi.execute(
            () => personApi.admin.create(request),
            {
                successMessage: 'Person created successfully',
            }
        );
        personsApi.invalidateCache();
        return response;
    }, []);

    const quickCreate = useCallback(async (request: QuickCreatePersonRequest) => {
        const response = await mutationApi.execute(
            () => personApi.admin.quickCreate(request),
            {
                successMessage: 'Person created successfully',
            }
        );
        personsApi.invalidateCache();
        return response;
    }, []);

    const update = useCallback(async (id: number, request: PersonRequest) => {
        const response = await mutationApi.execute(
            () => personApi.admin.update(id, request),
            {
                successMessage: 'Person updated successfully',
            }
        );
        personApiInstance.invalidateCache(`person_${id}`);
        personApiInstance.invalidateCache(`admin_person_${id}`);
        personsApi.invalidateCache();
        return response;
    }, []);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => personApi.admin.delete(id),
            {
                successMessage: 'Person deleted successfully',
            }
        );
        personApiInstance.invalidateCache(`person_${id}`);
        personApiInstance.invalidateCache(`admin_person_${id}`);
        personsApi.invalidateCache();
    }, []);

    const clearCache = useCallback(() => {
        personsApi.invalidateCache();
        personApiInstance.invalidateCache();
        mutationApi.invalidateCache();
    }, []);

    const resetAll = useCallback(() => {
        personsApi.reset();
        personApiInstance.reset();
        mutationApi.reset();
    }, []);

    return {
        allPersons: personsApi.data?.content || [],
        person: personApiInstance.data,
        pagination: personsApi.data,
        currentPage: personsApi.data?.number || 0,
        totalPages: personsApi.data?.totalPages || 0,
        totalElements: personsApi.data?.totalElements || 0,
        pageSize: personsApi.data?.size || 10,
        isEmpty: personsApi.data?.empty || false,
        loading,
        error,
        getAll,
        getById,
        create,
        quickCreate,
        update,
        remove,
        clearCache,
        resetAll,
    };
};