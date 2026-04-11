import { useCallback } from 'react';
import { ticketTypeApi } from '@/api/ticketTypeApi';
import type {
    TicketTypeResponse,
    TicketTypeRequest,
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
    const mutationApi = useApi<TicketTypeResponse | void>();

    const loading = useDelayedLoading(
        ticketTypesApi.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getTicketTypeName = useCallback((id: number): string => {
        const ticketType = ticketTypesApi.data?.content?.find(t => t.id === id);
        return ticketType?.displayName || String(id);
    }, [ticketTypesApi.data]);

    const getAll = useCallback(async (params?: TicketTypeParams) => {
        return ticketTypesApi.execute(() => ticketTypeApi.admin.getAll(params));
    }, [ticketTypesApi]);

    const create = useCallback(async (request: TicketTypeRequest) => {
        return mutationApi.execute(
            () => ticketTypeApi.admin.create(request),
            { successMessage: `Ticket type "${request.displayName}" created successfully` }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: TicketTypeRequest) => {
        return mutationApi.execute(
            () => ticketTypeApi.admin.update(id, request),
            { successMessage: `Ticket type "${getTicketTypeName(id)}" updated successfully` }
        );
    }, [mutationApi, getTicketTypeName]);

    const remove = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => ticketTypeApi.admin.delete(id),
            { successMessage: `Ticket type "${getTicketTypeName(id)}" deleted successfully` }
        );
    }, [mutationApi, getTicketTypeName]);

    const toggleActive = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => ticketTypeApi.admin.toggleActive(id),
            { successMessage: `Ticket type "${getTicketTypeName(id)}" status updated successfully` }
        );
    }, [mutationApi, getTicketTypeName]);

    return {
        ticketTypes: ticketTypesApi.data?.content || [],
        pagination: ticketTypesApi.data,
        loading,
        typesError: ticketTypesApi.error,
        mutationError: mutationApi.error,
        getAll,
        create,
        update,
        remove,
        toggleActive,
    };
};