import { useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest,
    TicketTypeSimpleResponse
} from '@/types/ticketType';
import { useApi } from '@/hooks/common/useApi';

export const useTicketType = () => {
    const ticketTypesApi = useApi<TicketTypeResponse[]>();
    const dropdownTypesApi = useApi<TicketTypeSimpleResponse[]>();
    const ticketTypeByIdApi = useApi<TicketTypeResponse>();
    const ticketTypeByCodeApi = useApi<TicketTypeResponse>();

    const getAll = useCallback(async (params?: any) => {
        return ticketTypesApi.callApi(
            () => ticketTypeApi.admin.getAll(params),
            {
                cacheKey: `ticket_types_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketTypesApi]);

    const getById = useCallback(async (id: number) => {
        return ticketTypeByIdApi.callApi(
            () => ticketTypeApi.admin.getById(id),
            {
                cacheKey: `ticket_type_${id}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, [ticketTypeByIdApi]);

    const getByCode = useCallback(async (code: string) => {
        return ticketTypeByCodeApi.callApi(
            () => ticketTypeApi.admin.getByCode(code),
            {
                cacheKey: `ticket_type_code_${code}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, [ticketTypeByCodeApi]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        const api = useApi<TicketTypeResponse>();
        return api.callApi(
            () => ticketTypeApi.admin.create(request),
            {
                successMessage: 'Ticket type created successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                },
            }
        );
    }, [ticketTypesApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest) => {
        const api = useApi<TicketTypeResponse>();
        return api.callApi(
            () => ticketTypeApi.admin.update(id, request),
            {
                successMessage: 'Ticket type updated successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    ticketTypeByIdApi.invalidateCache(`ticket_type_${id}`);
                },
            }
        );
    }, [ticketTypesApi, ticketTypeByIdApi]);

    const remove = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => ticketTypeApi.admin.delete(id),
            {
                successMessage: 'Ticket type deleted successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                },
            }
        );
    }, [ticketTypesApi]);

    const toggleActive = useCallback(async (id: number) => {
        const api = useApi<TicketTypeResponse>();
        return api.callApi(
            () => ticketTypeApi.admin.toggleActive(id),
            {
                successMessage: 'Ticket type status updated successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    ticketTypeByIdApi.invalidateCache(`ticket_type_${id}`);
                },
            }
        );
    }, [ticketTypesApi, ticketTypeByIdApi]);

    const getDropdownTypes = useCallback(async () => {
        return dropdownTypesApi.callApi(
            () => ticketTypeApi.public.getDropdownTypes(),
            {
                cacheKey: 'ticket_type_dropdown',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [dropdownTypesApi]);

    const getAdminDropdownTypes = useCallback(async () => {
        const api = useApi<TicketTypeSimpleResponse[]>();
        return api.callApi(
            () => ticketTypeApi.admin.getDropdownTypes(),
            {
                cacheKey: 'admin_ticket_type_dropdown',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, []);

    const clearCache = useCallback(() => {
        ticketTypesApi.invalidateCache();
        dropdownTypesApi.invalidateCache();
        ticketTypeByIdApi.invalidateCache();
        ticketTypeByCodeApi.invalidateCache();
    }, [ticketTypesApi, dropdownTypesApi, ticketTypeByIdApi, ticketTypeByCodeApi]);

    return {
        ticketTypes: ticketTypesApi.data || [],
        dropdownTypes: dropdownTypesApi.data || [],
        ticketType: ticketTypeByIdApi.data || ticketTypeByCodeApi.data,

        loading: ticketTypesApi.state.isLoading || dropdownTypesApi.state.isLoading ||
            ticketTypeByIdApi.state.isLoading || ticketTypeByCodeApi.state.isLoading,
        error: ticketTypesApi.state.isError || dropdownTypesApi.state.isError ||
            ticketTypeByIdApi.state.isError || ticketTypeByCodeApi.state.isError,

        getAll,
        getById,
        getByCode,
        create,
        update,
        remove,
        toggleActive,
        getDropdownTypes,
        getAdminDropdownTypes,
        clearCache,

        resetTicketTypes: ticketTypesApi.reset,
        resetDropdownTypes: dropdownTypesApi.reset,
        resetTicketType: ticketTypeByIdApi.reset,
        refetchTicketTypes: ticketTypesApi.refetch,
        refetchDropdownTypes: dropdownTypesApi.refetch,
    };
};