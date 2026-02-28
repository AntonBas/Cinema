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
        const response = await ticketTypesApi.execute(
            () => ticketTypeApi.admin.getAll(params),
            {
                cacheKey: `ticket_types_${JSON.stringify(params)}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [ticketTypesApi]);

    const getById = useCallback(async (id: number) => {
        const response = await ticketTypeByIdApi.execute(
            () => ticketTypeApi.admin.getById(id),
            {
                cacheKey: `ticket_type_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [ticketTypeByIdApi]);

    const getByCode = useCallback(async (code: string) => {
        const response = await ticketTypeByCodeApi.execute(
            () => ticketTypeApi.admin.getByCode(code),
            {
                cacheKey: `ticket_type_code_${code}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [ticketTypeByCodeApi]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        const response = await createApi.execute(
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
        return response?.data || null;
    }, [createApi, ticketTypesApi, dropdownTypesApi, adminDropdownApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest) => {
        const response = await updateApi.execute(
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
        return response?.data || null;
    }, [updateApi, ticketTypesApi, ticketTypeByIdApi, dropdownTypesApi, adminDropdownApi]);

    const remove = useCallback(async (id: number) => {
        await deleteApi.execute(
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
        const response = await toggleActiveApi.execute(
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
        return response?.data || null;
    }, [toggleActiveApi, ticketTypesApi, ticketTypeByIdApi, dropdownTypesApi, adminDropdownApi]);

    const getDropdownTypes = useCallback(async () => {
        const response = await dropdownTypesApi.execute(
            () => ticketTypeApi.public.getDropdownTypes(),
            {
                cacheKey: 'ticket_type_dropdown',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
    }, [dropdownTypesApi]);

    const getAdminDropdownTypes = useCallback(async () => {
        const response = await adminDropdownApi.execute(
            () => ticketTypeApi.admin.getDropdownTypes(),
            {
                cacheKey: 'admin_ticket_type_dropdown',
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response?.data || null;
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

    const loading = ticketTypesApi.loading || dropdownTypesApi.loading ||
        ticketTypeByIdApi.loading || ticketTypeByCodeApi.loading ||
        createApi.loading || updateApi.loading ||
        deleteApi.loading || toggleActiveApi.loading ||
        adminDropdownApi.loading;

    const error = !!(ticketTypesApi.error || dropdownTypesApi.error ||
        ticketTypeByIdApi.error || ticketTypeByCodeApi.error ||
        createApi.error || updateApi.error ||
        deleteApi.error || toggleActiveApi.error ||
        adminDropdownApi.error);

    return {
        ticketTypes: ticketTypesApi.data || [],
        dropdownTypes: dropdownTypesApi.data || [],
        adminDropdownTypes: adminDropdownApi.data || [],
        ticketType: ticketTypeByIdApi.data || ticketTypeByCodeApi.data,

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
        getAdminDropdownTypes,
        clearCache,

        resetTicketTypes: ticketTypesApi.reset,
        resetDropdownTypes: dropdownTypesApi.reset,
        resetTicketType: ticketTypeByIdApi.reset,
    };
};