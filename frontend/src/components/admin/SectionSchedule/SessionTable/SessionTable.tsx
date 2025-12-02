import React from 'react';
import type { SessionAdminResponse } from '@/types/session';
import { Button, Badge } from '@/components/ui';
import styles from './SessionTable.module.css';

interface SessionTableProps {
    sessions: SessionAdminResponse[];
    loading: boolean;
    onEdit: (session: SessionAdminResponse) => void;
    onDelete: (session: SessionAdminResponse) => void;
}

export const SessionTable: React.FC<SessionTableProps> = ({
    sessions,
    loading,
    onEdit,
    onDelete
}) => {
    if (loading) {
        return <div className={styles.loading}>Loading sessions...</div>;
    }

    if (sessions.length === 0) {
        return <div className={styles.empty}>No sessions found</div>;
    }

    const formatDateTime = (dateString: string) => {
        return new Date(dateString).toLocaleString();
    };

    const formatPrice = (price: number) => {
        return `${price.toFixed(2)} UAH`;
    };

    return (
        <div className={styles.tableContainer}>
            <table className={styles.table}>
                <thead>
                    <tr>
                        <th>Movie</th>
                        <th>Hall</th>
                        <th>Start Time</th>
                        <th>End Time</th>
                        <th>Price</th>
                        <th>Tickets</th>
                        <th>Revenue</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {sessions.map((session) => (
                        <tr key={session.id} className={styles.row}>
                            <td className={styles.movieCell}>
                                <div className={styles.movieInfo}>
                                    <span className={styles.movieTitle}>
                                        {session.movieTitle}
                                    </span>
                                    <span className={styles.duration}>
                                        {session.movieDuration} min
                                    </span>
                                </div>
                            </td>
                            <td>
                                <div>
                                    <div>{session.hallName}</div>
                                    <div className={styles.capacity}>
                                        {session.hallCapacity} seats
                                    </div>
                                </div>
                            </td>
                            <td>{formatDateTime(session.startTime)}</td>
                            <td>{session.endTime ? formatDateTime(session.endTime) : 'N/A'}</td>
                            <td>{formatPrice(session.price)}</td>
                            <td>
                                <div className={styles.ticketInfo}>
                                    {session.ticketsSold || 0} / {session.hallCapacity}
                                </div>
                            </td>
                            <td>
                                <div className={styles.revenue}>
                                    {session.totalRevenue ? `${session.totalRevenue.toFixed(2)} UAH` : '0.00 UAH'}
                                </div>
                            </td>
                            <td>
                                <Badge
                                    variant={session.available ? 'success' : 'error'}
                                >
                                    {session.available ? 'Available' : 'Ended'}
                                </Badge>
                            </td>
                            <td>
                                <div className={styles.actions}>
                                    <Button
                                        variant="success"
                                        size="small"
                                        onClick={() => onEdit(session)}
                                    >
                                        Edit
                                    </Button>
                                    <Button
                                        variant="error"
                                        size="small"
                                        onClick={() => onDelete(session)}
                                    >
                                        Delete
                                    </Button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};