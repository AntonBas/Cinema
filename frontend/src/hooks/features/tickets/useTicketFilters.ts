import { useState, useCallback, useMemo } from 'react';
import type { TicketResponse, TicketStatus } from '@/types/ticket';

export interface TicketFilters {
    status?: TicketStatus;
    searchQuery?: string;
    dateRange?: {
        from: string;
        to: string;
    };
}

export const useTicketFilters = (initialTickets: TicketResponse[] = []) => {
    const [filters, setFilters] = useState<TicketFilters>({});
    const [sortField, setSortField] = useState<keyof TicketResponse>('purchaseTime');
    const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('desc');

    const filteredTickets = useMemo(() => {
        let result = [...initialTickets];

        if (filters.status) {
            result = result.filter(ticket => ticket.status === filters.status);
        }

        if (filters.searchQuery) {
            const query = filters.searchQuery.toLowerCase();
            result = result.filter(ticket =>
                ticket.movieTitle.toLowerCase().includes(query) ||
                ticket.hallName.toLowerCase().includes(query) ||
                ticket.ticketCode.toLowerCase().includes(query) ||
                ticket.ticketType.toLowerCase().includes(query)
            );
        }

        if (filters.dateRange) {
            result = result.filter(ticket => {
                const purchaseDate = new Date(ticket.purchaseTime);
                const fromDate = new Date(filters.dateRange!.from);
                const toDate = new Date(filters.dateRange!.to);
                return purchaseDate >= fromDate && purchaseDate <= toDate;
            });
        }

        result.sort((a, b) => {
            const aValue = a[sortField];
            const bValue = b[sortField];

            if (typeof aValue === 'string' && typeof bValue === 'string') {
                return sortDirection === 'asc'
                    ? aValue.localeCompare(bValue)
                    : bValue.localeCompare(aValue);
            }

            if (typeof aValue === 'number' && typeof bValue === 'number') {
                return sortDirection === 'asc' ? aValue - bValue : bValue - aValue;
            }

            return 0;
        });

        return result;
    }, [initialTickets, filters, sortField, sortDirection]);

    const updateStatusFilter = useCallback((status?: TicketStatus) => {
        setFilters(prev => ({ ...prev, status }));
    }, []);

    const updateSearchQuery = useCallback((query?: string) => {
        setFilters(prev => ({ ...prev, searchQuery: query }));
    }, []);

    const updateDateRange = useCallback((from?: string, to?: string) => {
        if (from && to) {
            setFilters(prev => ({ ...prev, dateRange: { from, to } }));
        } else {
            setFilters(prev => {
                const { dateRange, ...rest } = prev;
                return rest;
            });
        }
    }, []);

    const clearFilters = useCallback(() => {
        setFilters({});
    }, []);

    const toggleSort = useCallback((field: keyof TicketResponse) => {
        if (sortField === field) {
            setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
        } else {
            setSortField(field);
            setSortDirection('asc');
        }
    }, [sortField]);

    const statistics = useMemo(() => {
        const total = initialTickets.length;
        const byStatus = initialTickets.reduce((acc, ticket) => {
            acc[ticket.status] = (acc[ticket.status] || 0) + 1;
            return acc;
        }, {} as Record<TicketStatus, number>);

        const totalRevenue = initialTickets
            .filter(ticket => ticket.status === 'ACTIVE' || ticket.status === 'USED')
            .reduce((sum, ticket) => sum + parseFloat(ticket.price), 0);

        return {
            total,
            byStatus,
            totalRevenue,
            activeCount: byStatus.ACTIVE || 0,
            usedCount: byStatus.USED || 0,
            cancelledCount: byStatus.CANCELLED || 0
        };
    }, [initialTickets]);

    return {
        filters,
        filteredTickets,
        sortField,
        sortDirection,
        updateStatusFilter,
        updateSearchQuery,
        updateDateRange,
        clearFilters,
        toggleSort,
        statistics,
        hasFilters: Object.keys(filters).length > 0,
        visibleCount: filteredTickets.length
    };
};