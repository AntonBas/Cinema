import { useState, useCallback, useEffect } from 'react';
import { useAdminTicketTypes } from './useAdminTicketTypes';
import type { TicketTypeResponse, TicketTypeCategory } from '@/types/ticketType';

export const useTicketTypeList = (
    options?: {
        statusFilter?: 'all' | 'active' | 'inactive';
        categoryFilter?: TicketTypeCategory | 'all';
        searchQuery?: string;
        autoFetch?: boolean;
    }
) => {
    const {
        statusFilter = 'all',
        categoryFilter = 'all',
        searchQuery = '',
        autoFetch = true
    } = options || {};

    const { getAll, loading, error, clearError } = useAdminTicketTypes();
    const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);

    const fetchTicketTypes = useCallback(async () => {
        try {
            const params: { active?: boolean; category?: string; search?: string } = {};

            if (statusFilter === 'active') {
                params.active = true;
            } else if (statusFilter === 'inactive') {
                params.active = false;
            }

            if (categoryFilter !== 'all') {
                params.category = categoryFilter;
            }

            if (searchQuery.trim()) {
                params.search = searchQuery;
            }

            const data = await getAll(params);
            setTicketTypes(data);
            return data;
        } catch {
            return [];
        }
    }, [getAll, statusFilter, categoryFilter, searchQuery]);

    useEffect(() => {
        if (autoFetch) {
            fetchTicketTypes();
        }
    }, [fetchTicketTypes, autoFetch]);

    const refresh = useCallback(async () => {
        return await fetchTicketTypes();
    }, [fetchTicketTypes]);

    const addTicketType = useCallback((ticketType: TicketTypeResponse) => {
        setTicketTypes(prev => [...prev, ticketType]);
    }, []);

    const updateTicketType = useCallback((updatedTicketType: TicketTypeResponse) => {
        setTicketTypes(prev =>
            prev.map(ticketType =>
                ticketType.id === updatedTicketType.id ? updatedTicketType : ticketType
            )
        );
    }, []);

    const removeTicketType = useCallback((id: number) => {
        setTicketTypes(prev => prev.filter(ticketType => ticketType.id !== id));
    }, []);

    const toggleTicketTypeActive = useCallback(async (id: number) => {
        try {
            const { toggleActive } = useAdminTicketTypes();
            const updated = await toggleActive(id);
            setTicketTypes(prev =>
                prev.map(ticketType =>
                    ticketType.id === id ? updated : ticketType
                )
            );
            return updated;
        } catch {
            return null;
        }
    }, []);

    return {
        ticketTypes,
        loading,
        error,
        refresh,
        addTicketType,
        updateTicketType,
        removeTicketType,
        toggleTicketTypeActive,
        clearError,
        isEmpty: ticketTypes.length === 0
    };
};