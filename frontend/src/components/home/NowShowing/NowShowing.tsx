import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { MovieCard } from "@/components/movies/MovieCard/MovieCard";
import { Button } from "@/components/ui/Button/Button";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import type { MovieCardResponse } from "@/types/movie";
import styles from "./NowShowing.module.css";

interface NowShowingProps {
  movies: MovieCardResponse[];
  loading?: boolean;
}

const AUTO_PLAY_INTERVAL = 5000;

export const NowShowing: React.FC<NowShowingProps> = ({ movies, loading }) => {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isHovered, setIsHovered] = useState(false);
  const [itemsToShow, setItemsToShow] = useState(3);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth <= 768) {
        setItemsToShow(1);
      } else {
        setItemsToShow(3);
      }
    };

    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const maxIndex = Math.max(0, movies.length - itemsToShow);

  const nextSlide = () => {
    setCurrentIndex((prev) => (prev >= maxIndex ? 0 : prev + 1));
  };

  const prevSlide = () => {
    setCurrentIndex((prev) => (prev <= 0 ? maxIndex : prev - 1));
  };

  useEffect(() => {
    if (isHovered || loading || movies.length <= itemsToShow) return;

    timerRef.current = window.setInterval(nextSlide, AUTO_PLAY_INTERVAL);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [isHovered, loading, movies.length, maxIndex, itemsToShow]);

  if (loading) {
    return (
      <section className={styles.section}>
        <div className={styles.container}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>Now Showing</h2>
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
      className={styles.section}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className={styles.container}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>Now Showing</h2>
          <Button
            variant="outline"
            size="small"
            onClick={() => navigate("/movies/current")}
          >
            View All
          </Button>
        </div>

        <div className={styles.carouselContainer}>
          {showCarousel && (
            <Button
              variant="outline"
              size="small"
              className={styles.navButton}
              onClick={prevSlide}
            >
              &#10094;
            </Button>
          )}

          <div className={styles.carouselWrapper}>
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
            >
              &#10095;
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
