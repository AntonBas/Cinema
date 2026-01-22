import React, { useState, useEffect, useCallback } from 'react';
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

    const searchMovies = useCallback(async (query: string) => {
        if (!query.trim()) {
            setMovies([]);
            return;
        }

        setLoading(true);
        try {
            const today = new Date().toISOString().split('T')[0];
            const results = await movieApi.admin.searchForSession(today, query);
            setMovies(results);
        } catch (error) {
            console.error('Error searching movies:', error);
            setMovies([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            searchMovies(searchTerm);
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [searchTerm, searchMovies]);

    useEffect(() => {
        if (selectedMovieId && movies.length > 0) {
            const movie = movies.find(m => m.id === selectedMovieId);
            if (movie) {
                setSelectedMovie(movie);
                setSearchTerm(movie.title);
            }
        }
    }, [selectedMovieId, movies]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSearchTerm(e.target.value);
        setShowDropdown(true);
        if (!e.target.value.trim()) {
            setSelectedMovie(null);
            onMovieChange(undefined);
        }
    };

    const handleMovieSelect = (movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setSearchTerm(movie.title);
        onMovieChange(movie.id);
        setShowDropdown(false);
    };

    const handleClearSelection = () => {
        setSelectedMovie(null);
        setSearchTerm('');
        onMovieChange(undefined);
        setShowDropdown(false);
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h3 className={styles.title}>Filter by Movie</h3>
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
                <input
                    type="text"
                    value={searchTerm}
                    onChange={handleInputChange}
                    onFocus={() => setShowDropdown(true)}
                    placeholder="Search movies by title..."
                    className={styles.searchInput}
                />

                {loading && (
                    <div className={styles.loadingIndicator}>Searching...</div>
                )}
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
                            <div className={styles.movieInfo}>
                                <span className={styles.movieTitle}>{movie.title}</span>
                                <span className={styles.movieYear}>({movie.releaseYear})</span>
                            </div>
                            <div className={styles.movieMeta}>
                                <span className={styles.movieDuration}>{movie.durationMinutes} min</span>
                            </div>
                        </button>
                    ))}
                </div>
            )}

            {showDropdown && searchTerm && !loading && movies.length === 0 && (
                <div className={styles.noResults}>
                    No movies found matching "{searchTerm}"
                </div>
            )}
        </div>
    );
};