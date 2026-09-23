import React, { useEffect, useState, useCallback, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { useSession } from "@/hooks/features/session/useSession";
import type { SessionScheduleResponse } from "@/types/session";
import { Layout } from "@/components/layout/Layout/Layout";
import { DateFilter } from "@/components/sessions/DateFilter/DateFilter";
import { MovieFilter } from "@/components/sessions/MovieFilter/MovieFilter";
import { SessionList } from "@/components/sessions/SessionList/SessionList";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { Button } from "@/components/ui/Button/Button";
import { PageContainer } from "@/components/ui/PageContainer/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./SessionsPage.module.css";

const getTodayString = (): string => new Date().toISOString().split("T")[0];

export const SessionsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { loading, getSchedule, getScheduleDates } = useSession();

  const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
  const [sessionDates, setSessionDates] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  const today = useMemo(() => getTodayString(), []);
  const dateParam = searchParams.get("date");
  const movieIdParam = searchParams.get("movieId");
  const selectedDate = dateParam || today;
  const selectedMovieId = movieIdParam ? parseInt(movieIdParam) : undefined;

  useEffect(() => {
    let isCurrent = true;

    const fetchSessionDates = async () => {
      try {
        const dates = await getScheduleDates(selectedMovieId);
        if (isCurrent) setSessionDates(dates || []);
      } catch {
        if (isCurrent) setSessionDates([]);
      }
    };

    fetchSessionDates();
    return () => {
      isCurrent = false;
    };
  }, [selectedMovieId, getScheduleDates]);

  useEffect(() => {
    let isCurrent = true;

    const fetchSessions = async () => {
      setError(null);

      try {
        const data = await getSchedule({
          date: selectedDate,
          movieId: selectedMovieId,
        });
        if (isCurrent) setSessions(data || []);
      } catch (err) {
        if (!isCurrent) return;
        setError(
          err instanceof Error ? err.message : "Failed to Load Sessions",
        );
        setSessions([]);
      }
    };

    fetchSessions();
    return () => {
      isCurrent = false;
    };
  }, [selectedDate, selectedMovieId, getSchedule]);

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
      <PageContainer>
        <PageHeader
          title="Schedule"
          subtitle="Browse available movie sessions and book your tickets"
          actions={
            hasFilters && (
              <Button variant="secondary" onClick={handleClearFilters}>
                Clear Filters
              </Button>
            )
          }
        />

        <div className={styles.filtersSection}>
          <DateFilter
            selectedDate={selectedDate}
            onDateChange={handleDateChange}
            sessionDates={sessionDates}
          />
          <MovieFilter
            selectedMovieId={selectedMovieId}
            onMovieChange={handleMovieChange}
          />
        </div>

        {error ? (
          <EmptyState
            variant="error"
            title="Failed to Load Sessions"
            message={error}
            action={
              <Button
                variant="primary"
                onClick={() => window.location.reload()}
              >
                Try Again
              </Button>
            }
          />
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
          <EmptyState
            title="No Sessions Found"
            message="Try selecting a different date or movie."
          />
        )}
      </PageContainer>
    </Layout>
  );
};
