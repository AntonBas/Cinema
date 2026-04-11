import React from 'react';
import { Button } from '@/components/ui/Button/Button';
import type { TicketResponse } from '@/types/ticket';
import { TicketStatusDisplay } from '@/types/ticket';
import styles from './TicketCard.module.css';

interface TicketCardProps {
    ticket: TicketResponse;
    viewMode: 'grid' | 'list';
    onShowQR: (ticketCode: string) => void;
    onViewDetails?: (ticket: TicketResponse) => void;
    onRequestRefund?: (ticket: TicketResponse) => void;
}

const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
    });
};

const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
    });
};

const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
        ACTIVE: '#48bb78',
        USED: '#3b82f6',
        REFUNDED: '#ed8936',
    };
    return colors[status] || '#a0a8c0';
};

const getSeatInfo = (ticket: TicketResponse) => {
    if (ticket.row == null || ticket.seatNumber == null) {
        return 'Seat not assigned';
    }
    return `Row ${ticket.row}, Seat ${ticket.seatNumber}`;
};

export const TicketCard: React.FC<TicketCardProps> = ({
    ticket,
    viewMode,
    onShowQR,
    onViewDetails,
    onRequestRefund,
}) => {
    const sessionDate = new Date(ticket.sessionTime);
    const hoursUntilSession = (sessionDate.getTime() - Date.now()) / (1000 * 60 * 60);
    const canRefund = ticket.status === 'ACTIVE' && hoursUntilSession > 24;

    if (viewMode === 'list') {
        return (
            <div className={styles.ticketCardList}>
                <div className={styles.listHeader}>
                    <div className={styles.listMovieInfo}>
                        <h3 className={styles.listMovieTitle}>{ticket.movieTitle}</h3>
                        <div className={styles.listMeta}>
                            <span>{ticket.hallName}</span>
                            <span>{getSeatInfo(ticket)}</span>
                            <span className={styles.ticketTypeBadge}>{ticket.ticketType}</span>
                        </div>
                    </div>
                    <div className={styles.listStatus} style={{ backgroundColor: getStatusColor(ticket.status) }}>
                        {TicketStatusDisplay[ticket.status]}
                    </div>
                </div>

                <div className={styles.listDetails}>
                    <span>📅 {formatDate(ticket.sessionTime)}</span>
                    <span>🕐 {formatTime(ticket.sessionTime)}</span>
                    <span>👤 {ticket.ticketType}</span>
                    <span className={styles.listPrice}>{ticket.price} UAH</span>
                </div>

                <div className={styles.listActions}>
                    <Button
                        variant="secondary"
                        size="small"
                        onClick={() => onShowQR(ticket.ticketCode)}
                        disabled={ticket.status !== 'ACTIVE'}
                    >
                        Show QR
                    </Button>
                    {canRefund && onRequestRefund && (
                        <Button variant="outline" size="small" onClick={() => onRequestRefund(ticket)}>
                            Return
                        </Button>
                    )}
                    {onViewDetails && (
                        <Button variant="outline" size="small" onClick={() => onViewDetails(ticket)}>
                            Details
                        </Button>
                    )}
                </div>
            </div>
        );
    }

    return (
        <div className={styles.ticketCardGrid}>
            <div className={styles.cardHeader}>
                <div className={styles.headerLeft}>
                    <div className={styles.statusBadge} style={{ backgroundColor: getStatusColor(ticket.status) }}>
                        {TicketStatusDisplay[ticket.status]}
                    </div>
                    <div className={styles.ticketTypeBadge}>{ticket.ticketType}</div>
                </div>
                <div className={styles.ticketCode}>#{ticket.ticketCode}</div>
            </div>

            <div className={styles.cardContent}>
                <h3 className={styles.movieTitle}>{ticket.movieTitle}</h3>

                <div className={styles.detailsGrid}>
                    <div className={styles.detailItem}>
                        <div className={styles.detailLabel}>Date</div>
                        <div className={styles.detailValue}>{formatDate(ticket.sessionTime)}</div>
                    </div>
                    <div className={styles.detailItem}>
                        <div className={styles.detailLabel}>Time</div>
                        <div className={styles.detailValue}>{formatTime(ticket.sessionTime)}</div>
                    </div>
                    <div className={styles.detailItem}>
                        <div className={styles.detailLabel}>Hall</div>
                        <div className={styles.detailValue}>{ticket.hallName}</div>
                    </div>
                    <div className={styles.detailItem}>
                        <div className={styles.detailLabel}>Seat</div>
                        <div className={styles.detailValue}>{getSeatInfo(ticket)}</div>
                    </div>
                </div>
            </div>

            <div className={styles.cardFooter}>
                <div className={styles.priceSection}>
                    <div className={styles.priceLabel}>Price</div>
                    <div className={styles.priceValue}>{ticket.price} UAH</div>
                </div>

                <div className={styles.actionButtons}>
                    <Button
                        variant="primary"
                        size="small"
                        onClick={() => onShowQR(ticket.ticketCode)}
                        disabled={ticket.status !== 'ACTIVE'}
                    >
                        Show QR
                    </Button>
                    {canRefund && onRequestRefund && (
                        <Button variant="secondary" size="small" onClick={() => onRequestRefund(ticket)}>
                            Return
                        </Button>
                    )}
                    {onViewDetails && (
                        <Button variant="secondary" size="small" onClick={() => onViewDetails(ticket)}>
                            Details
                        </Button>
                    )}
                </div>
            </div>
        </div>
    );
};