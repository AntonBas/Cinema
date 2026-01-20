import { useState, useCallback } from 'react';
import { useTicketManagement } from './useTicketManagement';

export const useTickets = () => {
    const { getUserTickets, loading, error } = useTicketManagement();
    const [tickets, setTickets] = useState<any[]>([]);

    const loadTickets = useCallback(async () => {
        try {
            const data = await getUserTickets();
            setTickets(data);
            return data;
        } catch (err) {
            throw err;
        }
    }, [getUserTickets]);

    return {
        tickets,
        loading,
        error,
        loadTickets,
        refreshTickets: loadTickets
    };
};