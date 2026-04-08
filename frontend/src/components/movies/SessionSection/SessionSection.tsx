import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { SessionMovieInfoResponse } from '@/types/session';
import styles from './SessionSection.module.css';

interface SessionSectionProps {
    dateList: string[];
    sessionsByDate: Record<string, SessionMovieInfoResponse[]>;
    selectedDate: string | null;
    onDateSelect: (date: string) => void;
    dateScrollIndex: number;
    datesPerView: number;
    onScrollDates: (direction: 'left' | 'right') => void;
}

const getDayInfo = (dateString: string) => {
    const date = new Date(dateString);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    if (dateOnly.getTime() === today.getTime()) {
        return { label: 'Today', shortLabel: 'Today' };
    }
    if (dateOnly.getTime() === tomorrow.getTime()) {
        return { label: 'Tomorrow', shortLabel: 'Tomorrow' };
    }
    return {
        label: date.toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' }),
        shortLabel: date.toLocaleDateString('en-US', { weekday: 'short' })
    };
};

const formatTime = (dateTimeString: string): string => {
    return new Date(dateTimeString).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
};

export const SessionSection: React.FC<SessionSectionProps> = ({
    dateList,
    sessionsByDate,
    selectedDate,
    onDateSelect,
    dateScrollIndex,
    datesPerView,
    onScrollDates
}) => {
    const navigate = useNavigate();
    const visibleDates = dateList.slice(dateScrollIndex, dateScrollIndex + datesPerView);
    const currentSessions = selectedDate ? sessionsByDate[selectedDate] || [] : [];

    if (dateList.length === 0) {
        return (
            <div className={styles.noSessions}>
                <div className={styles.noSessionsIcon}>🎬</div>
                <h3>No Showtimes Available</h3>
                <p>There are currently no scheduled sessions for this movie. Please check back later.</p>
            </div>
        );
    }

    return (
        <div className={styles.sessionsSection}>
            <h2 className={styles.sectionTitle}>Showtimes</h2>

            <div className={styles.dateCarousel}>
                {dateList.length > datesPerView && (
                    <button
                        className={styles.dateNavButton}
                        onClick={() => onScrollDates('left')}
                        disabled={dateScrollIndex === 0}
                    >
                        &#10094;
                    </button>
                )}

                <div className={styles.dateList}>
                    {visibleDates.map(date => {
                        const { shortLabel } = getDayInfo(date);
                        const isSelected = selectedDate === date;
                        return (
                            <button
                                key={date}
                                className={`${styles.dateButton} ${isSelected ? styles.dateButtonActive : ''}`}
                                onClick={() => onDateSelect(date)}
                            >
                                <span className={styles.dateButtonDay}>{shortLabel}</span>
                                <span className={styles.dateButtonNumber}>
                                    {new Date(date).getDate()}
                                </span>
                            </button>
                        );
                    })}
                </div>

                {dateList.length > datesPerView && (
                    <button
                        className={styles.dateNavButton}
                        onClick={() => onScrollDates('right')}
                        disabled={dateScrollIndex >= dateList.length - datesPerView}
                    >
                        &#10095;
                    </button>
                )}
            </div>

            <div className={styles.sessionsList}>
                {currentSessions.map((session) => (
                    <button
                        key={session.id}
                        className={styles.sessionTimeButton}
                        onClick={() => navigate(`/booking/${session.id}`)}
                    >
                        <span className={styles.sessionTimeValue}>
                            {formatTime(session.startTime)}
                        </span>
                        <span className={styles.sessionHall}>
                            {session.hallName}
                        </span>
                        <span className={styles.sessionPrice}>
                            {session.basePrice}₴
                        </span>
                    </button>
                ))}
            </div>
        </div>
    );
};