import React, { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, ChevronRight } from "lucide-react";
import type { SessionMovieInfoResponse } from "@/types/session";
import { formatPrice, formatTime } from "@/utils/formatters";
import styles from "./SessionSection.module.css";

interface SessionSectionProps {
  dateList: string[];
  sessionsByDate: Record<string, SessionMovieInfoResponse[]>;
  selectedDate: string | null;
  onDateSelect: (date: string) => void;
  dateScrollIndex: number;
  datesPerView: number;
  onScrollDates: (direction: "left" | "right") => void;
}

const parseLocalDate = (dateString: string): Date =>
  new Date(`${dateString}T00:00:00`);

const getDayLabel = (date: Date): string => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);

  if (date.getTime() === today.getTime()) return "Today";
  if (date.getTime() === tomorrow.getTime()) return "Tomorrow";
  return date.toLocaleDateString("en-US", { weekday: "short" });
};

const getDayInfo = (dateString: string) => {
  const date = parseLocalDate(dateString);
  return {
    dayLabel: getDayLabel(date),
    dayNumber: date.getDate(),
    monthLabel: date.toLocaleDateString("en-US", { month: "short" }),
    fullLabel: date.toLocaleDateString("en-US", {
      weekday: "long",
      month: "long",
      day: "numeric",
    }),
  };
};

export const SessionSection: React.FC<SessionSectionProps> = ({
  dateList,
  sessionsByDate,
  selectedDate,
  onDateSelect,
  dateScrollIndex,
  datesPerView,
  onScrollDates,
}) => {
  const navigate = useNavigate();

  const visibleDates = useMemo(
    () => dateList.slice(dateScrollIndex, dateScrollIndex + datesPerView),
    [dateList, dateScrollIndex, datesPerView],
  );

  const currentSessions = useMemo(
    () => (selectedDate ? sessionsByDate[selectedDate] || [] : []),
    [selectedDate, sessionsByDate],
  );

  const canScrollLeft = dateScrollIndex > 0;
  const canScrollRight = dateScrollIndex < dateList.length - datesPerView;
  const showScrollButtons = dateList.length > datesPerView;

  if (dateList.length === 0) {
    return (
      <div className={styles.noSessions}>
        <h3>No Showtimes Available</h3>
        <p>
          There are currently no scheduled sessions for this movie. Please check
          back later.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.sessionsSection}>
      <h2 className={styles.sectionTitle}>Showtimes</h2>

      <div className={styles.dateCarousel}>
        {showScrollButtons && (
          <button
            className={styles.dateNavButton}
            onClick={() => onScrollDates("left")}
            disabled={!canScrollLeft}
          >
            <ChevronLeft size={18} />
          </button>
        )}

        <div className={styles.dateList}>
          {visibleDates.map((date) => {
            const { dayLabel, dayNumber, monthLabel, fullLabel } =
              getDayInfo(date);
            const isSelected = selectedDate === date;
            return (
              <button
                key={date}
                className={`${styles.dateButton} ${isSelected ? styles.dateButtonActive : ""}`}
                onClick={() => onDateSelect(date)}
                aria-label={fullLabel}
                aria-pressed={isSelected}
              >
                <span className={styles.dateButtonDay}>{dayLabel}</span>
                <span className={styles.dateButtonNumber}>{dayNumber}</span>
                <span className={styles.dateButtonMonth}>{monthLabel}</span>
              </button>
            );
          })}
        </div>

        {showScrollButtons && (
          <button
            className={styles.dateNavButton}
            onClick={() => onScrollDates("right")}
            disabled={!canScrollRight}
          >
            <ChevronRight size={18} />
          </button>
        )}
      </div>

      <div className={styles.sessionsList}>
        {currentSessions.map((session) => (
          <button
            key={session.id}
            className={styles.sessionTimeButton}
            onClick={() => navigate(`/booking/${session.publicId}`)}
          >
            <span className={styles.sessionTimeValue}>
              {formatTime(session.startTime)}
            </span>
            <span className={styles.sessionHall}>{session.hallName}</span>
            <span className={styles.sessionPrice}>
              {formatPrice(session.basePrice)}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
};
