import React, { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { MovieCard } from "@/components/movies/MovieCard/MovieCard";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { MovieCardResponse } from "@/types/movie";
import styles from "./MovieRail.module.css";

interface MovieRailProps {
  title: string;
  movies: MovieCardResponse[];
  loading?: boolean;
  viewAllPath?: string;
  highlighted?: boolean;
}

const AUTO_PLAY_INTERVAL = 5000;
const SWIPE_THRESHOLD = 50;

const getInitialItemsToShow = () => {
  return window.innerWidth <= 768 ? 1 : 3;
};

export const MovieRail: React.FC<MovieRailProps> = ({
  title,
  movies,
  loading,
  viewAllPath,
  highlighted = false,
}) => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [itemsToShow, setItemsToShow] = useState(getInitialItemsToShow);
  const touchStartRef = useRef<{ x: number; y: number } | null>(null);

  useEffect(() => {
    const handleResize = () => {
      setItemsToShow(window.innerWidth <= 768 ? 1 : 3);
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const maxIndex = Math.max(0, movies.length - itemsToShow);

  const nextSlide = useCallback(() => {
    setCurrentIndex((prev) => (prev >= maxIndex ? 0 : prev + 1));
  }, [maxIndex]);

  const prevSlide = () => {
    setCurrentIndex((prev) => (prev <= 0 ? maxIndex : prev - 1));
  };

  useEffect(() => {
    if (isPaused || loading || movies.length <= itemsToShow) return;

    const timer = window.setTimeout(nextSlide, AUTO_PLAY_INTERVAL);
    return () => clearTimeout(timer);
  }, [isPaused, loading, movies.length, itemsToShow, nextSlide, currentIndex]);

  const handlePointerEnter = (event: React.PointerEvent) => {
    if (event.pointerType === "mouse") setIsPaused(true);
  };

  const handlePointerLeave = (event: React.PointerEvent) => {
    if (event.pointerType === "mouse") setIsPaused(false);
  };

  const handleTouchStart = (event: React.TouchEvent) => {
    const touch = event.touches[0];
    touchStartRef.current = { x: touch.clientX, y: touch.clientY };
  };

  const handleTouchEnd = (event: React.TouchEvent) => {
    const start = touchStartRef.current;
    touchStartRef.current = null;
    if (!start) return;

    const touch = event.changedTouches[0];
    const deltaX = touch.clientX - start.x;
    const deltaY = touch.clientY - start.y;
    if (
      Math.abs(deltaX) < SWIPE_THRESHOLD ||
      Math.abs(deltaX) < Math.abs(deltaY)
    )
      return;

    if (deltaX < 0) nextSlide();
    else prevSlide();
  };

  if (loading) {
    return (
      <section
        className={`${styles.section} ${highlighted ? styles.highlighted : ""}`}
      >
        <div className={styles.container}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>{title}</h2>
          </div>
          <LoadingSpinner text="Loading movies..." />
        </div>
      </section>
    );
  }

  if (!movies.length) return null;

  const showCarousel = movies.length > itemsToShow;
  const visibleMovies = showCarousel
    ? movies.slice(currentIndex, currentIndex + itemsToShow)
    : movies;

  return (
    <section
      className={`${styles.section} ${highlighted ? styles.highlighted : ""}`}
    >
      <div className={styles.container}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>{title}</h2>
          {viewAllPath && (
            <Button
              variant="outline"
              size="small"
              onClick={() => navigate(viewAllPath)}
            >
              View All
            </Button>
          )}
        </div>

        <div
          className={styles.carouselContainer}
          onPointerEnter={handlePointerEnter}
          onPointerLeave={handlePointerLeave}
        >
          {showCarousel && (
            <Button
              variant="outline"
              size="small"
              className={styles.navButton}
              onClick={prevSlide}
              aria-label="Previous"
            >
              <ChevronLeft size={20} />
            </Button>
          )}

          <div
            className={styles.carouselWrapper}
            onTouchStart={showCarousel ? handleTouchStart : undefined}
            onTouchEnd={showCarousel ? handleTouchEnd : undefined}
          >
            <div
              className={`${styles.moviesGrid} ${!showCarousel ? styles.moviesGridCentered : ""}`}
            >
              {visibleMovies.map((movie) => (
                <div key={movie.id} className={styles.slideItem}>
                  <MovieCard movie={movie} />
                </div>
              ))}
            </div>
          </div>

          {showCarousel && (
            <Button
              variant="outline"
              size="small"
              className={styles.navButton}
              onClick={nextSlide}
              aria-label="Next"
            >
              <ChevronRight size={20} />
            </Button>
          )}
        </div>

        {showCarousel && (
          <div className={styles.dots}>
            {Array.from({ length: maxIndex + 1 }).map((_, idx) => (
              <button
                key={idx}
                className={`${styles.dot} ${currentIndex === idx ? styles.dotActive : ""}`}
                onClick={() => setCurrentIndex(idx)}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  );
};
