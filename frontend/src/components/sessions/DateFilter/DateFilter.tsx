import React, { useState, useRef, useEffect } from 'react';
import { CustomCalendar } from '../CustomCalendar';
import styles from './DateFilter.module.css';

interface DateFilterProps {
    selectedDate: string;
    onDateChange: (date: string) => void;
    sessionDates?: string[];
}

export const DateFilter: React.FC<DateFilterProps> = ({
    selectedDate,
    onDateChange,
    sessionDates = []
}) => {
    const today = new Date().toISOString().split('T')[0];
    const [isCalendarOpen, setIsCalendarOpen] = useState(false);
    const calendarRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (calendarRef.current && !calendarRef.current.contains(event.target as Node)) {
                setIsCalendarOpen(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleTodayClick = () => {
        onDateChange(today);
        setIsCalendarOpen(false);
    };

    const handleTomorrowClick = () => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        onDateChange(tomorrow.toISOString().split('T')[0]);
        setIsCalendarOpen(false);
    };

    const handleDateClick = (date: string) => {
        onDateChange(date);
        setIsCalendarOpen(false);
    };

    const formatDisplayDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    };

    const isTomorrow = selectedDate === new Date(new Date().setDate(new Date().getDate() + 1)).toISOString().split('T')[0];

    return (
        <div className={styles.container} ref={calendarRef}>
            <div className={styles.quickActions}>
                <button
                    type="button"
                    onClick={handleTodayClick}
                    className={`${styles.quickButton} ${selectedDate === today ? styles.activeButton : ''}`}
                >
                    Today
                </button>
                <button
                    type="button"
                    onClick={handleTomorrowClick}
                    className={`${styles.quickButton} ${isTomorrow ? styles.activeButton : ''}`}
                >
                    Tomorrow
                </button>
            </div>

            <div
                className={styles.dateDisplay}
                onClick={() => setIsCalendarOpen(!isCalendarOpen)}
            >
                <div className={styles.dateInfo}>
                    <span className={styles.currentDate}>{formatDisplayDate(selectedDate)}</span>
                    <button
                        className={styles.calendarToggle}
                        onClick={(e) => {
                            e.stopPropagation();
                            setIsCalendarOpen(!isCalendarOpen);
                        }}
                    >
                        {isCalendarOpen ? '▲' : '▼'}
                    </button>
                </div>
            </div>

            {isCalendarOpen && (
                <div className={styles.calendarWrapper}>
                    <CustomCalendar
                        selectedDate={selectedDate}
                        onDateChange={handleDateClick}
                        sessionDates={sessionDates}
                    />
                </div>
            )}
        </div>
    );
};