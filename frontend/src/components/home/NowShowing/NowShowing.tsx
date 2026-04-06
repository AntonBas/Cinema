import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { MovieCard } from '@/components/movies/MovieCard/MovieCard';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { MovieCardResponse } from '@/types/movie';
import styles from './NowShowing.module.css';

interface NowShowingProps {
    movies: MovieCardResponse[];
    loading?: boolean;
}

export const NowShowing: React.FC<NowShowingProps> = ({ movies, loading }) => {
    const navigate = useNavigate();
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const itemsToShow = 3;

    const maxIndex = Math.max(0, movies.length - itemsToShow);

    const nextSlide = useCallback(() => {
        setCurrentIndex((prev) => (prev >= maxIndex ? 0 : prev + 1));
    }, [maxIndex]);

    const prevSlide = useCallback(() => {
        setCurrentIndex((prev) => (prev <= 0 ? maxIndex : prev - 1));
    }, [maxIndex]);

    const goToSlide = useCallback((index: number) => {
        setCurrentIndex(Math.min(Math.max(0, index), maxIndex));
    }, [maxIndex]);

    useEffect(() => {
        if (isHovered || loading || movies.length <= itemsToShow) return;

        if (timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }

        timerRef.current = setInterval(() => {
            nextSlide();
        }, 5000);

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
                timerRef.current = null;
            }
        };
    }, [isHovered, loading, movies.length, nextSlide]);

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

    if (movies.length === 0) {
        return null;
    }

    const visibleMovies = movies.slice(currentIndex, currentIndex + itemsToShow);

    return (
        <section
            className={styles.section}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
        >
            <div className={styles.container}>
                <div className={styles.sectionHeader}>
                    <h2 className={styles.sectionTitle}>Now Showing</h2>
                    <Button variant="outline" size="small" onClick={() => navigate('/movies/current')}>
                        View All
                    </Button>
                </div>

                <div className={styles.carouselContainer}>
                    {movies.length > itemsToShow && (
                        <Button
                            variant="outline"
                            size="small"
                            className={styles.navButton}
                            onClick={prevSlide}
                            aria-label="Previous movies"
                        >
                            &#10094;
                        </Button>
                    )}

                    <div className={styles.carouselWrapper}>
                        <div className={styles.moviesGrid}>
                            {visibleMovies.map((movie) => (
                                <div key={movie.id} className={styles.slideItem}>
                                    <MovieCard movie={movie} />
                                </div>
                            ))}
                        </div>
                    </div>

                    {movies.length > itemsToShow && (
                        <Button
                            variant="outline"
                            size="small"
                            className={styles.navButton}
                            onClick={nextSlide}
                            aria-label="Next movies"
                        >
                            &#10095;
                        </Button>
                    )}
                </div>

                {movies.length > itemsToShow && (
                    <div className={styles.dots}>
                        {Array.from({ length: maxIndex + 1 }).map((_, idx) => (
                            <button
                                key={idx}
                                className={`${styles.dot} ${currentIndex === idx ? styles.dotActive : ''}`}
                                onClick={() => goToSlide(idx)}
                                aria-label={`Go to slide ${idx + 1}`}
                            />
                        ))}
                    </div>
                )}
            </div>
        </section>
    );
};