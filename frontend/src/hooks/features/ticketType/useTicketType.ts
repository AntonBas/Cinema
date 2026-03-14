import { useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeUserResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest,
    TicketTypeCategory
} from '@/types/ticketType';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface TicketTypeParams {
    active?: boolean;
    category?: TicketTypeCategory;
    search?: string;
    page?: number;
    size?: number;
}

export const useTicketType = () => {
    const ticketTypesApi = useApi<PageResponse<TicketTypeResponse>>();
    const ticketTypeApiInstance = useApi<TicketTypeResponse>();
    const dropdownApi = useApi<TicketTypeUserResponse[]>();
    const mutationApi = useApi<TicketTypeResponse | void>();

    const rawLoading = ticketTypesApi.loading || ticketTypeApiInstance.loading ||
        dropdownApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(ticketTypesApi.error || ticketTypeApiInstance.error ||
        dropdownApi.error || mutationApi.error);

    const getAll = useCallback(async (params?: TicketTypeParams, skipCache: boolean = false) => {
        const cacheKey = `ticket_types_${JSON.stringify(params)}`;
        if (skipCache) {
            ticketTypesApi.invalidateCache(cacheKey);
        }
        const response = await ticketTypesApi.execute(
            () => ticketTypeApi.admin.getAll(params),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [ticketTypesApi]);

    const getById = useCallback(async (id: number, skipCache: boolean = false) => {
        const cacheKey = `ticket_type_${id}`;
        if (skipCache) {
            ticketTypeApiInstance.invalidateCache(cacheKey);
        }
        const response = await ticketTypeApiInstance.execute(
            () => ticketTypeApi.admin.getById(id),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [ticketTypeApiInstance]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.create(request),
            {
                successMessage: 'Ticket type created successfully',
                showErrorNotification: true,
            }
        );
        ticketTypesApi.invalidateCache();
        dropdownApi.invalidateCache('ticket_type_dropdown');
        return response || null;
    }, [mutationApi, ticketTypesApi, dropdownApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.update(id, request),
            {
                successMessage: 'Ticket type updated successfully',
                showErrorNotification: true,
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache('ticket_type_dropdown');
        return response || null;
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const remove = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => ticketTypeApi.admin.delete(id),
            {
                successMessage: 'Ticket type deleted successfully',
                showErrorNotification: true,
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache('ticket_type_dropdown');
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const toggleActive = useCallback(async (id: number) => {
        const response = await mutationApi.execute(
            () => ticketTypeApi.admin.toggleActive(id),
            {
                successMessage: 'Ticket type status updated successfully',
                showErrorNotification: true,
            }
        );
        ticketTypesApi.invalidateCache();
        ticketTypeApiInstance.invalidateCache(`ticket_type_${id}`);
        dropdownApi.invalidateCache('ticket_type_dropdown');
        return response || null;
    }, [mutationApi, ticketTypesApi, ticketTypeApiInstance, dropdownApi]);

    const getDropdownTypes = useCallback(async (skipCache: boolean = false) => {
        const cacheKey = 'ticket_type_dropdown';
        if (skipCache) {
            dropdownApi.invalidateCache(cacheKey);
        }
        const response = await dropdownApi.execute(
            () => ticketTypeApi.public.getDropdownTypes(),
            {
                cacheKey,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
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
        ticketTypes: ticketTypesApi.data,
        dropdownTypes: dropdownApi.data || [],
        ticketType: ticketTypeApiInstance.data,

        pagination: ticketTypesApi.data,
        currentPage: ticketTypesApi.data?.number || 0,
        totalPages: ticketTypesApi.data?.totalPages || 0,
        totalElements: ticketTypesApi.data?.totalElements || 0,
        pageSize: ticketTypesApi.data?.size || 10,
        isEmpty: ticketTypesApi.data?.empty || false,

        loading,
        error,

        getAll,
        getById,
        create,
        update,
        remove,
        toggleActive,
        getDropdownTypes,

        clearCache,
        resetAll,
    };
};