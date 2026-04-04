import React from 'react';
import { Button } from '@/components/ui';
import type { TicketResponse } from '@/types/ticket';
import { TicketStatusDisplay } from '@/types/ticket';
import { QrCode, Calendar, MapPin, User, Clock, ArrowRight, Undo2, Tag } from 'lucide-react';
import styles from './TicketCard.module.css';

interface TicketCardProps {
    ticket: TicketResponse;
    viewMode: 'grid' | 'list';
    onShowQR: (ticketCode: string) => void;
    onViewDetails?: (ticket: TicketResponse) => void;
    onRequestRefund?: (ticket: TicketResponse) => void;
}

export const TicketCard: React.FC<TicketCardProps> = ({
    ticket,
    viewMode,
    onShowQR,
    onViewDetails,
    onRequestRefund
}) => {
    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    };

    const formatTime = (dateString: string) => {
        return new Date(dateString).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusColor = (status: string) => {
        const colors = {
            ACTIVE: '#48bb78',
            USED: '#3b82f6',
            REFUNDED: '#ed8936'
        };
        return colors[status as keyof typeof colors] || '#a0a8c0';
    };

    const getSeatInfo = () => {
        if (ticket.row == null || ticket.seatNumber == null) {
            return 'Seat not assigned';
        }
        return `Row ${ticket.row}, Seat ${ticket.seatNumber}`;
    };

    const canShowRefundButton = ticket.status === 'ACTIVE';
    const sessionDate = new Date(ticket.sessionTime);
    const now = new Date();
    const hoursUntilSession = (sessionDate.getTime() - now.getTime()) / (1000 * 60 * 60);
    const isWithinRefundWindow = hoursUntilSession > 24;

    if (viewMode === 'list') {
        return (
            <div className={styles.ticketCardList}>
                <div className={styles.listHeader}>
                    <div className={styles.listMovieInfo}>
                        <h3 className={styles.listMovieTitle}>{ticket.movieTitle}</h3>
                        <div className={styles.listMeta}>
                            <span className={styles.listHall}>{ticket.hallName}</span>
                            <span className={styles.listSeat}>
                                {getSeatInfo()}
                            </span>
                            <span className={styles.ticketTypeBadge}>
                                <Tag size={12} /> {ticket.ticketType}
                            </span>
                        </div>
                    </div>
                    <div className={styles.listStatus} style={{ backgroundColor: getStatusColor(ticket.status) }}>
                        {TicketStatusDisplay[ticket.status]}
                    </div>
                </div>

                <div className={styles.listDetails}>
                    <div className={styles.listDetailItem}>
                        <Calendar size={16} />
                        <span>{formatDate(ticket.sessionTime)}</span>
                    </div>
                    <div className={styles.listDetailItem}>
                        <Clock size={16} />
                        <span>{formatTime(ticket.sessionTime)}</span>
                    </div>
                    <div className={styles.listDetailItem}>
                        <User size={16} />
                        <span>{ticket.ticketType}</span>
                    </div>
                    <div className={styles.listPrice}>
                        {ticket.price} UAH
                    </div>
                </div>

                <div className={styles.listActions}>
                    <Button
                        variant="secondary"
                        size="small"
                        onClick={() => onShowQR(ticket.ticketCode)}
                        disabled={ticket.status !== 'ACTIVE'}
                    >
                        <QrCode size={18} /> {ticket.status === 'ACTIVE' ? 'Show QR' : 'QR Code'}
                    </Button>

                    {canShowRefundButton && isWithinRefundWindow && onRequestRefund && (
                        <Button
                            variant="outline"
                            size="small"
                            onClick={() => onRequestRefund(ticket)}
                        >
                            <Undo2 size={18} /> Return
                        </Button>
                    )}

                    {onViewDetails && (
                        <Button
                            variant="outline"
                            size="small"
                            onClick={() => onViewDetails(ticket)}
                        >
                            <ArrowRight size={18} /> Details
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
                    <div className={styles.ticketTypeBadge}>
                        <Tag size={12} /> {ticket.ticketType}
                    </div>
                </div>
                <div className={styles.ticketCode}>
                    #{ticket.ticketCode}
                </div>
            </div>

            <div className={styles.cardContent}>
                <h3 className={styles.movieTitle}>{ticket.movieTitle}</h3>

                <div className={styles.detailsGrid}>
                    <div className={styles.detailItem}>
                        <Calendar size={16} className={styles.detailIcon} />
                        <div>
                            <div className={styles.detailLabel}>Date</div>
                            <div className={styles.detailValue}>{formatDate(ticket.sessionTime)}</div>
                        </div>
                    </div>

                    <div className={styles.detailItem}>
                        <Clock size={16} className={styles.detailIcon} />
                        <div>
                            <div className={styles.detailLabel}>Time</div>
                            <div className={styles.detailValue}>{formatTime(ticket.sessionTime)}</div>
                        </div>
                    </div>

                    <div className={styles.detailItem}>
                        <MapPin size={16} className={styles.detailIcon} />
                        <div>
                            <div className={styles.detailLabel}>Hall</div>
                            <div className={styles.detailValue}>{ticket.hallName}</div>
                        </div>
                    </div>

                    <div className={styles.detailItem}>
                        <User size={16} className={styles.detailIcon} />
                        <div>
                            <div className={styles.detailLabel}>Seat</div>
                            <div className={styles.detailValue}>
                                {getSeatInfo()}
                            </div>
                        </div>
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
                        <QrCode size={18} /> {ticket.status === 'ACTIVE' ? 'Show QR' : 'QR Code'}
                    </Button>

                    {canShowRefundButton && isWithinRefundWindow && onRequestRefund && (
                        <Button
                            variant="secondary"
                            size="small"
                            onClick={() => onRequestRefund(ticket)}
                        >
                            <Undo2 size={18} /> Return
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