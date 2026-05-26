import React, { useState, useEffect, useRef, useCallback } from "react";
import { movieApi } from "@/api/movieApi";
import { Check } from "lucide-react";
import type { MovieSessionSearchResponse } from "@/types/movie";
import styles from "./MovieFilter.module.css";

interface MovieFilterProps {
  selectedMovieId: number | undefined;
  onMovieChange: (movieId: number | undefined) => void;
}

const MIN_SEARCH_LENGTH = 2;
const DEBOUNCE_MS = 300;

type SearchStatus = "idle" | "loading" | "results" | "empty";

export const MovieFilter: React.FC<MovieFilterProps> = ({
  selectedMovieId,
  onMovieChange,
}) => {
  const [movies, setMovies] = useState<MovieSessionSearchResponse[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [status, setStatus] = useState<SearchStatus>("idle");
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

  const searchMovies = useCallback(async (query: string) => {
    if (query.trim().length < MIN_SEARCH_LENGTH) {
      setMovies([]);
      setStatus("idle");
      return;
    }

    setStatus("loading");
    try {
      const response = await movieApi.public.search(query);
      const data = response?.data || [];
      setMovies(data);
      setStatus(data.length > 0 ? "results" : "empty");
      setShowDropdown(true);
    } catch {
      setMovies([]);
      setStatus("empty");
    }
  }, []);

  const debouncedSearch = useCallback(
    (query: string) => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      timeoutRef.current = window.setTimeout(
        () => searchMovies(query),
        DEBOUNCE_MS,
      );
    },
    [searchMovies],
  );

  const resetSearch = useCallback(() => {
    if (timeoutRef.current) clearTimeout(timeoutRef.current);
    setSearchTerm("");
    setMovies([]);
    setStatus("idle");
    setShowDropdown(false);
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);
    setShowDropdown(true);

    if (!value.trim()) {
      resetSearch();
      onMovieChange(undefined);
    } else if (value.trim().length >= MIN_SEARCH_LENGTH) {
      debouncedSearch(value);
    } else {
      setMovies([]);
      setStatus("idle");
    }
  };

  const handleMovieSelect = (movie: MovieSessionSearchResponse) => {
    setSearchTerm(movie.title);
    onMovieChange(movie.id);
    setShowDropdown(false);
    setMovies([]);
    setStatus("idle");
  };

  const handleClearSelection = () => {
    resetSearch();
    onMovieChange(undefined);
  };

  const isLoading = status === "loading";
  const showResults = showDropdown && status === "results" && movies.length > 0;
  const showNoResults = showDropdown && status === "empty";

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
          {isLoading && (
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
              className={`${styles.movieOption} ${
                selectedMovieId === movie.id ? styles.selected : ""
              }`}
            >
              <div className={styles.movieContent}>
                <span className={styles.movieTitle}>{movie.title}</span>
                <span className={styles.movieDuration}>
                  {movie.durationMinutes} min
                </span>
              </div>
              {selectedMovieId === movie.id && (
                <Check size={16} className={styles.checkmark} />
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
