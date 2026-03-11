import { useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketFilterRequest } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useTickets = () => {
    const ticketsApi = useApi<PageResponse<TicketResponse>>();
    const ticketApiInstance = useApi<TicketResponse>();
    const qrCodeApi = useApi<Blob>();
    const mutationApi = useApi<void>();

    const rawLoading = ticketsApi.loading || ticketApiInstance.loading ||
        qrCodeApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(ticketsApi.error || ticketApiInstance.error ||
        qrCodeApi.error || mutationApi.error);

    const getUserTickets = useCallback(async (params?: SearchParams & TicketFilterRequest) => {
        const response = await ticketsApi.execute(
            () => ticketApi.getUserTickets(params),
            {
                cacheKey: `user_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [ticketsApi]);

    const getByCode = useCallback(async (ticketCode: string) => {
        const response = await ticketApiInstance.execute(
            () => ticketApi.getByCode(ticketCode),
            {
                cacheKey: `ticket_${ticketCode}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [ticketApiInstance]);

    const getQRCode = useCallback(async (ticketCode: string) => {
        const response = await qrCodeApi.execute(
            () => ticketApi.getQRCode(ticketCode),
            {
                cacheKey: `qr_${ticketCode}`,
                cacheTime: 24 * 60 * 60 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [qrCodeApi]);

    const validate = useCallback(async (ticketCode: string) => {
        await mutationApi.execute(
            () => ticketApi.validate(ticketCode),
            {
                successMessage: 'Ticket validated successfully',
            }
        );
        ticketApiInstance.invalidateCache(`ticket_${ticketCode}`);
    }, [mutationApi, ticketApiInstance]);

    const clearCache = useCallback(() => {
        ticketsApi.invalidateCache();
        ticketApiInstance.invalidateCache();
        qrCodeApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [ticketsApi, ticketApiInstance, qrCodeApi, mutationApi]);

    const resetAll = useCallback(() => {
        ticketsApi.reset();
        ticketApiInstance.reset();
        qrCodeApi.reset();
        mutationApi.reset();
    }, [ticketsApi, ticketApiInstance, qrCodeApi, mutationApi]);

    return {
        tickets: ticketsApi.data?.content || [],
        pagination: ticketsApi.data,
        ticket: ticketApiInstance.data,
        qrCode: qrCodeApi.data,

        loading,
        error,

        getUserTickets,
        getByCode,
        getQRCode,
        validate,
        clearCache,
        resetAll,
    };
};