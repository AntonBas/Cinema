import { useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest,
    TicketTypeSimpleResponse,
    TicketTypeCategory
} from '@/types/ticketType';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface TicketTypeParams {
    active?: boolean;
    category?: TicketTypeCategory;
    search?: string;
}

export const useTicketType = () => {
    const ticketTypesApi = useApi<TicketTypeResponse[]>();
    const ticketTypeApiInstance = useApi<TicketTypeResponse>();
    const dropdownApi = useApi<TicketTypeSimpleResponse[]>();
    const mutationApi = useApi<TicketTypeResponse | void>();

    const rawLoading = ticketTypesApi.loading || ticketTypeApiInstance.loading ||
        dropdownApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(ticketTypesApi.error || ticketTypeApiInstance.error ||
        dropdownApi.error || mutationApi.error);

    const getAll = useCallback(async (params?: TicketTypeParams) => {
        const response = await ticketTypesApi.execute(
            () => ticketTypeApi.admin.getAll(params),
            {
                cacheKey: `ticket_types_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [ticketTypesApi]);

    const getById = useCallback(async (id: number) => {
        const response = await ticketTypeApiInstance.execute(
            () => ticketTypeApi.admin.getById(id),
            {
                cacheKey: `ticket_type_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [ticketTypeApiInstance]);

    const getByCode = useCallback(async (code: string) => {
        const response = await ticketTypeApiInstance.execute(
            () => ticketTypeApi.admin.getByCode(code),
            {
                cacheKey: `ticket_type_code_${code}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [ticketTypeApiInstance]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.create(request),
            {
                successMessage: 'Ticket type created successfully',
            }
        );
        ticketTypesApi.invalidateCache();
        dropdownApi.invalidateCache();
        return response || null;
    }, [mutationApi, ticketTypesApi, dropdownApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.update(id, request),
            {
                successMessage: 'Ticket type updated successfully',
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache();
        return response || null;
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => ticketTypeApi.admin.delete(id),
            {
                successMessage: 'Ticket type deleted successfully',
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache();
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const toggleActive = useCallback(async (id: number) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.toggleActive(id),
            {
                successMessage: 'Ticket type status updated successfully',
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache();
        return response || null;
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const getDropdownTypes = useCallback(async (isAdmin: boolean = false) => {
        const cacheKey = isAdmin ? 'admin_ticket_type_dropdown' : 'ticket_type_dropdown';
        const apiCall = isAdmin
            ? () => ticketTypeApi.admin.getDropdownTypes()
            : () => ticketTypeApi.public.getDropdownTypes();

        const response = await dropdownApi.execute(apiCall, {
            cacheKey,
            cacheTime: 5 * 60 * 1000,
            showErrorNotification: false,
        });
        return response || null;
    }, [dropdownApi]);

    const clearCache = useCallback(() => {
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache();
        dropdownApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [ticketTypesApi, ticketTypeApiInstance, dropdownApi, mutationApi]);

    const resetAll = useCallback(() => {
        ticketTypesApi.reset();
        ticketTypeApiInstance.reset();
        dropdownApi.reset();
        mutationApi.reset();
    }, [ticketTypesApi, ticketTypeApiInstance, dropdownApi, mutationApi]);

    return {
        ticketTypes: ticketTypesApi.data || [],
        dropdownTypes: dropdownApi.data || [],
        ticketType: ticketTypeApiInstance.data,

        loading,
        error,

        getAll,
        getById,
        getByCode,
        create,
        update,
        remove,
        toggleActive,
        getDropdownTypes,
        clearCache,
        resetAll,
    };
};