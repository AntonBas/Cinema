import { useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useTickets = () => {
    const upcomingApi = useApi<PageResponse<TicketResponse>>();
    const historyApi = useApi<PageResponse<TicketResponse>>();
    const ticketApiInstance = useApi<TicketResponse>();
    const qrCodeApi = useApi<Blob>();
    const mutationApi = useApi<void>();

    const rawLoading = upcomingApi.loading || historyApi.loading ||
        ticketApiInstance.loading || qrCodeApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(upcomingApi.error || historyApi.error || ticketApiInstance.error ||
        qrCodeApi.error || mutationApi.error);

    const getUpcoming = useCallback(async (params?: SearchParams) => {
        const response = await upcomingApi.execute(
            () => ticketApi.getUpcoming(params),
            {
                cacheKey: `upcoming_tickets_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [upcomingApi]);

    const getHistory = useCallback(async (params?: SearchParams) => {
        const response = await historyApi.execute(
            () => ticketApi.getHistory(params),
            {
                cacheKey: `ticket_history_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [historyApi]);

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
        upcomingApi.invalidateCache();
        historyApi.invalidateCache();
        ticketApiInstance.invalidateCache();
        qrCodeApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [upcomingApi, historyApi, ticketApiInstance, qrCodeApi, mutationApi]);

    const resetAll = useCallback(() => {
        upcomingApi.reset();
        historyApi.reset();
        ticketApiInstance.reset();
        qrCodeApi.reset();
        mutationApi.reset();
    }, [upcomingApi, historyApi, ticketApiInstance, qrCodeApi, mutationApi]);

    return {
        upcomingTickets: upcomingApi.data?.content || [],
        historyTickets: historyApi.data?.content || [],
        ticket: ticketApiInstance.data,
        qrCode: qrCodeApi.data,

        upcomingPagination: upcomingApi.data,
        historyPagination: historyApi.data,

        loading,
        error,

        getUpcoming,
        getHistory,
        getByCode,
        getQRCode,
        validate,
        clearCache,
        resetAll,
    };
};