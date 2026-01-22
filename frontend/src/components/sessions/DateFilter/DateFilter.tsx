import React, { useState } from 'react';
import styles from './DateFilter.module.css';

interface DateFilterProps {
    selectedDate: string;
    onDateChange: (date: string) => void;
}

export const DateFilter: React.FC<DateFilterProps> = ({ selectedDate, onDateChange }) => {
    const today = new Date().toISOString().split('T')[0];
    const [showDatePicker, setShowDatePicker] = useState(false);

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onDateChange(e.target.value);
    };

    const handleTodayClick = () => {
        onDateChange(today);
    };

    const handleTomorrowClick = () => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        onDateChange(tomorrow.toISOString().split('T')[0]);
    };

    const handleNextWeekClick = () => {
        const nextWeek = new Date();
        nextWeek.setDate(nextWeek.getDate() + 7);
        onDateChange(nextWeek.toISOString().split('T')[0]);
    };

    const formatDisplayDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    };

    const formatShortDate = (dateString: string): string => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric'
        });
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h3 className={styles.title}>Select Date</h3>
            </div>

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
                    className={`${styles.quickButton} ${formatShortDate(selectedDate) === formatShortDate(new Date(new Date().setDate(new Date().getDate() + 1)).toISOString().split('T')[0]) ? styles.activeButton : ''}`}
                >
                    Tomorrow
                </button>
                <button
                    type="button"
                    onClick={handleNextWeekClick}
                    className={styles.quickButton}
                >
                    Next Week
                </button>
            </div>

            <div className={styles.dateDisplay}>
                <span className={styles.currentDate}>{formatDisplayDate(selectedDate)}</span>
                <button
                    type="button"
                    onClick={() => setShowDatePicker(!showDatePicker)}
                    className={styles.toggleButton}
                >
                    {showDatePicker ? 'Hide Calendar' : 'Select Date'}
                </button>
            </div>

            {showDatePicker && (
                <div className={styles.datePicker}>
                    <input
                        type="date"
                        value={selectedDate}
                        onChange={handleDateChange}
                        className={styles.dateInput}
                        min={today}
                        max={new Date(new Date().setDate(new Date().getDate() + 60)).toISOString().split('T')[0]}
                    />
                </div>
            )}
        </div>
    );
};