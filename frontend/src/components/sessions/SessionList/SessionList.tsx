import React, { useMemo, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import type { SessionScheduleResponse } from "@/types/session";
import { AgeRatingDisplay, AgeRatingDescription } from "@/types/movie";
import { Button } from "@/components/ui/Button/Button";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import { DEFAULT_POSTER_URL, resolvePosterUrl } from "@/utils/posterUrl";
import { formatPrice, formatTime } from "@/utils/formatters";
import styles from "./SessionList.module.css";

interface SessionListProps {
  sessions: SessionScheduleResponse[];
}

interface MovieGroup {
  movieId: number;
  movieTitle: string;
  movieSlug: string;
  movieAgeRating: string;
  movieDuration: number;
  sessions: SessionScheduleResponse[];
}

interface SessionCardProps {
  session: SessionScheduleResponse;
  onBook: (sessionPublicId: string) => void;
}

const AGE_RATING_COLORS: Record<string, string> = {
  PEGI_3: styles.ageRatingGreen,
  PEGI_7: styles.ageRatingBlue,
  PEGI_12: styles.ageRatingYellow,
  PEGI_16: styles.ageRatingOrange,
  PEGI_18: styles.ageRatingRed,
};

const formatDuration = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}h ${mins}m`;
};

const groupSessionsByMovie = (
  sessions: SessionScheduleResponse[],
): MovieGroup[] => {
  const groupedMap: Record<number, MovieGroup> = {};

  sessions.forEach((session) => {
    if (!groupedMap[session.movieId]) {
      groupedMap[session.movieId] = {
        movieId: session.movieId,
        movieTitle: session.movieTitle,
        movieSlug: session.movieSlug,
        movieAgeRating: session.movieAgeRating,
        movieDuration: session.movieDuration,
        sessions: [],
      };
    }
    groupedMap[session.movieId].sessions.push(session);
  });

  return Object.values(groupedMap).map((group) => ({
    ...group,
    sessions: group.sessions.sort(
      (a, b) =>
        new Date(a.startTime).getTime() - new Date(b.startTime).getTime(),
    ),
  }));
};

const SessionCard: React.FC<SessionCardProps> = ({ session, onBook }) => {
  const isAvailable = session.availableSeats > 0;

  const handleBook = () => {
    if (isAvailable) onBook(session.publicId);
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLDivElement>) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      handleBook();
    }
  };

  return (
    <div
      className={`${styles.sessionCard} ${isAvailable ? styles.available : styles.unavailable}`}
      role="button"
      tabIndex={isAvailable ? 0 : -1}
      aria-disabled={!isAvailable}
      aria-label={`${formatTime(session.startTime)}, ${session.hallName}, ${isAvailable ? "book now" : "sold out"}`}
      onClick={handleBook}
      onKeyDown={handleKeyDown}
    >
      <div className={styles.sessionTime}>
        <div className={styles.timeRange}>
          <span className={styles.startTime}>
            {formatTime(session.startTime)}
          </span>
          <span className={styles.timeSeparator}>–</span>
          <span className={styles.endTime}>{formatTime(session.endTime)}</span>
        </div>
        <span className={styles.hall}>{session.hallName}</span>
      </div>

      <div className={styles.sessionInfo}>
        <div className={styles.price}>{formatPrice(session.basePrice)}</div>
        <div className={styles.seats}>Available: {session.availableSeats}</div>
        <div className={styles.sessionStatus}>
          <span className={styles.statusBadge}>
            {isAvailable ? "Book Now" : "Sold Out"}
          </span>
        </div>
      </div>
    </div>
  );
};

export const SessionList: React.FC<SessionListProps> = ({ sessions }) => {
  const navigate = useNavigate();

  const groupedByMovie = useMemo(
    () => groupSessionsByMovie(sessions),
    [sessions],
  );

  const handleBook = useCallback(
    (sessionPublicId: string) => navigate(`/booking/${sessionPublicId}`),
    [navigate],
  );

  const handleViewDetails = useCallback(
    (movieSlug: string) => navigate(`/movies/${movieSlug}`),
    [navigate],
  );

  if (!sessions.length) return null;

  return (
    <div className={styles.container}>
      {groupedByMovie.map((movieGroup) => {
        const ageRatingKey =
          movieGroup.movieAgeRating as keyof typeof AgeRatingDisplay;
        const ageDescriptionKey =
          movieGroup.movieAgeRating as keyof typeof AgeRatingDescription;

        return (
          <div key={movieGroup.movieId} className={styles.movieGroup}>
            <div className={styles.movieHeader}>
              <div className={styles.posterContainer}>
                <img
                  src={resolvePosterUrl(
                    `/api/movies/${movieGroup.movieId}/poster`,
                  )}
                  alt={movieGroup.movieTitle}
                  className={styles.poster}
                  loading="lazy"
                  onError={(e) => {
                    const target = e.target as HTMLImageElement;
                    if (target.src !== DEFAULT_POSTER_URL) {
                      target.src = DEFAULT_POSTER_URL;
                    }
                  }}
                />
              </div>

              <div className={styles.movieInfo}>
                <div className={styles.movieHeaderRow}>
                  <h2 className={styles.movieTitle}>{movieGroup.movieTitle}</h2>
                  <Button
                    variant="secondary"
                    size="small"
                    onClick={() => handleViewDetails(movieGroup.movieSlug)}
                  >
                    Details
                  </Button>
                </div>

                <div className={styles.movieMeta}>
                  <Tooltip
                    content={
                      AgeRatingDescription[ageDescriptionKey] || "Age rating"
                    }
                    position="top"
                  >
                    <span
                      className={`${styles.ageRating} ${
                        AGE_RATING_COLORS[movieGroup.movieAgeRating] ||
                        styles.ageRatingRed
                      }`}
                    >
                      {AgeRatingDisplay[ageRatingKey]}
                    </span>
                  </Tooltip>
                  <span className={styles.duration}>
                    {formatDuration(movieGroup.movieDuration)}
                  </span>
                  <span className={styles.sessionCount}>
                    {movieGroup.sessions.length} session
                    {movieGroup.sessions.length !== 1 ? "s" : ""}
                  </span>
                </div>
              </div>
            </div>

            <div className={styles.sessionsContainer}>
              <div className={styles.sessionsGrid}>
                {movieGroup.sessions.map((session) => (
                  <SessionCard
                    key={session.id}
                    session={session}
                    onBook={handleBook}
                  />
                ))}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
