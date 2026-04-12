import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import type { SessionScheduleResponse } from '@/types/session';
import { AgeRatingDisplay, AgeRatingDescription } from '@/types/movie';
import { Button } from '@/components/ui/Button/Button';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import styles from './SessionList.module.css';

interface SessionListProps {
    sessions: SessionScheduleResponse[];
}

interface MovieGroup {
    movieId: number;
    movieTitle: string;
    movieAgeRating: string;
    movieDuration: number;
    sessions: SessionScheduleResponse[];
}

const AGE_RATING_COLORS: Record<string, string> = {
    PEGI_3: styles.ageRatingGreen,
    PEGI_7: styles.ageRatingBlue,
    PEGI_12: styles.ageRatingYellow,
    PEGI_16: styles.ageRatingOrange,
    PEGI_18: styles.ageRatingRed,
};

const formatTime = (dateString: string): string => {
    return new Date(dateString).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
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

export const SessionList: React.FC<SessionListProps> = ({ sessions }) => {
    const navigate = useNavigate();

    const groupedByMovie = useMemo<MovieGroup[]>(() => {
        const groupedMap: Record<number, MovieGroup> = {};

        sessions.forEach(session => {
            if (!groupedMap[session.movieId]) {
                groupedMap[session.movieId] = {
                    movieId: session.movieId,
                    movieTitle: session.movieTitle,
                    movieAgeRating: session.movieAgeRating,
                    movieDuration: session.movieDuration,
                    sessions: [],
                };
            }
            groupedMap[session.movieId].sessions.push(session);
        });

        return Object.values(groupedMap).map(group => ({
            ...group,
            sessions: group.sessions.sort((a, b) =>
                new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
            ),
        }));
    }, [sessions]);

    if (!sessions.length) return null;

    return (
        <div className={styles.container}>
            {groupedByMovie.map(movieGroup => (
                <div key={movieGroup.movieId} className={styles.movieGroup}>
                    <div className={styles.movieHeader}>
                        <div className={styles.posterContainer}>
                            <img
                                src={`/api/movies/${movieGroup.movieId}/poster`}
                                alt={movieGroup.movieTitle}
                                className={styles.poster}
                                loading="lazy"
                                onError={(e) => (e.target as HTMLImageElement).src = '/placeholder-poster.jpg'}
                            />
                        </div>

                        <div className={styles.movieInfo}>
                            <div className={styles.movieHeaderRow}>
                                <h2 className={styles.movieTitle}>{movieGroup.movieTitle}</h2>
                                <Button
                                    variant="secondary"
                                    size="small"
                                    onClick={() => navigate(`/movies/${getMovieSlug(movieGroup.movieTitle)}`)}
                                >
                                    Details
                                </Button>
                            </div>

                            <div className={styles.movieMeta}>
                                <Tooltip
                                    content={AgeRatingDescription[movieGroup.movieAgeRating as keyof typeof AgeRatingDescription] || 'Age rating'}
                                    position="top"
                                >
                                    <span className={`${styles.ageRating} ${AGE_RATING_COLORS[movieGroup.movieAgeRating] || styles.ageRatingRed}`}>
                                        {AgeRatingDisplay[movieGroup.movieAgeRating as keyof typeof AgeRatingDisplay]}
                                    </span>
                                </Tooltip>
                                <span className={styles.duration}>{formatDuration(movieGroup.movieDuration)}</span>
                                <span className={styles.sessionCount}>
                                    {movieGroup.sessions.length} session{movieGroup.sessions.length !== 1 ? 's' : ''}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.sessionsContainer}>
                        <div className={styles.sessionsGrid}>
                            {movieGroup.sessions.map(session => {
                                const isAvailable = session.availableSeats > 0;

                                return (
                                    <div
                                        key={session.id}
                                        className={`${styles.sessionCard} ${isAvailable ? styles.available : styles.unavailable}`}
                                        onClick={() => isAvailable && navigate(`/booking/${session.id}`)}
                                    >
                                        <div className={styles.sessionTime}>
                                            <div className={styles.timeRange}>
                                                <span className={styles.startTime}>{formatTime(session.startTime)}</span>
                                                <span className={styles.timeSeparator}>–</span>
                                                <span className={styles.endTime}>{formatTime(session.endTime)}</span>
                                            </div>
                                            <span className={styles.hall}>{session.hallName}</span>
                                        </div>

                                        <div className={styles.sessionInfo}>
                                            <div className={styles.price}>{session.basePrice.toFixed(0)}₴</div>
                                            <div className={styles.seats}>Available: {session.availableSeats}</div>
                                            <div className={styles.sessionStatus}>
                                                <span className={styles.statusBadge}>
                                                    {isAvailable ? 'Book Now' : 'Sold Out'}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};