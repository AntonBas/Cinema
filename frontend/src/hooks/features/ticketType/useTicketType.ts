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
    const getAllTicketTypesApi = useApi<PageResponse<TicketTypeResponse>>();
    const getTicketTypeByIdApi = useApi<TicketTypeResponse>();
    const getDropdownTypesApi = useApi<TicketTypeUserResponse[]>();
    const createTicketTypeApi = useApi<TicketTypeResponse>();
    const updateTicketTypeApi = useApi<TicketTypeResponse>();
    const deleteTicketTypeApi = useApi<void>();
    const toggleTicketTypeApi = useApi<TicketTypeResponse>();

    const rawLoading = getAllTicketTypesApi.loading || getTicketTypeByIdApi.loading ||
        getDropdownTypesApi.loading || createTicketTypeApi.loading || updateTicketTypeApi.loading ||
        deleteTicketTypeApi.loading || toggleTicketTypeApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAllTicketTypesApi.error || getTicketTypeByIdApi.error ||
        getDropdownTypesApi.error || createTicketTypeApi.error || updateTicketTypeApi.error ||
        deleteTicketTypeApi.error || toggleTicketTypeApi.error);

    const getAll = useCallback(async (params?: TicketTypeParams) => {
        const response = await getAllTicketTypesApi.execute(
            () => ticketTypeApi.admin.getAll(params),
            { showErrorNotification: true }
        );
        return response || null;
    }, [getAllTicketTypesApi]);

    const getById = useCallback(async (id: number) => {
        const response = await getTicketTypeByIdApi.execute(
            () => ticketTypeApi.admin.getById(id),
            { showErrorNotification: true }
        );
        return response || null;
    }, [getTicketTypeByIdApi]);

    const create = useCallback(async (request: TicketTypeCreateRequest) => {
        const response = await createTicketTypeApi.execute(
            () => ticketTypeApi.admin.create(request),
            { successMessage: `Ticket type "${request.displayName}" created successfully` }
        );
        return response || null;
    }, [createTicketTypeApi]);

    const update = useCallback(async (id: number, request: TicketTypeUpdateRequest, oldName?: string) => {
        const response = await updateTicketTypeApi.execute(
            () => ticketTypeApi.admin.update(id, request),
            { successMessage: `Ticket type "${oldName || request.displayName}" updated successfully` }
        );
        return response || null;
    }, [updateTicketTypeApi]);

    const remove = useCallback(async (id: number, ticketTypeName?: string) => {
        await deleteTicketTypeApi.execute(
            () => ticketTypeApi.admin.delete(id),
            { successMessage: `Ticket type "${ticketTypeName || id}" deleted successfully` }
        );
    }, [deleteTicketTypeApi]);

    const toggleActive = useCallback(async (id: number, ticketTypeName?: string) => {
        const response = await toggleTicketTypeApi.execute(
            () => ticketTypeApi.admin.toggleActive(id),
            { successMessage: `Ticket type "${ticketTypeName || id}" status updated successfully` }
        );
        return response || null;
    }, [toggleTicketTypeApi]);

    const getDropdownTypes = useCallback(async () => {
        const response = await getDropdownTypesApi.execute(
            () => ticketTypeApi.public.getDropdownTypes(),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getDropdownTypesApi]);

    const resetAll = useCallback(() => {
        getAllTicketTypesApi.reset();
        getTicketTypeByIdApi.reset();
        getDropdownTypesApi.reset();
        createTicketTypeApi.reset();
        updateTicketTypeApi.reset();
        deleteTicketTypeApi.reset();
        toggleTicketTypeApi.reset();
    }, [getAllTicketTypesApi, getTicketTypeByIdApi, getDropdownTypesApi, createTicketTypeApi, updateTicketTypeApi, deleteTicketTypeApi, toggleTicketTypeApi]);

    return {
        ticketTypes: getAllTicketTypesApi.data,
        dropdownTypes: getDropdownTypesApi.data || [],
        ticketType: getTicketTypeByIdApi.data,
        pagination: getAllTicketTypesApi.data,
        loading,
        error,
        getAll,
        getById,
        create,
        update,
        remove,
        toggleActive,
        getDropdownTypes,
        resetAll,
    };
};