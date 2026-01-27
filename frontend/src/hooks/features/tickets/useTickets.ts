import { useState, useCallback } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export const useTickets = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getById = useCallback(async (ticketId: number): Promise<TicketResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketApi.getById(ticketId);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch ticket with ID: ${ticketId}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getByCode = useCallback(async (ticketCode: string): Promise<TicketResponse> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketApi.getByCode(ticketCode);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : `Failed to fetch ticket with code: ${ticketCode}`;
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUserTickets = useCallback(async (status?: TicketStatus): Promise<TicketResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketApi.getUserTickets(status);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch user tickets';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getUpcomingTickets = useCallback(async (): Promise<TicketResponse[]> => {
        setLoading(true);
        setError(null);
        try {
            return await ticketApi.getUpcomingTickets();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch upcoming tickets';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const validate = useCallback(async (ticketCode: string): Promise<void> => {
        setLoading(true);
        setError(null);
        try {
            await ticketApi.validate(ticketCode);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to validate ticket';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const getQrCode = useCallback(async (ticketCode: string): Promise<Blob> => {
        setLoading(true);
        setError(null);
        try {
            const response = await ticketApi.getQrCode(ticketCode);
            return await response.blob();
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch QR code';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        loading,
        error,
        getById,
        getByCode,
        getUserTickets,
        getUpcomingTickets,
        validate,
        getQrCode,
        clearError
    };
};