import React from 'react';
import { Badge, Button } from '@/components/ui';
import type { TicketResponse } from '@/types/ticket';
import { TicketStatusDisplay } from '@/types/ticket';
import styles from './TicketCard.module.css';

interface TicketCardProps {
    ticket: TicketResponse;
    onViewDetails?: (ticket: TicketResponse) => void;
    onValidate?: (ticketCode: string) => void;
}

export const TicketCard: React.FC<TicketCardProps> = ({
    ticket,
    onViewDetails,
    onValidate
}) => {
    const formatDateTime = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusVariant = (status: string) => {
        switch (status) {
            case 'ACTIVE': return 'success';
            case 'USED': return 'info';
            case 'CANCELLED': return 'error';
            case 'REFUNDED': return 'warning';
            case 'EXPIRED': return 'secondary';
            case 'PENDING': return 'outline';
            default: return 'secondary';
        }
    };

    return (
        <div className={styles.ticketCard}>
            <div className={styles.cardHeader}>
                <div className={styles.headerLeft}>
                    <h3 className={styles.movieTitle}>{ticket.movieTitle}</h3>
                    <Badge variant={getStatusVariant(ticket.status)} size="small">
                        {TicketStatusDisplay[ticket.status]}
                    </Badge>
                </div>
                <div className={styles.ticketCode}>
                    {ticket.ticketCode}
                </div>
            </div>

            <div className={styles.cardContent}>
                <div className={styles.sessionInfo}>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>Session:</span>
                        <span className={styles.infoValue}>{formatDateTime(ticket.sessionTime)}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>Hall:</span>
                        <span className={styles.infoValue}>{ticket.hallName}</span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>Seat:</span>
                        <span className={styles.infoValue}>
                            Row {ticket.row}, Seat {ticket.seatNumber}
                        </span>
                    </div>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>Type:</span>
                        <span className={styles.infoValue}>{ticket.ticketType}</span>
                    </div>
                </div>

                <div className={styles.purchaseInfo}>
                    <div className={styles.infoRow}>
                        <span className={styles.infoLabel}>Purchased:</span>
                        <span className={styles.infoValue}>{formatDateTime(ticket.purchaseTime)}</span>
                    </div>
                    <div className={styles.price}>
                        {ticket.price} UAH
                    </div>
                </div>
            </div>

            <div className={styles.cardActions}>
                {ticket.status === 'ACTIVE' && onValidate && (
                    <Button
                        variant="primary"
                        size="small"
                        onClick={() => onValidate(ticket.ticketCode)}
                    >
                        Show QR Code
                    </Button>
                )}

                {onViewDetails && (
                    <Button
                        variant="secondary"
                        size="small"
                        onClick={() => onViewDetails(ticket)}
                    >
                        Details
                    </Button>
                )}
            </div>
        </div>
    );
};