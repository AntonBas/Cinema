import { useState, useCallback, useEffect, useRef, useMemo } from 'react';
import { ticketApi } from '@/api/ticketApi';
import type { TicketResponse, TicketStatus } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export interface TicketFilters {
    search?: string;
    dateRange?: {
        from: string;
        to: string;
    };
}

export const useTickets = () => {
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

    const getUserTicketsHook = useApi<PageResponse<TicketResponse>>();
    const getUpcomingTicketsHook = useApi<PageResponse<TicketResponse>>();
    const getByIdHook = useApi<TicketResponse>();
    const getByCodeHook = useApi<TicketResponse>();
    const validateHook = useApi<void>();
    const getQRCodeHook = useApi<Blob>();

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
        if (isUpcoming) {
            return getUpcomingTicketsHook.callApi(async () => {
                const response = await ticketApi.getUpcomingTickets(searchParams);
                setData(response);
                return response;
            }, { showErrorNotification: false });
        } else {
            return getUserTicketsHook.callApi(async () => {
                const response = await ticketApi.getUserTickets(status, searchParams);
                setData(response);
                return response;
            }, { showErrorNotification: false });
        }
    }, [isUpcoming, getUserTicketsHook, getUpcomingTicketsHook, searchParams, status]);

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

    const getById = useCallback(async (ticketId: number): Promise<TicketResponse> => {
        return getByIdHook.callApi(async () => {
            return await ticketApi.getById(ticketId);
        }, { showErrorNotification: false });
    }, [getByIdHook]);

    const getByCode = useCallback(async (ticketCode: string): Promise<TicketResponse> => {
        return getByCodeHook.callApi(async () => {
            return await ticketApi.getByCode(ticketCode);
        }, { showErrorNotification: false });
    }, [getByCodeHook]);

    const getQrCode = useCallback(async (ticketCode: string): Promise<Blob> => {
        return getQRCodeHook.callApi(async () => {
            return await ticketApi.getQRCode(ticketCode);
        }, { showErrorNotification: false });
    }, [getQRCodeHook]);

    const validate = useCallback(async (ticketCode: string): Promise<void> => {
        return validateHook.callApi(async () => {
            await ticketApi.validate(ticketCode);
        }, { showErrorNotification: false });
    }, [validateHook]);

    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    return {
        data,
        loading: getUserTicketsHook.loading || getUpcomingTicketsHook.loading ||
            getByIdHook.loading || getByCodeHook.loading || validateHook.loading ||
            getQRCodeHook.loading,
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
        clearFilters,
        getById,
        getByCode,
        getQrCode,
        validate,
        hasFilters: status !== undefined || Object.keys(filters).length > 0,
        currentPage: data.number,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        pageSize: data.size,
        isEmpty: data.empty,
        isFirstPage: data.first,
        isLastPage: data.last,
        tickets: data.content,
    };
};