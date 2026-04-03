import { useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeAdminResponse,
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
    const getAllTicketTypesApi = useApi<PageResponse<TicketTypeAdminResponse>>();
    const getTicketTypeByIdApi = useApi<TicketTypeAdminResponse>();
    const createTicketTypeApi = useApi<TicketTypeAdminResponse>();
    const updateTicketTypeApi = useApi<TicketTypeAdminResponse>();
    const deleteTicketTypeApi = useApi<void>();
    const toggleTicketTypeApi = useApi<TicketTypeAdminResponse>();

    const rawLoading = getAllTicketTypesApi.loading || getTicketTypeByIdApi.loading ||
        createTicketTypeApi.loading || updateTicketTypeApi.loading ||
        deleteTicketTypeApi.loading || toggleTicketTypeApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAllTicketTypesApi.error || getTicketTypeByIdApi.error ||
        createTicketTypeApi.error || updateTicketTypeApi.error ||
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

    const resetAll = useCallback(() => {
        getAllTicketTypesApi.reset();
        getTicketTypeByIdApi.reset();
        createTicketTypeApi.reset();
        updateTicketTypeApi.reset();
        deleteTicketTypeApi.reset();
        toggleTicketTypeApi.reset();
    }, [getAllTicketTypesApi, getTicketTypeByIdApi, createTicketTypeApi, updateTicketTypeApi, deleteTicketTypeApi, toggleTicketTypeApi]);

    return {
        ticketTypes: getAllTicketTypesApi.data,
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
        resetAll,
    };
};