import React, { useState, useEffect, useCallback, useMemo } from 'react';
import styles from './CustomCalendar.module.css';

interface CustomCalendarProps {
    selectedDate: string;
    onDateChange: (date: string) => void;
    sessionDates?: string[];
}

export const CustomCalendar: React.FC<CustomCalendarProps> = ({
    selectedDate,
    onDateChange,
    sessionDates = []
}) => {
    const [currentMonth, setCurrentMonth] = useState(new Date());

    useEffect(() => {
        const date = new Date(selectedDate);
        if (!isNaN(date.getTime())) {
            setCurrentMonth(new Date(date.getFullYear(), date.getMonth(), 1));
        }
    }, [selectedDate]);

    const availableDatesSet = useMemo(() => {
        const dates = new Set<string>();
        sessionDates.forEach(dateStr => {
            const date = new Date(dateStr);
            if (!isNaN(date.getTime())) {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                dates.add(`${year}-${month}-${day}`);
            }
        });
        return dates;
    }, [sessionDates]);

    const formatDate = useCallback((date: Date): string => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }, []);

    const getToday = useCallback((): string => {
        const today = new Date();
        return formatDate(today);
    }, [formatDate]);

    const getDaysInMonth = useCallback((year: number, month: number) => {
        return new Date(year, month + 1, 0).getDate();
    }, []);

    const getFirstDayOfMonth = useCallback((year: number, month: number) => {
        const day = new Date(year, month, 1).getDay();
        return day === 0 ? 6 : day - 1;
    }, []);

    const handlePrevMonth = useCallback(() => {
        setCurrentMonth(prev => new Date(prev.getFullYear(), prev.getMonth() - 1, 1));
    }, []);

    const handleNextMonth = useCallback(() => {
        setCurrentMonth(prev => new Date(prev.getFullYear(), prev.getMonth() + 1, 1));
    }, []);

    const handleDateClick = useCallback((date: Date) => {
        onDateChange(formatDate(date));
    }, [onDateChange, formatDate]);

    const renderCalendar = useCallback(() => {
        const year = currentMonth.getFullYear();
        const month = currentMonth.getMonth();
        const daysInMonth = getDaysInMonth(year, month);
        const firstDay = getFirstDayOfMonth(year, month);
        const today = getToday();
        const todayDate = new Date();
        todayDate.setHours(0, 0, 0, 0);

        const days = [];
        const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

        for (let i = 0; i < 7; i++) {
            days.push(
                <div key={`day-${i}`} className={styles.dayName}>
                    {dayNames[i]}
                </div>
            );
        }

        for (let i = 0; i < firstDay; i++) {
            days.push(<div key={`empty-${i}`} className={styles.emptyDay}></div>);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dateString = formatDate(date);
            const isSelected = dateString === selectedDate;
            const isToday = dateString === today;
            const hasSession = availableDatesSet.has(dateString);
            const isPast = date < todayDate && !isToday;

            days.push(
                <button
                    key={day}
                    onClick={() => handleDateClick(date)}
                    disabled={isPast}
                    className={`${styles.day} 
                        ${isSelected ? styles.selected : ''} 
                        ${isToday ? styles.today : ''} 
                        ${hasSession ? styles.hasSession : ''} 
                        ${isPast ? styles.disabled : ''}`}
                    title={isPast ? "Past dates are not available" : undefined}
                >
                    <span className={styles.dayNumber}>{day}</span>
                    {hasSession && <div className={styles.sessionDot} />}
                </button>
            );
        }

        return days;
    }, [currentMonth, getDaysInMonth, getFirstDayOfMonth, getToday, formatDate, selectedDate, availableDatesSet, handleDateClick]);

    const monthYear = currentMonth.toLocaleDateString('en-US', {
        month: 'long',
        year: 'numeric'
    });

    return (
        <div className={styles.calendar}>
            <div className={styles.header}>
                <button
                    className={styles.navButton}
                    onClick={handlePrevMonth}
                    aria-label="Previous month"
                >
                    ←
                </button>
                <h3 className={styles.monthTitle}>{monthYear}</h3>
                <button
                    className={styles.navButton}
                    onClick={handleNextMonth}
                    aria-label="Next month"
                >
                    →
                </button>
            </div>
            <div className={styles.daysGrid}>
                {renderCalendar()}
            </div>
            <div className={styles.legend}>
                <div className={styles.legendItem}>
                    <div className={styles.legendDot} />
                    <span>Available sessions</span>
                </div>
            </div>
        </div>
    );
};