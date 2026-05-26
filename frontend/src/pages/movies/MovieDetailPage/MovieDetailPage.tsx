import React, { useState, useEffect, useCallback, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useMovies } from "@/hooks/features/movies/useMovies";
import type { SessionMovieInfoResponse } from "@/types/session";
import {
  AgeRatingDisplay,
  AgeRatingDescription,
  MovieStatusDisplay,
} from "@/types/movie";
import { Button } from "@/components/ui/Button/Button";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { Layout } from "@/components/layout/Layout/Layout";
import { SessionSection } from "@/components/movies/SessionSection/SessionSection";
import styles from "./MovieDetailPage.module.css";

const AGE_RATING_COLORS: Record<string, string> = {
  PEGI_3: styles.ageRatingGreen,
  PEGI_7: styles.ageRatingBlue,
  PEGI_12: styles.ageRatingYellow,
  PEGI_16: styles.ageRatingOrange,
  PEGI_18: styles.ageRatingRed,
};

const DEFAULT_POSTER = "/images/default-movie-poster.svg";
const DATES_PER_VIEW = 5;

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

const getAgeRatingClass = (ageRating: string): string => {
  return AGE_RATING_COLORS[ageRating] || styles.ageRatingRed;
};

const groupSessionsByDate = (
  sessions: SessionMovieInfoResponse[],
): { dates: string[]; grouped: Record<string, SessionMovieInfoResponse[]> } => {
  const grouped: Record<string, SessionMovieInfoResponse[]> = {};

  sessions.forEach((session) => {
    const date = session.startTime.split("T")[0];
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(session);
  });

  const dates = Object.keys(grouped).sort();
  return { dates, grouped };
};

const getPosterUrl = (url: string | undefined | null): string => {
  if (!url || url.trim() === "") return DEFAULT_POSTER;
  return url;
};

export const MovieDetailPage: React.FC = () => {
  const { slug } = useParams<{ slug: string }>();
  const navigate = useNavigate();
  const { movieDetail, loading, getBySlug } = useMovies();

  const [selectedDate, setSelectedDate] = useState<string | null>(null);
  const [dateList, setDateList] = useState<string[]>([]);
  const [sessionsByDate, setSessionsByDate] = useState<
    Record<string, SessionMovieInfoResponse[]>
  >({});
  const [dateScrollIndex, setDateScrollIndex] = useState(0);

  useEffect(() => {
    if (slug) {
      getBySlug(slug);
    }
  }, [slug, getBySlug]);

  useEffect(() => {
    if (movieDetail?.sessions?.length) {
      const { dates, grouped } = groupSessionsByDate(movieDetail.sessions);
      setDateList(dates);
      setSessionsByDate(grouped);
      setSelectedDate(dates[0]);
    }
  }, [movieDetail]);

  const handleScrollDates = useCallback(
    (direction: "left" | "right") => {
      const maxIndex = Math.max(0, dateList.length - DATES_PER_VIEW);
      setDateScrollIndex((prev) => {
        if (direction === "left") return Math.max(0, prev - 1);
        return Math.min(maxIndex, prev + 1);
      });
    },
    [dateList.length],
  );

  const posterUrl = useMemo(
    () => getPosterUrl(movieDetail?.posterUrl),
    [movieDetail?.posterUrl],
  );

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner text="Loading movie details..." />
      </Layout>
    );
  }

  if (!movieDetail) {
    return (
      <Layout>
        <div className={styles.errorContainer}>
          <h2>Movie not found</h2>
          <p>The movie you're looking for doesn't exist or has been removed.</p>
          <Button
            variant="primary"
            onClick={() => navigate("/movies/currently-showing")}
          >
            Browse Current Movies
          </Button>
        </div>
      </Layout>
    );
  }

  const ageDescriptionKey =
    movieDetail.ageRating as keyof typeof AgeRatingDescription;

  return (
    <Layout>
      <div className={styles.container}>
        <div className={styles.content}>
          <div className={styles.leftColumn}>
            <div className={styles.posterContainer}>
              <img
                src={posterUrl}
                alt={movieDetail.title}
                className={styles.poster}
                onError={(e) => {
                  const target = e.target as HTMLImageElement;
                  if (target.src !== DEFAULT_POSTER) {
                    target.src = DEFAULT_POSTER;
                  }
                }}
              />
              <div className={styles.statusBadge}>
                <span
                  className={`${styles.status} ${styles[movieDetail.status.toLowerCase()]}`}
                >
                  {MovieStatusDisplay[movieDetail.status]}
                </span>
              </div>
            </div>

            {movieDetail.trailerUrl && (
              <div className={styles.actionButtons}>
                <Button
                  variant="secondary"
                  onClick={() => window.open(movieDetail.trailerUrl, "_blank")}
                  className={styles.trailerButton}
                >
                  Watch Trailer
                </Button>
              </div>
            )}
          </div>

          <div className={styles.rightColumn}>
            <div className={styles.titleSection}>
              <div className={styles.titleContent}>
                <h1 className={styles.title}>{movieDetail.title}</h1>
                <div className={styles.metaInfo}>
                  <Tooltip
                    content={
                      AgeRatingDescription[ageDescriptionKey] || "Age rating"
                    }
                    position="top"
                  >
                    <div
                      className={`${styles.ageRating} ${getAgeRatingClass(movieDetail.ageRating)}`}
                    >
                      {AgeRatingDisplay[movieDetail.ageRating]}
                    </div>
                  </Tooltip>
                  <span className={styles.duration}>
                    {movieDetail.durationMinutes} min
                  </span>
                </div>
              </div>
            </div>

            <div className={styles.infoSection}>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>Release Date:</span>
                <div className={styles.infoValue}>
                  {formatDate(movieDetail.releaseDate)}
                </div>
              </div>

              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>End Showing:</span>
                <div className={styles.infoValue}>
                  {formatDate(movieDetail.endShowingDate)}
                </div>
              </div>

              {movieDetail.genres?.length > 0 && (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>Genres:</span>
                  <div className={styles.infoValue}>
                    <div className={styles.genreList}>
                      {movieDetail.genres.map((genre) => (
                        <span key={genre.id} className={styles.genreItem}>
                          {genre.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {movieDetail.directors?.length > 0 && (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>
                    Director{movieDetail.directors.length > 1 ? "s" : ""}:
                  </span>
                  <div className={styles.infoValue}>
                    <div className={styles.peopleList}>
                      {movieDetail.directors.map((person) => (
                        <span key={person.id} className={styles.personItem}>
                          {person.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {movieDetail.screenwriters?.length > 0 && (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>
                    Screenwriter
                    {movieDetail.screenwriters.length > 1 ? "s" : ""}:
                  </span>
                  <div className={styles.infoValue}>
                    <div className={styles.peopleList}>
                      {movieDetail.screenwriters.map((person) => (
                        <span key={person.id} className={styles.personItem}>
                          {person.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {movieDetail.actors?.length > 0 && (
                <div className={styles.infoRow}>
                  <span className={styles.infoLabel}>Cast:</span>
                  <div className={styles.infoValue}>
                    <div className={styles.peopleList}>
                      {movieDetail.actors.map((person) => (
                        <span key={person.id} className={styles.personItem}>
                          {person.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className={styles.descriptionSection}>
              <h2 className={styles.sectionTitle}>Overview</h2>
              <p className={styles.description}>{movieDetail.description}</p>
            </div>

            <SessionSection
              dateList={dateList}
              sessionsByDate={sessionsByDate}
              selectedDate={selectedDate}
              onDateSelect={setSelectedDate}
              dateScrollIndex={dateScrollIndex}
              datesPerView={DATES_PER_VIEW}
              onScrollDates={handleScrollDates}
            />
          </div>
        </div>
      </div>
    </Layout>
  );
};
