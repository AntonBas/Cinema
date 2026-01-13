import { useState, useCallback, useEffect } from 'react';
import { useTicketTypes } from './useTicketTypes';
import type { TicketTypeSimpleResponse } from '@/types/ticketType';

export const useTicketTypeSelection = (age?: number) => {
    const { getSimpleActive, getAvailableForAge, loading, error, clearError } = useTicketTypes();
    const [ticketTypes, setTicketTypes] = useState<TicketTypeSimpleResponse[]>([]);
    const [selectedId, setSelectedId] = useState<number | null>(null);

    const fetchTicketTypes = useCallback(async () => {
        clearError();
        try {
            let data: TicketTypeSimpleResponse[];
            if (age !== undefined) {
                data = await getAvailableForAge(age);
            } else {
                data = await getSimpleActive();
            }
            setTicketTypes(data);

            if (data.length > 0 && !selectedId) {
                setSelectedId(data[0].id);
            }

            return data;
        } catch {
            setTicketTypes([]);
            return [];
        }
    }, [age, getSimpleActive, getAvailableForAge, selectedId, clearError]);

    useEffect(() => {
        fetchTicketTypes();
    }, [fetchTicketTypes]);

    const selectTicketType = useCallback((id: number) => {
        setSelectedId(id);
    }, []);

    const getSelectedTicketType = useCallback((): TicketTypeSimpleResponse | undefined => {
        return ticketTypes.find(ticketType => ticketType.id === selectedId);
    }, [ticketTypes, selectedId]);

    const refresh = useCallback(async () => {
        return await fetchTicketTypes();
    }, [fetchTicketTypes]);

    return {
        ticketTypes,
        selectedId,
        selectedTicketType: getSelectedTicketType(),
        loading,
        error,
        selectTicketType,
        refresh,
        isEmpty: ticketTypes.length === 0
    };
};