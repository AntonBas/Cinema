import { useState, useCallback, useEffect } from 'react';
import { useAdminTicketTypes } from './useAdminTicketTypes';
import type { TicketTypeResponse } from '@/types/ticketType';

export const useTicketTypeList = (options?: { activeFilter?: boolean; autoFetch?: boolean }) => {
    const { activeFilter, autoFetch = true } = options || {};
    const { getAll, loading, error, clearError } = useAdminTicketTypes();
    const [ticketTypes, setTicketTypes] = useState<TicketTypeResponse[]>([]);

    const fetchTicketTypes = useCallback(async () => {
        try {
            const data = await getAll(activeFilter);
            setTicketTypes(data);
            return data;
        } catch {
            return [];
        }
    }, [getAll, activeFilter]);

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
            const updated = await useAdminTicketTypes().toggleActive(id);
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