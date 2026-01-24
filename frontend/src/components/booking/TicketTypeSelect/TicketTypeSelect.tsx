import React from 'react';
import type { TicketPriceInfo } from '@/types/seatAvailability';
import styles from './TicketTypeSelect.module.css';

interface TicketTypeSelectProps {
    seatId: number;
    ticketPrices: TicketPriceInfo[];
    selectedTicketTypeId?: number;
    onSelect: (seatId: number, ticketTypeId: number) => void;
}

export const TicketTypeSelect: React.FC<TicketTypeSelectProps> = ({
    seatId,
    ticketPrices,
    selectedTicketTypeId,
    onSelect
}) => {
    if (!ticketPrices || ticketPrices.length === 0) {
        return <span className={styles.noTypes}>No ticket types available</span>;
    }

    const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const ticketTypeId = parseInt(e.target.value);
        if (!isNaN(ticketTypeId)) {
            onSelect(seatId, ticketTypeId);
        }
    };

    const defaultValue = selectedTicketTypeId || ticketPrices[0].ticketTypeId;

    return (
        <select
            className={styles.select}
            value={defaultValue}
            onChange={handleChange}
            aria-label={`Select ticket type for seat ${seatId}`}
        >
            {ticketPrices.map((ticket) => (
                <option
                    key={ticket.ticketTypeId}
                    value={ticket.ticketTypeId}
                    className={styles.option}
                >
                    {ticket.ticketTypeName} - ${parseFloat(ticket.finalPrice).toFixed(2)}
                </option>
            ))}
        </select>
    );
};