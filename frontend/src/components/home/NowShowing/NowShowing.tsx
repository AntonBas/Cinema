import React, { useState, useEffect, useRef } from 'react';
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

const ITEMS_TO_SHOW = 3;
const AUTO_PLAY_INTERVAL = 5000;

export const NowShowing: React.FC<NowShowingProps> = ({ movies, loading }) => {
    const navigate = useNavigate();
    const [currentIndex, setCurrentIndex] = useState(0);
    const [isHovered, setIsHovered] = useState(false);
    const timerRef = useRef<number | null>(null);

    const maxIndex = Math.max(0, movies.length - ITEMS_TO_SHOW);

    const nextSlide = () => {
        setCurrentIndex(prev => (prev >= maxIndex ? 0 : prev + 1));
    };

    const prevSlide = () => {
        setCurrentIndex(prev => (prev <= 0 ? maxIndex : prev - 1));
    };

    useEffect(() => {
        if (isHovered || loading || movies.length <= ITEMS_TO_SHOW) return;

        timerRef.current = window.setInterval(nextSlide, AUTO_PLAY_INTERVAL);

        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
    }, [isHovered, loading, movies.length, maxIndex]);

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

    const visibleMovies = movies.slice(currentIndex, currentIndex + ITEMS_TO_SHOW);

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
                    {movies.length > ITEMS_TO_SHOW && (
                        <Button variant="outline" size="small" className={styles.navButton} onClick={prevSlide}>
                            &#10094;
                        </Button>
                    )}

                    <div className={styles.carouselWrapper}>
                        <div className={styles.moviesGrid}>
                            {visibleMovies.map(movie => (
                                <div key={movie.id} className={styles.slideItem}>
                                    <MovieCard movie={movie} />
                                </div>
                            ))}
                        </div>
                    </div>

                    {movies.length > ITEMS_TO_SHOW && (
                        <Button variant="outline" size="small" className={styles.navButton} onClick={nextSlide}>
                            &#10095;
                        </Button>
                    )}
                </div>

                {movies.length > ITEMS_TO_SHOW && (
                    <div className={styles.dots}>
                        {Array.from({ length: maxIndex + 1 }).map((_, idx) => (
                            <button
                                key={idx}
                                className={`${styles.dot} ${currentIndex === idx ? styles.dotActive : ''}`}
                                onClick={() => setCurrentIndex(idx)}
                            />
                        ))}
                    </div>
                )}
            </div>
        </section>
    );
};