import React from 'react';
import { TicketCard } from '@/components/account/TicketCard/TicketCard';
import type { TicketResponse } from '@/types/ticket';
import styles from './TicketsList.module.css';

interface TicketsListProps {
    tickets: TicketResponse[];
}

export const TicketsList: React.FC<TicketsListProps> = ({ tickets }) => {
    if (!tickets || tickets.length === 0) {
        return (
            <div className={styles.ticketsList}>
                <div className={styles.empty}>
                    No tickets available
                </div>
            </div>
        );
    }

    return (
        <div className={styles.ticketsList}>
            <div className={styles.header}>
                <h2 className={styles.title}>My Tickets ({tickets.length})</h2>
            </div>

            <div className={styles.grid}>
                {tickets.map(ticket => (
                    <TicketCard key={ticket.id} ticket={ticket} />
                ))}
            </div>
        </div>
    );
};