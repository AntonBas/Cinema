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
    const getAllPersonsApi = useApi<PageResponse<PersonResponse>>();
    const getPersonByIdApi = useApi<PersonResponse>();
    const createPersonApi = useApi<PersonResponse>();
    const quickCreatePersonApi = useApi<PersonResponse>();
    const updatePersonApi = useApi<PersonResponse>();
    const deletePersonApi = useApi<void>();

    const rawLoading = getAllPersonsApi.loading || getPersonByIdApi.loading ||
        createPersonApi.loading || quickCreatePersonApi.loading ||
        updatePersonApi.loading || deletePersonApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAllPersonsApi.error || getPersonByIdApi.error ||
        createPersonApi.error || quickCreatePersonApi.error ||
        updatePersonApi.error || deletePersonApi.error);

    const getAll = useCallback(async (params?: PersonSearchParams) => {
        const response = await getAllPersonsApi.execute(
            () => personApi.admin.getAll(params),
            { showErrorNotification: false }
        );
        return response;
    }, [getAllPersonsApi]);

    const getById = useCallback(async (id: number, isAdmin: boolean = false) => {
        const apiCall = isAdmin
            ? () => personApi.admin.getById(id)
            : () => personApi.public.getById(id);
        const response = await getPersonByIdApi.execute(apiCall, {
            showErrorNotification: false,
        });
        return response;
    }, [getPersonByIdApi]);

    const create = useCallback(async (request: PersonRequest) => {
        const response = await createPersonApi.execute(
            () => personApi.admin.create(request),
            { successMessage: `Person "${request.name}" created successfully` }
        );
        return response;
    }, [createPersonApi]);

    const quickCreate = useCallback(async (request: QuickCreatePersonRequest) => {
        const response = await quickCreatePersonApi.execute(
            () => personApi.admin.quickCreate(request),
            { successMessage: `Person "${request.name}" created successfully` }
        );
        return response;
    }, [quickCreatePersonApi]);

    const update = useCallback(async (id: number, request: PersonRequest, oldName?: string) => {
        const response = await updatePersonApi.execute(
            () => personApi.admin.update(id, request),
            { successMessage: `Person "${oldName || request.name}" updated successfully` }
        );
        return response;
    }, [updatePersonApi]);

    const remove = useCallback(async (id: number, personName?: string) => {
        await deletePersonApi.execute(
            () => personApi.admin.delete(id),
            { successMessage: `Person "${personName || id}" deleted successfully` }
        );
    }, [deletePersonApi]);

    const resetAll = useCallback(() => {
        getAllPersonsApi.reset();
        getPersonByIdApi.reset();
        createPersonApi.reset();
        quickCreatePersonApi.reset();
        updatePersonApi.reset();
        deletePersonApi.reset();
    }, [getAllPersonsApi, getPersonByIdApi, createPersonApi, quickCreatePersonApi, updatePersonApi, deletePersonApi]);

    return {
        allPersons: getAllPersonsApi.data?.content || [],
        person: getPersonByIdApi.data,
        pagination: getAllPersonsApi.data,
        loading,
        error,
        getAll,
        getById,
        create,
        quickCreate,
        update,
        remove,
        resetAll,
    };
};