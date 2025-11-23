import React from 'react';
import type { SessionResponse } from '@/types/session';
import { Button, Badge } from '@/components/ui';
import styles from './SessionTable.module.css';

interface SessionTableProps {
    sessions: SessionResponse[];
    loading: boolean;
    onEdit: (session: SessionResponse) => void;
    onDelete: (session: SessionResponse) => void;
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
                                        {session.movie.title}
                                    </span>
                                    <span className={styles.duration}>
                                        {session.movie.durationMinutes} min
                                    </span>
                                </div>
                            </td>
                            <td>{session.hall.name}</td>
                            <td>{formatDateTime(session.startTime)}</td>
                            <td>{formatDateTime(session.endTime)}</td>
                            <td>{formatPrice(session.price)}</td>
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