import React, { useMemo, useCallback } from 'react';
import type { SessionAdminResponse } from '@/types/session';
import type { BadgeVariant } from '@/components/ui/Badge/Badge';
import { Button, Badge } from '@/components/ui';
import styles from './SessionTable.module.css';

interface SessionTableProps {
    sessions: SessionAdminResponse[];
    onEdit: (session: SessionAdminResponse) => void;
    onDelete: (session: SessionAdminResponse) => void;
    onCancel: (session: SessionAdminResponse) => void;
    onReactivate: (session: SessionAdminResponse) => void;
    onViewDetails?: (session: SessionAdminResponse) => void;
}

const getStatusText = (status: string): string => {
    const statusMap: Record<string, string> = {
        SCHEDULED: 'Scheduled',
        ONGOING: 'Ongoing',
        COMPLETED: 'Completed',
        CANCELLED: 'Cancelled'
    };
    return statusMap[status] || status;
};

const getStatusBadgeVariant = (status: string): BadgeVariant => {
    const variantMap: Record<string, BadgeVariant> = {
        SCHEDULED: 'info',
        ONGOING: 'success',
        COMPLETED: 'secondary',
        CANCELLED: 'error'
    };
    return variantMap[status] || 'secondary';
};

const canEdit = (status: string): boolean => status === 'SCHEDULED';
const canDelete = (status: string): boolean => status === 'SCHEDULED';
const canCancel = (status: string): boolean => status === 'SCHEDULED';
const canReactivate = (status: string): boolean => status === 'CANCELLED';

const getOccupancyPercentage = (ticketsSold: number, capacity: number): number => {
    return capacity > 0 ? Math.round((ticketsSold / capacity) * 100) : 0;
};

const getOccupancyColor = (percentage: number): string => {
    if (percentage >= 80) return '#10b981';
    if (percentage >= 50) return '#f59e0b';
    return '#6b7280';
};

const formatCurrency = (price: string | number | null | undefined): string => {
    if (price === null || price === undefined) return '0.00 UAH';
    const numericPrice = typeof price === 'string' ? parseFloat(price) : price;
    if (isNaN(numericPrice)) return '0.00 UAH';
    return `${numericPrice.toFixed(2)} UAH`;
};

const formatTime = (dateString: string): string => {
    return new Date(dateString).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit'
    });
};

const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric'
    });
};

const truncateText = (text: string | null | undefined, maxLength: number): string => {
    if (!text) return '';
    return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
};

const EmptyState: React.FC = () => (
    <div className={styles.empty}>
        <div className={styles.emptyIcon}>📽️</div>
        <h3>No sessions found</h3>
        <p>There are currently no movie sessions matching your criteria.</p>
    </div>
);

export const SessionTable: React.FC<SessionTableProps> = ({
    sessions,
    onEdit,
    onDelete,
    onCancel,
    onReactivate,
    onViewDetails
}) => {
    const sortedSessions = useMemo(() => {
        return [...sessions].sort((a, b) =>
            new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        );
    }, [sessions]);

    const handleEdit = useCallback((e: React.MouseEvent, session: SessionAdminResponse) => {
        e.stopPropagation();
        onEdit(session);
    }, [onEdit]);

    const handleDelete = useCallback((e: React.MouseEvent, session: SessionAdminResponse) => {
        e.stopPropagation();
        onDelete(session);
    }, [onDelete]);

    const handleCancel = useCallback((e: React.MouseEvent, session: SessionAdminResponse) => {
        e.stopPropagation();
        onCancel(session);
    }, [onCancel]);

    const handleReactivate = useCallback((e: React.MouseEvent, session: SessionAdminResponse) => {
        e.stopPropagation();
        onReactivate(session);
    }, [onReactivate]);

    const handleRowClick = useCallback((session: SessionAdminResponse) => {
        onViewDetails?.(session);
    }, [onViewDetails]);

    const renderTableHeader = useCallback(() => (
        <div className={styles.tableHeader}>
            <div>Movie</div>
            <div>Hall</div>
            <div>Time</div>
            <div>Price</div>
            <div>Occupancy</div>
            <div>Revenue</div>
            <div>Status</div>
            <div>Actions</div>
        </div>
    ), []);

    const renderTableRow = useCallback((session: SessionAdminResponse) => {
        const occupancy = getOccupancyPercentage(
            session.ticketsSold,
            session.hallCapacity
        );
        const occupancyColor = getOccupancyColor(occupancy);
        const isPast = new Date(session.endTime) < new Date();
        const editable = canEdit(session.status);
        const deletable = canDelete(session.status);
        const cancellable = canCancel(session.status);
        const reactivatable = canReactivate(session.status);

        const totalRevenue = parseFloat(session.totalRevenue || '0');
        const avgRevenue = session.ticketsSold > 0
            ? totalRevenue / session.ticketsSold
            : 0;

        return (
            <div
                key={session.id}
                className={`${styles.tableRow} ${isPast ? styles.past : ''}`}
                onClick={() => handleRowClick(session)}
                style={{ cursor: onViewDetails ? 'pointer' : 'default' }}
            >
                <div className={styles.movieInfo}>
                    <div className={styles.movieTitle}>
                        {truncateText(session.movieTitle, 30)}
                    </div>
                    <div className={styles.movieMeta}>
                        <span className={styles.duration}>
                            {session.movieDuration} min
                        </span>
                    </div>
                </div>

                <div className={styles.hallInfo}>
                    <div className={styles.hallName}>
                        {truncateText(session.hallName, 20)}
                    </div>
                    <div className={styles.capacity}>
                        {session.hallCapacity} seats
                    </div>
                </div>

                <div className={styles.timeInfo}>
                    <div className={styles.date}>
                        {formatDate(session.startTime)}
                    </div>
                    <div className={styles.time}>
                        {formatTime(session.startTime)}
                    </div>
                    <div className={styles.endTime}>
                        - {formatTime(session.endTime)}
                    </div>
                </div>

                <div className={styles.price}>
                    {formatCurrency(session.basePrice)}
                </div>

                <div className={styles.occupancyWrapper}>
                    <div className={styles.occupancyInfo}>
                        <div className={styles.ticketCount}>
                            {session.ticketsSold}/{session.hallCapacity}
                        </div>
                        <div className={styles.occupancyPercent}>
                            {occupancy}%
                        </div>
                    </div>
                    <div className={styles.occupancyBar}>
                        <div
                            className={styles.occupancyFill}
                            style={{
                                width: `${Math.min(occupancy, 100)}%`,
                                backgroundColor: occupancyColor
                            }}
                        />
                    </div>
                </div>

                <div className={styles.revenueInfo}>
                    <div className={styles.revenue}>
                        {formatCurrency(totalRevenue)}
                    </div>
                    {session.ticketsSold > 0 && (
                        <div className={styles.revenuePerSeat}>
                            {avgRevenue.toFixed(2)} UAH avg
                        </div>
                    )}
                </div>

                <div className={styles.status}>
                    <Badge variant={getStatusBadgeVariant(session.status)}>
                        {getStatusText(session.status)}
                    </Badge>
                </div>

                <div className={styles.actions}>
                    {editable && (
                        <Button
                            variant="success"
                            size="small"
                            onClick={(e) => handleEdit(e, session)}
                            className={styles.actionButton}
                        >
                            Edit
                        </Button>
                    )}
                    {cancellable && (
                        <Button
                            variant="secondary"
                            size="small"
                            onClick={(e) => handleCancel(e, session)}
                            className={styles.actionButton}
                        >
                            Cancel
                        </Button>
                    )}
                    {reactivatable && (
                        <Button
                            variant="success"
                            size="small"
                            onClick={(e) => handleReactivate(e, session)}
                            className={styles.actionButton}
                        >
                            Reactivate
                        </Button>
                    )}
                    {deletable && (
                        <Button
                            variant="error"
                            size="small"
                            onClick={(e) => handleDelete(e, session)}
                            className={styles.actionButton}
                        >
                            Delete
                        </Button>
                    )}
                </div>
            </div>
        );
    }, [handleEdit, handleDelete, handleCancel, handleReactivate, handleRowClick, onViewDetails]);

    if (!sessions.length) {
        return <EmptyState />;
    }

    return (
        <div className={styles.table}>
            {renderTableHeader()}
            {sortedSessions.map(renderTableRow)}
        </div>
    );
};