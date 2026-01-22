import React, { useMemo } from 'react';
import type { SessionScheduleResponse } from '@/types/session';
import { SessionCard } from '../SessionCard';
import styles from './SessionList.module.css';

interface SessionListProps {
    sessions: SessionScheduleResponse[];
}

interface GroupedSessions {
    [date: string]: {
        date: string;
        displayDate: string;
        dayOfWeek: string;
        sessions: SessionScheduleResponse[];
    };
}

export const SessionList: React.FC<SessionListProps> = ({ sessions }) => {
    const groupedSessions = useMemo(() => {
        const grouped: GroupedSessions = {};

        sessions.forEach(session => {
            const sessionDate = new Date(session.startTime).toISOString().split('T')[0];

            if (!grouped[sessionDate]) {
                const date = new Date(sessionDate);
                grouped[sessionDate] = {
                    date: sessionDate,
                    displayDate: date.toLocaleDateString('en-US', {
                        month: 'long',
                        day: 'numeric',
                        year: 'numeric'
                    }),
                    dayOfWeek: date.toLocaleDateString('en-US', { weekday: 'long' }),
                    sessions: []
                };
            }

            grouped[sessionDate].sessions.push(session);
        });

        Object.values(grouped).forEach(group => {
            group.sessions.sort((a, b) =>
                new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
            );
        });

        return Object.keys(grouped)
            .sort()
            .reduce((sorted, date) => {
                sorted[date] = grouped[date];
                return sorted;
            }, {} as GroupedSessions);
    }, [sessions]);

    if (sessions.length === 0) {
        return null;
    }

    return (
        <div className={styles.container}>
            {Object.entries(groupedSessions).map(([date, group]) => (
                <div key={date} className={styles.dayGroup}>
                    <div className={styles.dayHeader}>
                        <h2 className={styles.dayTitle}>
                            <span className={styles.dayOfWeek}>{group.dayOfWeek}</span>
                            <span className={styles.date}>{group.displayDate}</span>
                        </h2>
                        <span className={styles.sessionCount}>
                            {group.sessions.length} session{group.sessions.length !== 1 ? 's' : ''}
                        </span>
                    </div>

                    <div className={styles.sessionsGrid}>
                        {group.sessions.map(session => (
                            <SessionCard key={session.id} session={session} />
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );
};