import React, { useState, useMemo } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import styles from "./Calendar.module.css";

interface CalendarProps {
  selectedDate: string;
  onDateChange: (date: string) => void;
  highlightedDates?: string[];
}

const DAY_NAMES = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

const formatDate = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const getDaysInMonth = (year: number, month: number): number => {
  return new Date(year, month + 1, 0).getDate();
};

const getFirstDayOfMonth = (year: number, month: number): number => {
  const day = new Date(year, month, 1).getDay();
  return day === 0 ? 6 : day - 1;
};

export const Calendar: React.FC<CalendarProps> = ({
  selectedDate,
  onDateChange,
  highlightedDates = [],
}) => {
  const initialDate = selectedDate ? new Date(selectedDate) : new Date();
  const [currentMonth, setCurrentMonth] = useState(
    new Date(initialDate.getFullYear(), initialDate.getMonth(), 1),
  );

  const highlightedSet = useMemo(() => {
    const dates = new Set<string>();
    highlightedDates.forEach((dateStr) => {
      const date = new Date(dateStr);
      if (!isNaN(date.getTime())) {
        dates.add(formatDate(date));
      }
    });
    return dates;
  }, [highlightedDates]);

  const handlePrevMonth = () => {
    setCurrentMonth(
      (prev) => new Date(prev.getFullYear(), prev.getMonth() - 1, 1),
    );
  };

  const handleNextMonth = () => {
    setCurrentMonth(
      (prev) => new Date(prev.getFullYear(), prev.getMonth() + 1, 1),
    );
  };

  const handleDateClick = (date: Date) => {
    onDateChange(formatDate(date));
  };

  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const daysInMonth = getDaysInMonth(year, month);
  const firstDay = getFirstDayOfMonth(year, month);
  const today = formatDate(new Date());
  const todayDate = new Date();
  todayDate.setHours(0, 0, 0, 0);

  const days = [];

  for (let i = 0; i < 7; i++) {
    days.push(
      <div key={`day-${i}`} className={styles.dayName}>
        {DAY_NAMES[i]}
      </div>,
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
    const isHighlighted = highlightedSet.has(dateString);
    const isPast = date < todayDate && !isToday;

    days.push(
      <button
        key={day}
        onClick={() => handleDateClick(date)}
        disabled={isPast}
        className={`${styles.day} 
                    ${isSelected ? styles.selected : ""} 
                    ${isToday ? styles.today : ""} 
                    ${isHighlighted ? styles.highlighted : ""} 
                    ${isPast ? styles.disabled : ""}`}
      >
        <span className={styles.dayNumber}>{day}</span>
        {isHighlighted && <div className={styles.highlightedDot} />}
      </button>,
    );
  }

  const monthYear = currentMonth.toLocaleDateString("en-US", {
    month: "long",
    year: "numeric",
  });

  return (
    <div className={styles.calendar}>
      <div className={styles.header}>
        <button className={styles.navButton} onClick={handlePrevMonth}>
          <ChevronLeft size={16} />
        </button>
        <h3 className={styles.monthTitle}>{monthYear}</h3>
        <button className={styles.navButton} onClick={handleNextMonth}>
          <ChevronRight size={16} />
        </button>
      </div>
      <div className={styles.daysGrid}>{days}</div>
      {highlightedDates.length > 0 && (
        <div className={styles.legend}>
          <div className={styles.legendItem}>
            <div className={styles.legendDot} />
            <span>Available sessions</span>
          </div>
        </div>
      )}
    </div>
  );
};
