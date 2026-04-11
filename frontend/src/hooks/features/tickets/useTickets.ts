import { useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketFilterRequest } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

export const useTickets = () => {
    const ticketsApi = useApi<PageResponse<TicketResponse>>();
    const ticketApiHook = useApi<TicketResponse>();
    const qrCodeApi = useApi<Blob>();
    const validateApi = useApi<void>();

    const loading = useDelayedLoading(
        ticketsApi.loading || ticketApiHook.loading || qrCodeApi.loading || validateApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getUserTickets = useCallback(async (params?: SearchParams & TicketFilterRequest) => {
        return ticketsApi.execute(() => ticketApi.getUserTickets(params));
    }, [ticketsApi]);

    const getByCode = useCallback(async (ticketCode: string) => {
        return ticketApiHook.execute(() => ticketApi.getByCode(ticketCode));
    }, [ticketApiHook]);

    const getQRCode = useCallback(async (ticketCode: string) => {
        return qrCodeApi.execute(() => ticketApi.getQRCode(ticketCode));
    }, [qrCodeApi]);

    const validate = useCallback(async (ticketCode: string) => {
        return validateApi.execute(
            () => ticketApi.validate(ticketCode),
            { successMessage: 'Ticket validated successfully' }
        );
    }, [validateApi]);

    return {
        tickets: ticketsApi.data?.content || [],
        pagination: ticketsApi.data,
        ticket: ticketApiHook.data,
        qrCode: qrCodeApi.data,
        loading,
        ticketsError: ticketsApi.error,
        ticketError: ticketApiHook.error,
        qrCodeError: qrCodeApi.error,
        validateError: validateApi.error,
        getUserTickets,
        getByCode,
        getQRCode,
        validate,
    };
};