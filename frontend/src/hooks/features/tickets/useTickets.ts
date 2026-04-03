import { useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketFilterRequest } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useTickets = () => {
    const getUserTicketsApi = useApi<PageResponse<TicketResponse>>();
    const getTicketByCodeApi = useApi<TicketResponse>();
    const getQRCodeApi = useApi<Blob>();
    const validateTicketApi = useApi<void>();

    const rawLoading = getUserTicketsApi.loading || getTicketByCodeApi.loading ||
        getQRCodeApi.loading || validateTicketApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getUserTicketsApi.error || getTicketByCodeApi.error ||
        getQRCodeApi.error || validateTicketApi.error);

    const getUserTickets = useCallback(async (params?: SearchParams & TicketFilterRequest) => {
        const response = await getUserTicketsApi.execute(
            () => ticketApi.getUserTickets(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getUserTicketsApi]);

    const getByCode = useCallback(async (ticketCode: string) => {
        const response = await getTicketByCodeApi.execute(
            () => ticketApi.getByCode(ticketCode),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getTicketByCodeApi]);

    const getQRCode = useCallback(async (ticketCode: string) => {
        const response = await getQRCodeApi.execute(
            () => ticketApi.getQRCode(ticketCode),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getQRCodeApi]);

    const validate = useCallback(async (ticketCode: string) => {
        await validateTicketApi.execute(
            () => ticketApi.validate(ticketCode),
            { successMessage: 'Ticket validated successfully' }
        );
    }, [validateTicketApi]);

    const resetAll = useCallback(() => {
        getUserTicketsApi.reset();
        getTicketByCodeApi.reset();
        getQRCodeApi.reset();
        validateTicketApi.reset();
    }, [getUserTicketsApi, getTicketByCodeApi, getQRCodeApi, validateTicketApi]);

    return {
        tickets: getUserTicketsApi.data?.content || [],
        pagination: getUserTicketsApi.data,
        ticket: getTicketByCodeApi.data,
        qrCode: getQRCodeApi.data,
        loading,
        error,
        getUserTickets,
        getByCode,
        getQRCode,
        validate,
        resetAll,
    };
};