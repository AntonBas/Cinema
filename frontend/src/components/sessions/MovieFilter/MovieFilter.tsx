import React, { useState, useEffect, useCallback, useRef } from 'react';
import { movieApi } from '@/api/movieApi';
import type { MovieSessionSearchResponse } from '@/types/movie';
import styles from './MovieFilter.module.css';

interface MovieFilterProps {
    selectedMovieId: number | undefined;
    onMovieChange: (movieId: number | undefined) => void;
}

export const MovieFilter: React.FC<MovieFilterProps> = ({ selectedMovieId, onMovieChange }) => {
    const [movies, setMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [showDropdown, setShowDropdown] = useState(false);
    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(null);
    const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const searchMovies = useCallback(async (query: string) => {
        if (!query.trim()) {
            setMovies([]);
            return;
        }

        setLoading(true);
        try {
            const response = await movieApi.admin.searchMoviesForSession(query);
            const results = response?.data || [];
            setMovies(results);
        } catch (error) {
            console.error('Error searching movies:', error);
            setMovies([]);
        } finally {
            setLoading(false);
        }
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

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
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
        } else {
            searchTimeoutRef.current = setTimeout(() => {
                searchMovies(value);
            }, 300);
        }
    };

    const handleMovieSelect = (movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setSearchTerm(movie.title);
        onMovieChange(movie.id);
        setShowDropdown(false);
        setMovies([]);
    };

    const handleClearSelection = () => {
        setSelectedMovie(null);
        setSearchTerm('');
        onMovieChange(undefined);
        setShowDropdown(false);
        setMovies([]);
    };

    const handleInputBlur = () => {
        setTimeout(() => {
            setShowDropdown(false);
        }, 200);
    };

    return (
        <div className={styles.container}>
            <div className={styles.filterHeader}>
                <h3 className={styles.title}>Movie</h3>
                {selectedMovie && (
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
                    <span className={styles.searchIcon}>🎬</span>
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={handleInputChange}
                        onFocus={() => setShowDropdown(true)}
                        onBlur={handleInputBlur}
                        placeholder="Search for a movie..."
                        className={styles.searchInput}
                    />
                    {loading && (
                        <div className={styles.loadingIndicator}>
                            <div className={styles.spinner}></div>
                        </div>
                    )}
                </div>
            </div>

            {showDropdown && searchTerm && movies.length > 0 && (
                <div className={styles.dropdown}>
                    {movies.map(movie => (
                        <button
                            key={movie.id}
                            type="button"
                            onClick={() => handleMovieSelect(movie)}
                            className={`${styles.movieOption} ${selectedMovie?.id === movie.id ? styles.selected : ''}`}
                        >
                            <div className={styles.movieContent}>
                                <span className={styles.movieTitle}>{movie.title}</span>
                                <div className={styles.movieMeta}>
                                    <span className={styles.movieDuration}>{movie.durationMinutes} min</span>
                                </div>
                            </div>
                            {selectedMovie?.id === movie.id && (
                                <span className={styles.checkmark}>✓</span>
                            )}
                        </button>
                    ))}
                </div>
            )}

            {showDropdown && searchTerm && !loading && movies.length === 0 && (
                <div className={styles.noResults}>
                    No movies found
                </div>
            )}
        </div>
    );
};