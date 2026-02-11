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
    const createApi = useApi<TicketTypeResponse>();
    const updateApi = useApi<TicketTypeResponse>();
    const deleteApi = useApi<void>();
    const toggleActiveApi = useApi<TicketTypeResponse>();
    const adminDropdownApi = useApi<TicketTypeSimpleResponse[]>();

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
                showErrorNotification: false,
            }
        );
    }, [ticketTypeByIdApi]);

    const getByCode = useCallback(async (code: string) => {
        return ticketTypeByCodeApi.callApi(
            () => ticketTypeApi.admin.getByCode(code),
            {
                cacheKey: `ticket_type_code_${code}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketTypeByCodeApi]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        return createApi.callApi(
            () => ticketTypeApi.admin.create(request),
            {
                successMessage: 'Ticket type created successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    dropdownTypesApi.invalidateCache();
                    adminDropdownApi.invalidateCache();
                },
            }
        );
    }, [createApi, ticketTypesApi, dropdownTypesApi, adminDropdownApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest) => {
        return updateApi.callApi(
            () => ticketTypeApi.admin.update(id, request),
            {
                successMessage: 'Ticket type updated successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    ticketTypeByIdApi.invalidateCache(`ticket_type_${id}`);
                    dropdownTypesApi.invalidateCache();
                    adminDropdownApi.invalidateCache();
                },
            }
        );
    }, [updateApi, ticketTypesApi, ticketTypeByIdApi, dropdownTypesApi, adminDropdownApi]);

    const remove = useCallback(async (id: number) => {
        return deleteApi.callApi(
            () => ticketTypeApi.admin.delete(id),
            {
                successMessage: 'Ticket type deleted successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    dropdownTypesApi.invalidateCache();
                    adminDropdownApi.invalidateCache();
                },
            }
        );
    }, [deleteApi, ticketTypesApi, dropdownTypesApi, adminDropdownApi]);

    const toggleActive = useCallback(async (id: number) => {
        return toggleActiveApi.callApi(
            () => ticketTypeApi.admin.toggleActive(id),
            {
                successMessage: 'Ticket type status updated successfully',
                onSuccess: () => {
                    ticketTypesApi.invalidateCache();
                    ticketTypeByIdApi.invalidateCache(`ticket_type_${id}`);
                    dropdownTypesApi.invalidateCache();
                    adminDropdownApi.invalidateCache();
                },
            }
        );
    }, [toggleActiveApi, ticketTypesApi, ticketTypeByIdApi, dropdownTypesApi, adminDropdownApi]);

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
        return adminDropdownApi.callApi(
            () => ticketTypeApi.admin.getDropdownTypes(),
            {
                cacheKey: 'admin_ticket_type_dropdown',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [adminDropdownApi]);

    const clearCache = useCallback(() => {
        ticketTypesApi.invalidateCache();
        dropdownTypesApi.invalidateCache();
        ticketTypeByIdApi.invalidateCache();
        ticketTypeByCodeApi.invalidateCache();
        createApi.invalidateCache();
        updateApi.invalidateCache();
        deleteApi.invalidateCache();
        toggleActiveApi.invalidateCache();
        adminDropdownApi.invalidateCache();
    }, [ticketTypesApi, dropdownTypesApi, ticketTypeByIdApi, ticketTypeByCodeApi,
        createApi, updateApi, deleteApi, toggleActiveApi, adminDropdownApi]);

    return {
        ticketTypes: ticketTypesApi.data || [],
        dropdownTypes: dropdownTypesApi.data || [],
        adminDropdownTypes: adminDropdownApi.data || [],
        ticketType: ticketTypeByIdApi.data || ticketTypeByCodeApi.data,

        loading: ticketTypesApi.state.isLoading || dropdownTypesApi.state.isLoading ||
            ticketTypeByIdApi.state.isLoading || ticketTypeByCodeApi.state.isLoading ||
            createApi.state.isLoading || updateApi.state.isLoading ||
            deleteApi.state.isLoading || toggleActiveApi.state.isLoading ||
            adminDropdownApi.state.isLoading,
        error: ticketTypesApi.state.isError || dropdownTypesApi.state.isError ||
            ticketTypeByIdApi.state.isError || ticketTypeByCodeApi.state.isError ||
            createApi.state.isError || updateApi.state.isError ||
            deleteApi.state.isError || toggleActiveApi.state.isError ||
            adminDropdownApi.state.isError,

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