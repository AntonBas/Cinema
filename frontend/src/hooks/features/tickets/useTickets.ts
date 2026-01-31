import { useState, useCallback, useEffect, useRef, useMemo } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { isApiErrorException } from '@/utils/apiErrorHandler';

export interface TicketFilters {
    search?: string;
    dateRange?: {
        from: string;
        to: string;
    };
}

export const useTickets = () => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [data, setData] = useState<PageResponse<TicketResponse>>({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 12,
        number: 0,
        first: true,
        last: true,
        empty: true
    });
    const [status, setStatus] = useState<TicketStatus | undefined>();
    const [isUpcoming, setIsUpcoming] = useState(false);
    const [filters, setFilters] = useState<TicketFilters>({});
    const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const searchParams = useMemo(() => {
        const params: SearchParams = {
            page: data.number,
            size: data.size
        };

        if (status) {
            params.status = status;
        }

        if (filters.search) {
            params.search = filters.search;
        }

        if (filters.dateRange) {
            params.from = filters.dateRange.from;
            params.to = filters.dateRange.to;
        }

        return params;
    }, [status, filters.search, filters.dateRange, data.number, data.size]);

    const fetchTickets = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = isUpcoming
                ? await ticketApi.getUpcomingTickets(searchParams)
                : await ticketApi.getUserTickets(status, searchParams);

            setData(response);
        } catch (err) {
            const message = isApiErrorException(err) ? err.message : 'Failed to fetch tickets';
            setError(message);
            console.error('Error fetching tickets:', err);
        } finally {
            setLoading(false);
        }
    }, [isUpcoming, status, searchParams]);

    useEffect(() => {
        fetchTickets();
    }, [fetchTickets]);

    const handlePageChange = useCallback((page: number) => {
        setData(prev => ({ ...prev, number: page }));
    }, []);

    const handleSizeChange = useCallback((size: number) => {
        setData(prev => ({ ...prev, size, number: 0 }));
    }, []);

    const handleStatusChange = useCallback((newStatus?: TicketStatus) => {
        setStatus(newStatus);
    }, []);

    const handleUpcomingToggle = useCallback((upcoming: boolean) => {
        setIsUpcoming(upcoming);
    }, []);

    const handleSearch = useCallback((query: string) => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        searchTimeoutRef.current = setTimeout(() => {
            setFilters(prev => ({ ...prev, search: query }));
            setData(prev => ({ ...prev, number: 0 }));
        }, 300);
    }, []);

    const updateDateRange = useCallback((from?: string, to?: string) => {
        if (from && to) {
            setFilters(prev => ({ ...prev, dateRange: { from, to } }));
            setData(prev => ({ ...prev, number: 0 }));
        } else {
            setFilters(prev => {
                const { dateRange, ...rest } = prev;
                return rest;
            });
        }
    }, []);

    const clearFilters = useCallback(() => {
        setFilters({});
        setStatus(undefined);
        setData(prev => ({ ...prev, number: 0 }));
    }, []);

    const clearError = useCallback(() => {
        setError(null);
    }, []);

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

    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    return {
        data,
        loading,
        error,
        status,
        isUpcoming,
        filters,
        handlePageChange,
        handleSizeChange,
        handleStatusChange,
        handleUpcomingToggle,
        handleSearch,
        handleDateRangeChange: updateDateRange,
        refresh: fetchTickets,
        clearError,
        clearFilters,
        getById,
        getByCode,
        validate,
        getQrCode,
        hasFilters: status !== undefined || Object.keys(filters).length > 0
    };
};