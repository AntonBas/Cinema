import React, { useState, useRef, useEffect } from "react";
import { ChevronUp, ChevronDown } from "lucide-react";
import { CustomCalendar } from "../CustomCalendar/CustomCalendar";
import styles from "./DateFilter.module.css";

interface DateFilterProps {
  selectedDate: string;
  onDateChange: (date: string) => void;
  sessionDates?: string[];
}

const formatDisplayDate = (dateString: string): string => {
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return "Select date";
  return date.toLocaleDateString("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
  });
};

export const DateFilter: React.FC<DateFilterProps> = ({
  selectedDate,
  onDateChange,
  sessionDates = [],
}) => {
  const today = new Date().toISOString().split("T")[0];
  const tomorrow = new Date(Date.now() + 86400000).toISOString().split("T")[0];

  const [isCalendarOpen, setIsCalendarOpen] = useState(false);
  const calendarRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        calendarRef.current &&
        !calendarRef.current.contains(event.target as Node)
      ) {
        setIsCalendarOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleDateSelect = (date: string) => {
    onDateChange(date);
    setIsCalendarOpen(false);
  };

  const isToday = selectedDate === today;
  const isTomorrow = selectedDate === tomorrow;

  return (
    <div className={styles.container} ref={calendarRef}>
      <div className={styles.filterHeader}>
        <h3 className={styles.title}>Date</h3>
      </div>

      <div className={styles.dateSelector}>
        <div className={styles.quickActions}>
          <button
            type="button"
            onClick={() => handleDateSelect(today)}
            className={`${styles.quickButton} ${isToday ? styles.activeButton : ""}`}
          >
            Today
          </button>
          <button
            type="button"
            onClick={() => handleDateSelect(tomorrow)}
            className={`${styles.quickButton} ${isTomorrow ? styles.activeButton : ""}`}
          >
            Tomorrow
          </button>
        </div>

        <button
          className={styles.dateDisplay}
          onClick={() => setIsCalendarOpen(!isCalendarOpen)}
        >
          <span className={styles.currentDate}>
            {formatDisplayDate(selectedDate)}
          </span>
          <span className={styles.calendarToggle}>
            {isCalendarOpen ? (
              <ChevronUp size={16} />
            ) : (
              <ChevronDown size={16} />
            )}
          </span>
        </button>
      </div>

      {isCalendarOpen && (
        <div className={styles.calendarWrapper}>
          <CustomCalendar
            selectedDate={selectedDate}
            onDateChange={handleDateSelect}
            sessionDates={sessionDates}
          />
        </div>
      )}
    </div>
  );
};
