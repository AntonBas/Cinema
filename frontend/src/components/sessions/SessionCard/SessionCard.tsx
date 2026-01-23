import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { SessionScheduleResponse } from '@/types/session';
import { SessionStatusDisplay } from '@/types/session';
import { movieApi } from '@/api/movieApi';
import { Button } from '@/components/ui';
import styles from './SessionCard.module.css';

interface SessionCardProps {
    session: SessionScheduleResponse;
}

export const SessionCard: React.FC<SessionCardProps> = ({ session }) => {
    const navigate = useNavigate();

    const formatTime = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    };

    const formatDuration = (minutes: number): string => {
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        return `${hours}h ${mins}m`;
    };

    const getMovieSlug = (title: string): string => {
        return title
            .toLowerCase()
            .replace(/[^\w\s-]/g, '')
            .replace(/\s+/g, '-')
            .replace(/--+/g, '-')
            .trim();
    };

    const handleBookClick = () => {
        navigate(`/book/${session.id}`);
    };

    const handleMovieClick = () => {
        const slug = getMovieSlug(session.movieTitle);
        navigate(`/movies/${slug}`);
    };

    const isAvailable = session.status === 'SCHEDULED' && session.availableSeats > 0;

    return (
        <div className={styles.container}>
            <div className={styles.posterContainer}>
                <img
                    src={movieApi.public.getPosterUrl(session.movieId)}
                    alt={session.movieTitle}
                    className={styles.poster}
                    onError={(e) => {
                        (e.target as HTMLImageElement).src = '/placeholder-poster.jpg';
                    }}
                />
            </div>

            <div className={styles.content}>
                <div className={styles.header}>
                    <div className={styles.movieInfo}>
                        <h3
                            className={styles.movieTitle}
                            onClick={handleMovieClick}
                            style={{ cursor: 'pointer' }}
                        >
                            {session.movieTitle}
                        </h3>
                        <div className={styles.movieMeta}>
                            <span className={styles.ageRating}>{session.movieAgeRating || 'N/A'}</span>
                            <span className={styles.duration}>{formatDuration(session.movieDuration)}</span>
                        </div>
                    </div>
                    <div className={styles.status}>
                        <span className={`${styles.statusBadge} ${styles[session.status.toLowerCase()]}`}>
                            {SessionStatusDisplay[session.status]}
                        </span>
                    </div>
                </div>

                <div className={styles.timeInfo}>
                    <div className={styles.timeSlot}>
                        <span className={styles.timeLabel}>Start:</span>
                        <span className={styles.timeValue}>{formatTime(session.startTime)}</span>
                    </div>
                    <div className={styles.timeSlot}>
                        <span className={styles.timeLabel}>End:</span>
                        <span className={styles.timeValue}>{formatTime(session.endTime)}</span>
                    </div>
                </div>

                <div className={styles.hallInfo}>
                    <span className={styles.hallName}>{session.hallName}</span>
                    <span className={styles.seatsInfo}>
                        {session.availableSeats} / {session.hallCapacity} seats available
                    </span>
                </div>

                <div className={styles.footer}>
                    <div className={styles.price}>
                        <span className={styles.priceLabel}>Price:</span>
                        <span className={styles.priceValue}>₴{parseFloat(session.basePrice).toFixed(0)}</span>
                    </div>
                    <Button
                        variant="primary"
                        onClick={handleBookClick}
                        disabled={!isAvailable}
                        className={styles.bookButton}
                    >
                        {isAvailable ? 'Book Tickets' : 'Unavailable'}
                    </Button>
                </div>
            </div>
        </div>
    );
};