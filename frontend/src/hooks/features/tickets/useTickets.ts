import { useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse } from '@/types/ticket';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useTickets = () => {
    const userTicketsApi = useApi<PageResponse<TicketResponse>>();
    const upcomingTicketsApi = useApi<PageResponse<TicketResponse>>();
    const ticketByIdApi = useApi<TicketResponse>();
    const ticketByCodeApi = useApi<TicketResponse>();

    const getUserTickets = useCallback(async (params?: any) => {
        return userTicketsApi.callApi(
            () => ticketApi.getUserTickets(params),
            {
                cacheKey: `user_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [userTicketsApi]);

    const getUpcomingTickets = useCallback(async (params?: any) => {
        return upcomingTicketsApi.callApi(
            () => ticketApi.getUpcomingTickets(params),
            {
                cacheKey: `upcoming_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [upcomingTicketsApi]);

    const getById = useCallback(async (ticketId: number) => {
        return ticketByIdApi.callApi(
            () => ticketApi.getById(ticketId),
            {
                cacheKey: `ticket_${ticketId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketByIdApi]);

    const getByCode = useCallback(async (ticketCode: string) => {
        return ticketByCodeApi.callApi(
            () => ticketApi.getByCode(ticketCode),
            {
                cacheKey: `ticket_code_${ticketCode}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketByCodeApi]);

    const validateTicket = useCallback(async (ticketCode: string) => {
        const api = useApi<void>();
        return api.callApi(
            () => ticketApi.validate(ticketCode),
            {
                successMessage: 'Ticket validated successfully',
            }
        );
    }, []);

    const getQRCode = useCallback(async (ticketCode: string) => {
        const api = useApi<Blob>();
        return api.callApi(
            async () => {
                const response = await fetch(`/api/tickets/${ticketCode}/qr`, {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    },
                });
                if (!response.ok) throw new Error('Failed to fetch QR code');
                return response.blob();
            },
            {
                cacheKey: `qr_code_${ticketCode}`,
                cacheTime: 24 * 60 * 60 * 1000,
                silent: true,
                showErrorNotification: false,
            }
        );
    }, []);

    const clearCache = useCallback(() => {
        userTicketsApi.invalidateCache();
        upcomingTicketsApi.invalidateCache();
        ticketByIdApi.invalidateCache();
        ticketByCodeApi.invalidateCache();
    }, [userTicketsApi, upcomingTicketsApi, ticketByIdApi, ticketByCodeApi]);

    return {
        userTickets: userTicketsApi.data?.content || [],
        upcomingTickets: upcomingTicketsApi.data?.content || [],
        ticket: ticketByIdApi.data || ticketByCodeApi.data,
        userPagination: userTicketsApi.data,
        upcomingPagination: upcomingTicketsApi.data,

        loading: userTicketsApi.state.isLoading || upcomingTicketsApi.state.isLoading ||
            ticketByIdApi.state.isLoading || ticketByCodeApi.state.isLoading,
        error: userTicketsApi.state.isError || upcomingTicketsApi.state.isError ||
            ticketByIdApi.state.isError || ticketByCodeApi.state.isError,

        getUserTickets,
        getUpcomingTickets,
        getById,
        getByCode,
        validateTicket,
        getQRCode,
        clearCache,

        resetUserTickets: userTicketsApi.reset,
        resetUpcomingTickets: upcomingTicketsApi.reset,
        resetTicket: ticketByIdApi.reset,
        resetTicketByCode: ticketByCodeApi.reset,
        refetchUserTickets: userTicketsApi.refetch,
        refetchUpcomingTickets: upcomingTicketsApi.refetch,
    };
};