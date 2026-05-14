import React, { useState, useEffect, useRef } from "react";
import { movieApi } from "@/api/movieApi";
import type { MovieSessionSearchResponse } from "@/types/movie";
import styles from "./MovieFilter.module.css";

interface MovieFilterProps {
  selectedMovieId: number | undefined;
  onMovieChange: (movieId: number | undefined) => void;
}

const MIN_SEARCH_LENGTH = 2;

export const MovieFilter: React.FC<MovieFilterProps> = ({
  selectedMovieId,
  onMovieChange,
}) => {
  const [movies, setMovies] = useState<MovieSessionSearchResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [showDropdown, setShowDropdown] = useState(false);
  const timeoutRef = useRef<number | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setShowDropdown(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  useEffect(() => {
    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, []);

  const searchMovies = async (query: string) => {
    if (!query.trim() || query.trim().length < MIN_SEARCH_LENGTH) {
      setMovies([]);
      return;
    }

    setLoading(true);
    try {
      const response = await movieApi.public.search(query);
      setMovies(response?.data || []);
      setShowDropdown(true);
    } catch {
      setMovies([]);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    setShowDropdown(true);

    if (timeoutRef.current) clearTimeout(timeoutRef.current);

    if (!value.trim()) {
      setMovies([]);
      onMovieChange(undefined);
      setShowDropdown(false);
    } else if (value.trim().length >= MIN_SEARCH_LENGTH) {
      timeoutRef.current = window.setTimeout(() => searchMovies(value), 300);
    } else {
      setMovies([]);
    }
  };

  const handleMovieSelect = (movie: MovieSessionSearchResponse) => {
    setSearchTerm(movie.title);
    onMovieChange(movie.id);
    setShowDropdown(false);
    setMovies([]);
  };

  const handleClearSelection = () => {
    setSearchTerm("");
    onMovieChange(undefined);
    setShowDropdown(false);
    setMovies([]);
  };

  const showResults =
    showDropdown &&
    searchTerm.length >= MIN_SEARCH_LENGTH &&
    !loading &&
    movies.length > 0;
  const showNoResults =
    showDropdown &&
    searchTerm.length >= MIN_SEARCH_LENGTH &&
    !loading &&
    movies.length === 0;

  return (
    <div className={styles.container} ref={dropdownRef}>
      <div className={styles.filterHeader}>
        <h3 className={styles.title}>Movie</h3>
        {selectedMovieId && (
          <button
            type="button"
            onClick={handleClearSelection}
            className={styles.clearButton}
          >
            Clear
          </button>
        )}
      </div>

      <div className={styles.searchContainer}>
        <div className={styles.searchWrapper}>
          <input
            type="text"
            value={searchTerm}
            onChange={handleInputChange}
            placeholder="Search for a movie..."
            className={styles.searchInput}
          />
          {loading && (
            <div className={styles.loadingIndicator}>
              <div className={styles.spinner} />
            </div>
          )}
        </div>
      </div>

      {showResults && (
        <div className={styles.dropdown}>
          {movies.map((movie) => (
            <button
              key={movie.id}
              type="button"
              onClick={() => handleMovieSelect(movie)}
              className={`${styles.movieOption} ${selectedMovieId === movie.id ? styles.selected : ""}`}
            >
              <div className={styles.movieContent}>
                <span className={styles.movieTitle}>{movie.title}</span>
                <span className={styles.movieDuration}>
                  {movie.durationMinutes} min
                </span>
              </div>
              {selectedMovieId === movie.id && (
                <span className={styles.checkmark}>✓</span>
              )}
            </button>
          ))}
        </div>
      )}

      {showNoResults && (
        <div className={styles.noResults}>
          No movies found for "{searchTerm}"
        </div>
      )}
    </div>
  );
};
