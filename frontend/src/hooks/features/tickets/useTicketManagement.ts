import { useState, useCallback } from 'react';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import { ApiErrorException } from '@/utils/apiErrorHandler';
import type { PageResponse, SearchParams } from '@/types/pagination';

const BASE_URL = '/api/tickets';

export const useTicketManagement = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const getAuthHeaders = useCallback((): HeadersInit => {
        const token = localStorage.getItem('authToken');
        return {
            'Content-Type': 'application/json',
            ...(token && { 'Authorization': `Bearer ${token}` }),
        };
    }, []);

    const fetchApi = useCallback(async <T>(url: string, options: RequestInit = {}): Promise<T> => {
        const response = await fetch(url, {
            headers: getAuthHeaders(),
            ...options,
        });
        if (!response.ok) {
            const errorData = await response.json();
            throw new ApiErrorException(errorData);
        }
        if (response.status === 204) return undefined as T;
        return response.json();
    }, [getAuthHeaders]);

    const getTicket = useCallback(async (ticketId: number) => {
        setLoading(true);
        setError(null);
        try {
            const ticket = await fetchApi<TicketResponse>(`${BASE_URL}/${ticketId}`);
            return ticket;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch ticket';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const getUserTickets = useCallback(async (status?: TicketStatus, params?: SearchParams) => {
        setLoading(true);
        setError(null);
        try {
            const queryParams = new URLSearchParams();
            if (status) queryParams.append('status', status);
            if (params?.page) queryParams.append('page', params.page.toString());
            if (params?.size) queryParams.append('size', params.size.toString());
            if (params?.sort) queryParams.append('sort', params.sort);
            if (params?.query) queryParams.append('query', params.query);

            const url = `${BASE_URL}${queryParams.toString() ? `?${queryParams}` : ''}`;
            const tickets = await fetchApi<TicketResponse[]>(url);
            return tickets;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch user tickets';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const getPagedUserTickets = useCallback(async (status?: TicketStatus, params?: SearchParams) => {
        setLoading(true);
        setError(null);
        try {
            const queryParams = new URLSearchParams();
            if (status) queryParams.append('status', status);
            if (params?.page) queryParams.append('page', params.page.toString());
            if (params?.size) queryParams.append('size', params.size.toString());
            if (params?.sort) queryParams.append('sort', params.sort);
            if (params?.query) queryParams.append('query', params.query);

            const url = `${BASE_URL}${queryParams.toString() ? `?${queryParams}` : ''}`;
            const pageResponse = await fetchApi<PageResponse<TicketResponse>>(url);
            return pageResponse;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch paged tickets';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const getBookingTickets = useCallback(async (bookingId: number) => {
        setLoading(true);
        setError(null);
        try {
            const tickets = await fetchApi<TicketResponse[]>(`${BASE_URL}/booking/${bookingId}`);
            return tickets;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to fetch booking tickets';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const validateTicket = useCallback(async (ticketCode: string) => {
        setLoading(true);
        setError(null);
        try {
            const result = await fetchApi<string>(`${BASE_URL}/validate/${ticketCode}`, {
                method: 'POST',
            });
            return result;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to validate ticket';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const getTicketQRCode = useCallback(async (ticketCode: string) => {
        try {
            const response = await fetch(`${BASE_URL}/${ticketCode}/qr`, {
                headers: getAuthHeaders(),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new ApiErrorException(errorData);
            }

            return await response.blob();
        } catch (err) {
            throw err;
        }
    }, [getAuthHeaders]);

    const voidTicket = useCallback(async (ticketId: number) => {
        setLoading(true);
        setError(null);
        try {
            await fetchApi<void>(`${BASE_URL}/${ticketId}/void`, {
                method: 'POST',
            });
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to void ticket';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    const checkTicketStatus = useCallback(async (ticketCode: string) => {
        setLoading(true);
        setError(null);
        try {
            const status = await fetchApi<string>(`${BASE_URL}/${ticketCode}/status`);
            return status;
        } catch (err) {
            const message = err instanceof ApiErrorException ? err.message : 'Failed to check ticket status';
            setError(message);
            throw err;
        } finally {
            setLoading(false);
        }
    }, [fetchApi]);

    return {
        loading,
        error,
        getTicket,
        getUserTickets,
        getPagedUserTickets,
        getBookingTickets,
        validateTicket,
        getTicketQRCode,
        voidTicket,
        checkTicketStatus,
        clearError: () => setError(null)
    };
};