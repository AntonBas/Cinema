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
    const validateTicketApi = useApi<void>();
    const qrCodeApi = useApi<Blob>();

    const getUserTickets = useCallback(async (params?: any) => {
        return userTicketsApi.execute(
            () => ticketApi.getUserTickets(params),
            {
                cacheKey: `user_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [userTicketsApi]);

    const getUpcomingTickets = useCallback(async (params?: any) => {
        return upcomingTicketsApi.execute(
            () => ticketApi.getUpcomingTickets(params),
            {
                cacheKey: `upcoming_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [upcomingTicketsApi]);

    const getById = useCallback(async (ticketId: number) => {
        return ticketByIdApi.execute(
            () => ticketApi.getById(ticketId),
            {
                cacheKey: `ticket_${ticketId}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketByIdApi]);

    const getByCode = useCallback(async (ticketCode: string) => {
        return ticketByCodeApi.execute(
            () => ticketApi.getByCode(ticketCode),
            {
                cacheKey: `ticket_code_${ticketCode}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [ticketByCodeApi]);

    const validateTicket = useCallback(async (ticketCode: string) => {
        return validateTicketApi.execute(
            () => ticketApi.validate(ticketCode),
            {
                successMessage: 'Ticket validated successfully',
            }
        );
    }, [validateTicketApi]);

    const getQRCode = useCallback(async (ticketCode: string) => {
        return qrCodeApi.execute(
            () => ticketApi.getQRCode(ticketCode),
            {
                cacheKey: `qr_code_${ticketCode}`,
                cacheTime: 24 * 60 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [qrCodeApi]);

    const clearCache = useCallback(() => {
        userTicketsApi.invalidateCache();
        upcomingTicketsApi.invalidateCache();
        ticketByIdApi.invalidateCache();
        ticketByCodeApi.invalidateCache();
        validateTicketApi.invalidateCache();
        qrCodeApi.invalidateCache();
    }, [userTicketsApi, upcomingTicketsApi, ticketByIdApi, ticketByCodeApi,
        validateTicketApi, qrCodeApi]);

    const loading = userTicketsApi.loading || upcomingTicketsApi.loading ||
        ticketByIdApi.loading || ticketByCodeApi.loading ||
        validateTicketApi.loading || qrCodeApi.loading;

    const error = !!(userTicketsApi.error || upcomingTicketsApi.error ||
        ticketByIdApi.error || ticketByCodeApi.error ||
        validateTicketApi.error || qrCodeApi.error);

    return {
        userTickets: userTicketsApi.data?.content || [],
        upcomingTickets: upcomingTicketsApi.data?.content || [],
        ticket: ticketByIdApi.data || ticketByCodeApi.data,
        qrCode: qrCodeApi.data,
        userPagination: userTicketsApi.data,
        upcomingPagination: upcomingTicketsApi.data,

        loading,
        error,

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
    };
};