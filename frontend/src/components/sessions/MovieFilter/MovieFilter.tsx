import React, { useState, useEffect, useCallback, useRef } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieSessionSearchResponse } from '@/types/movie';
import styles from './MovieFilter.module.css';

interface MovieFilterProps {
    selectedMovieId: number | undefined;
    onMovieChange: (movieId: number | undefined) => void;
}

const SEARCH_DELAY = 300;
const MIN_SEARCH_LENGTH = 2;

export const MovieFilter: React.FC<MovieFilterProps> = ({ selectedMovieId, onMovieChange }) => {
    const [movies, setMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [showDropdown, setShowDropdown] = useState(false);
    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(null);
    const searchTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const searchMovies = useCallback(async (query: string) => {
        if (!query.trim() || query.trim().length < MIN_SEARCH_LENGTH) {
            setMovies([]);
            return;
        }

        setLoading(true);
        try {
            const response = await movieApi.admin.searchMoviesForSession(query);
            const results = response?.data || [];
            setMovies(results);
            setShowDropdown(results.length > 0);
        } catch (error) {
            console.error('Error searching movies:', error);
            setMovies([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setShowDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (selectedMovieId && selectedMovie?.id !== selectedMovieId) {
            const fetchSelectedMovie = async () => {
                try {
                    const response = await movieApi.admin.searchMoviesForSession('');
                    const results = response?.data || [];
                    const movie = results.find((m: MovieSessionSearchResponse) => m.id === selectedMovieId);
                    if (movie) {
                        setSelectedMovie(movie);
                        setSearchTerm(movie.title);
                    }
                } catch (error) {
                    console.error('Error fetching selected movie:', error);
                }
            };
            fetchSelectedMovie();
        }
    }, [selectedMovieId, selectedMovie]);

    const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setSearchTerm(value);
        setShowDropdown(true);

        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        if (!value.trim()) {
            setMovies([]);
            if (selectedMovie) {
                setSelectedMovie(null);
                onMovieChange(undefined);
            }
            setShowDropdown(false);
        } else if (value.trim().length >= MIN_SEARCH_LENGTH) {
            searchTimeoutRef.current = setTimeout(() => {
                searchMovies(value);
            }, SEARCH_DELAY);
        } else {
            setMovies([]);
        }
    }, [selectedMovie, onMovieChange, searchMovies]);

    const handleMovieSelect = useCallback((movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setSearchTerm(movie.title);
        onMovieChange(movie.id);
        setShowDropdown(false);
        setMovies([]);
    }, [onMovieChange]);

    const handleClearSelection = useCallback(() => {
        setSelectedMovie(null);
        setSearchTerm('');
        onMovieChange(undefined);
        setShowDropdown(false);
        setMovies([]);
    }, [onMovieChange]);

    const handleInputFocus = useCallback(() => {
        if (searchTerm.trim().length >= MIN_SEARCH_LENGTH) {
            searchMovies(searchTerm);
        }
    }, [searchTerm, searchMovies]);

    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    const showResults = showDropdown && searchTerm.trim().length >= MIN_SEARCH_LENGTH && !loading && movies.length > 0;
    const showNoResults = showDropdown && searchTerm.trim().length >= MIN_SEARCH_LENGTH && !loading && movies.length === 0;

    return (
        <div className={styles.container} ref={dropdownRef}>
            <div className={styles.filterHeader}>
                <h3 className={styles.title}>Movie</h3>
                {selectedMovie && (
                    <button
                        type="button"
                        onClick={handleClearSelection}
                        className={styles.clearButton}
                        aria-label="Clear selected movie"
                    >
                        Clear
                    </button>
                )}
            </div>

            <div className={styles.searchContainer}>
                <div className={styles.searchWrapper}>
                    <span className={styles.searchIcon} aria-hidden="true">🎬</span>
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={handleInputChange}
                        onFocus={handleInputFocus}
                        placeholder="Search for a movie..."
                        className={styles.searchInput}
                        aria-label="Search movies"
                    />
                    {loading && (
                        <div className={styles.loadingIndicator} aria-label="Loading">
                            <div className={styles.spinner}></div>
                        </div>
                    )}
                </div>
            </div>

            {showResults && (
                <div className={styles.dropdown} role="listbox">
                    {movies.map(movie => (
                        <button
                            key={movie.id}
                            type="button"
                            onClick={() => handleMovieSelect(movie)}
                            className={`${styles.movieOption} ${selectedMovie?.id === movie.id ? styles.selected : ''}`}
                            role="option"
                            aria-selected={selectedMovie?.id === movie.id}
                        >
                            <div className={styles.movieContent}>
                                <span className={styles.movieTitle}>{movie.title}</span>
                                <div className={styles.movieMeta}>
                                    <span className={styles.movieDuration}>{movie.durationMinutes} min</span>
                                </div>
                            </div>
                            {selectedMovie?.id === movie.id && (
                                <span className={styles.checkmark} aria-hidden="true">✓</span>
                            )}
                        </button>
                    ))}
                </div>
            )}

            {showNoResults && (
                <div className={styles.noResults} role="status">
                    No movies found for "{searchTerm}"
                </div>
            )}
        </div>
    );
};