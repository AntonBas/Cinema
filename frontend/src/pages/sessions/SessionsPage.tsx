import React, { useEffect, useState, useCallback, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { useSession } from "@/hooks/features/sessions/useSession";
import type { SessionScheduleResponse } from "@/types/session";
import { Layout } from "@/components/layout/Layout/Layout";
import { DateFilter } from "@/components/sessions/DateFilter/DateFilter";
import { MovieFilter } from "@/components/sessions/MovieFilter/MovieFilter";
import { SessionList } from "@/components/sessions/SessionList/SessionList";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { Button } from "@/components/ui/Button/Button";
import styles from "./SessionsPage.module.css";

const getTodayString = (): string => new Date().toISOString().split("T")[0];

const extractUniqueDates = (sessions: SessionScheduleResponse[]): string[] => {
  const dates = sessions.map((s) => s.startTime.split("T")[0]);
  return [...new Set(dates)].sort();
};

const filterByDate = (
  sessions: SessionScheduleResponse[],
  date: string,
): SessionScheduleResponse[] => {
  return sessions.filter((s) => s.startTime.startsWith(date));
};

const SessionsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { loading, getSchedule } = useSession();

  const [allSessions, setAllSessions] = useState<SessionScheduleResponse[]>([]);
  const [allSessionDates, setAllSessionDates] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  const today = useMemo(() => getTodayString(), []);
  const dateParam = searchParams.get("date");
  const movieIdParam = searchParams.get("movieId");
  const selectedDate = dateParam || today;
  const selectedMovieId = movieIdParam ? parseInt(movieIdParam) : undefined;

  useEffect(() => {
    const fetchSessions = async () => {
      setError(null);

      try {
        const data = await getSchedule({ movieId: selectedMovieId });
        const sessionList = data || [];
        setAllSessions(sessionList);
        setAllSessionDates(extractUniqueDates(sessionList));
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load sessions",
        );
        setAllSessions([]);
        setAllSessionDates([]);
      }
    };

    fetchSessions();
  }, [selectedMovieId]);

  const sessions = useMemo(
    () => filterByDate(allSessions, selectedDate),
    [allSessions, selectedDate],
  );

  const handleDateChange = useCallback(
    (date: string) => {
      const params = new URLSearchParams(searchParams);
      params.set("date", date);
      setSearchParams(params);
    },
    [searchParams, setSearchParams],
  );

  const handleMovieChange = useCallback(
    (movieId: number | undefined) => {
      const params = new URLSearchParams(searchParams);
      if (movieId) {
        params.set("movieId", movieId.toString());
      } else {
        params.delete("movieId");
      }
      setSearchParams(params);
    },
    [searchParams, setSearchParams],
  );

  const handleClearFilters = useCallback(() => {
    setSearchParams(new URLSearchParams());
  }, [setSearchParams]);

  const hasFilters = selectedMovieId !== undefined || selectedDate !== today;
  const uniqueMoviesCount = new Set(sessions.map((s) => s.movieId)).size;

  return (
    <Layout>
      <div className={styles.container}>
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>Movie Sessions</h1>
            <p className={styles.subtitle}>
              Browse available movie sessions and book your tickets
            </p>
          </div>
          {hasFilters && (
            <Button variant="secondary" onClick={handleClearFilters}>
              Clear Filters
            </Button>
          )}
        </div>

        <div className={styles.filtersSection}>
          <DateFilter
            selectedDate={selectedDate}
            onDateChange={handleDateChange}
            sessionDates={allSessionDates}
          />
          <MovieFilter
            selectedMovieId={selectedMovieId}
            onMovieChange={handleMovieChange}
          />
        </div>

        {error ? (
          <div className={styles.errorContainer}>
            <p className={styles.errorText}>{error}</p>
            <Button variant="primary" onClick={() => window.location.reload()}>
              Try Again
            </Button>
          </div>
        ) : loading ? (
          <LoadingSpinner text="Loading sessions..." />
        ) : sessions.length > 0 ? (
          <>
            <div className={styles.resultsHeader}>
              <span className={styles.resultsCount}>
                {uniqueMoviesCount} movie{uniqueMoviesCount !== 1 ? "s" : ""} •{" "}
                {sessions.length} session{sessions.length !== 1 ? "s" : ""}
              </span>
            </div>
            <SessionList sessions={sessions} />
          </>
        ) : (
          <div className={styles.emptyState}>
            <h3>No sessions found</h3>
            <p>Try selecting a different date or movie.</p>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default SessionsPage;
