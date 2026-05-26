import React, { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, ChevronRight } from "lucide-react";
import type { SessionMovieInfoResponse } from "@/types/session";
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

const today = new Date();
today.setHours(0, 0, 0, 0);

const getDayInfo = (dateString: string) => {
  const date = new Date(dateString);
  const dateOnly = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
  );

  if (dateOnly.getTime() === today.getTime()) {
    return { shortLabel: "Today", dayNumber: date.getDate() };
  }
  return {
    shortLabel: date.toLocaleDateString("en-US", { weekday: "short" }),
    dayNumber: date.getDate(),
  };
};

const formatTime = (dateTimeString: string): string => {
  return new Date(dateTimeString).toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
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
            const { shortLabel, dayNumber } = getDayInfo(date);
            const isSelected = selectedDate === date;
            return (
              <button
                key={date}
                className={`${styles.dateButton} ${isSelected ? styles.dateButtonActive : ""}`}
                onClick={() => onDateSelect(date)}
              >
                <span className={styles.dateButtonDay}>{shortLabel}</span>
                <span className={styles.dateButtonNumber}>{dayNumber}</span>
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
            onClick={() => navigate(`/booking/${session.id}`)}
          >
            <span className={styles.sessionTimeValue}>
              {formatTime(session.startTime)}
            </span>
            <span className={styles.sessionHall}>{session.hallName}</span>
            <span className={styles.sessionPrice}>{session.basePrice}₴</span>
          </button>
        ))}
      </div>
    </div>
  );
};
