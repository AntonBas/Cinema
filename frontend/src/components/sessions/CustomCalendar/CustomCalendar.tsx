import React, { useState, useEffect } from 'react';
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
    const [availableDates, setAvailableDates] = useState<{ [key: string]: boolean }>({});

    useEffect(() => {
        const date = new Date(selectedDate);
        setCurrentMonth(new Date(date.getFullYear(), date.getMonth(), 1));
    }, [selectedDate]);

    useEffect(() => {
        const sessionDateMap: { [key: string]: boolean } = {};
        sessionDates.forEach(dateStr => {
            const dateObj = new Date(dateStr);
            const year = dateObj.getFullYear();
            const month = String(dateObj.getMonth() + 1).padStart(2, '0');
            const day = String(dateObj.getDate()).padStart(2, '0');
            const formattedDate = `${year}-${month}-${day}`;
            sessionDateMap[formattedDate] = true;
        });
        setAvailableDates(sessionDateMap);
    }, [sessionDates]);

    const formatDate = (date: Date): string => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    const getToday = (): string => {
        const today = new Date();
        return formatDate(today);
    };

    const getDaysInMonth = (year: number, month: number) => {
        return new Date(year, month + 1, 0).getDate();
    };

    const getFirstDayOfMonth = (year: number, month: number) => {
        return new Date(year, month, 1).getDay();
    };

    const handlePrevMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
    };

    const handleNextMonth = () => {
        setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));
    };

    const handleDateClick = (date: Date) => {
        onDateChange(formatDate(date));
    };

    const renderCalendar = () => {
        const year = currentMonth.getFullYear();
        const month = currentMonth.getMonth();
        const daysInMonth = getDaysInMonth(year, month);
        const firstDay = getFirstDayOfMonth(year, month);

        const days = [];
        const dayNames = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];

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
            const isToday = dateString === getToday();
            const hasSession = availableDates[dateString];
            const today = new Date();
            const todayWithoutTime = new Date(today.getFullYear(), today.getMonth(), today.getDate());
            const dateWithoutTime = new Date(year, month, day);
            const isPast = dateWithoutTime < todayWithoutTime;
            const isDisabled = isPast && !isToday;

            days.push(
                <button
                    key={day}
                    onClick={() => !isDisabled && handleDateClick(date)}
                    disabled={isDisabled}
                    className={`${styles.day} ${isSelected ? styles.selected : ''
                        } ${isToday ? styles.today : ''
                        } ${hasSession ? styles.hasSession : ''
                        } ${isDisabled ? styles.disabled : ''
                        }`}
                    title={isDisabled ? "Past dates are not available" : dateString}
                >
                    <span className={styles.dayNumber}>{day}</span>
                    {hasSession && <div className={styles.sessionDot}></div>}
                </button>
            );
        }

        return days;
    };

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
                    <div className={styles.legendDot}></div>
                    <span>Available sessions</span>
                </div>
            </div>
        </div>
    );
};