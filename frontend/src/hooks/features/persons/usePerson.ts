import { useCallback } from 'react';
import { personApi } from '@/api/personApi';
import type { PersonResponse, PersonRequest, PersonListResponse, PersonRole } from '@/types/person';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const usePerson = () => {
    const personsApi = useApi<PageResponse<PersonListResponse>>();
    const mutationApi = useApi<PersonResponse | void>();

    const loading = useDelayedLoading(
        personsApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getPersonName = useCallback((id: number): string => {
        const person = personsApi.data?.content?.find(p => p.id === id);
        return person?.name || String(id);
    }, [personsApi.data]);

    const getAll = useCallback(async (params?: { query?: string; role?: PersonRole }) => {
        return personsApi.execute(() => personApi.admin.getAll(params));
    }, [personsApi]);

    const create = useCallback(async (request: PersonRequest) => {
        return mutationApi.execute(
            () => personApi.admin.create(request),
            { successMessage: `Person "${request.name}" created successfully` }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: PersonRequest) => {
        return mutationApi.execute(
            () => personApi.admin.update(id, request),
            { successMessage: `Person "${getPersonName(id)}" updated successfully` }
        );
    }, [mutationApi, getPersonName]);

    const remove = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => personApi.admin.delete(id),
            { successMessage: `Person "${getPersonName(id)}" deleted successfully` }
        );
    }, [mutationApi, getPersonName]);

    return {
        persons: personsApi.data?.content || [],
        pagination: personsApi.data,
        loading,
        personsError: personsApi.error,
        mutationError: mutationApi.error,
        getAll,
        create,
        update,
        remove,
    };
};