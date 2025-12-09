import React, { useState, useEffect, useRef } from 'react';
import { useHalls, useMovieSessionSearch } from '@/hooks/features';
import { Input, Select, Button, Modal, Notification } from '@/components/ui';
import type { SessionAdminResponse, SessionRequest } from '@/types/session';
import type { MovieSessionSearchResponse } from '@/types/movie';
import styles from './SessionModal.module.css';

interface SessionModalProps {
    isOpen: boolean;
    session: SessionAdminResponse | null;
    onSave: (data: SessionRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
}

export const SessionModal: React.FC<SessionModalProps> = ({
    isOpen,
    session,
    onSave,
    onClose,
    loading
}) => {
    const { halls, loading: hallsLoading } = useHalls();
    const { movies, searchMoviesForSession } = useMovieSessionSearch();

    const [formData, setFormData] = useState({
        startTime: '',
        basePrice: '',
        movieId: '',
        hallId: ''
    });

    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(null);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (movieSearchRef.current && !movieSearchRef.current.contains(event.target as Node)) {
                setShowMovieResults(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        if (session) {
            setFormData({
                startTime: session.startTime.slice(0, 16),
                basePrice: session.basePrice.toString(),
                movieId: session.movieId.toString(),
                hallId: session.hallId.toString()
            });
            setSelectedMovie({
                id: session.movieId,
                title: session.movieTitle,
                releaseYear: new Date(session.startTime).getFullYear(),
                durationMinutes: session.movieDuration
            });
            setMovieSearchTerm(session.movieTitle);
        } else {
            setFormData({
                startTime: '',
                basePrice: '',
                movieId: '',
                hallId: ''
            });
            setSelectedMovie(null);
            setMovieSearchTerm('');
        }
        setErrors({});
        setNotification(null);
        setShowMovieResults(false);
    }, [session, isOpen]);

    const showNotification = (message: string, type: 'success' | 'error') => {
        setNotification({ message, type });
    };

    const handleStartTimeChange = (value: string) => {
        setFormData(prev => ({ ...prev, startTime: value }));
        if (errors.startTime) {
            setErrors(prev => ({ ...prev, startTime: '' }));
        }
    };

    const handleBasePriceChange = (value: string) => {
        setFormData(prev => ({ ...prev, basePrice: value }));
        if (errors.basePrice) {
            setErrors(prev => ({ ...prev, basePrice: '' }));
        }
    };

    const handleMovieInputClick = async () => {
        if (formData.startTime) {
            await searchMoviesForSession(formData.startTime.split('T')[0], '');
            setShowMovieResults(true);
        }
    };

    const handleMovieInputChange = async (value: string) => {
        setMovieSearchTerm(value);
        if (formData.startTime) {
            await searchMoviesForSession(formData.startTime.split('T')[0], value);
            setShowMovieResults(true);
        }
    };

    const handleMovieSelect = (movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setFormData(prev => ({ ...prev, movieId: movie.id.toString() }));
        setMovieSearchTerm(movie.title);
        setShowMovieResults(false);
        if (errors.movieId) {
            setErrors(prev => ({ ...prev, movieId: '' }));
        }
    };

    const handleHallChange = (value: string | number) => {
        setFormData(prev => ({ ...prev, hallId: value.toString() }));
        if (errors.hallId) {
            setErrors(prev => ({ ...prev, hallId: '' }));
        }
    };

    const validateForm = (): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.startTime) {
            newErrors.startTime = 'Start time is required';
        } else {
            const selectedTime = new Date(formData.startTime);
            const minTime = new Date();
            minTime.setMinutes(minTime.getMinutes() + 30);

            if (selectedTime < minTime) {
                newErrors.startTime = 'Session must start at least 30 minutes from now';
            }
        }

        if (!formData.basePrice || Number(formData.basePrice) < 10) {
            newErrors.basePrice = 'Price must be at least 10 UAH';
        }

        if (!formData.movieId) {
            newErrors.movieId = 'Movie is required';
        }

        if (!formData.hallId) {
            newErrors.hallId = 'Hall is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        const sessionData: SessionRequest = {
            startTime: formData.startTime + ':00',
            basePrice: Number(formData.basePrice),
            movieId: Number(formData.movieId),
            hallId: Number(formData.hallId)
        };

        try {
            await onSave(sessionData);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to save session';
            showNotification(errorMessage, 'error');
        }
    };

    const hallOptions = halls.map(hall => ({
        value: hall.id.toString(),
        label: `${hall.name} (${hall.capacity} seats)`
    }));

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title={session ? 'Edit Session' : 'Create Session'}
                size="medium"
            >
                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label>Start Time *</label>
                        <Input
                            type="datetime-local"
                            value={formData.startTime}
                            onChange={handleStartTimeChange}
                            error={errors.startTime}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label>Base Price (UAH) *</label>
                        <Input
                            type="number"
                            step="0.01"
                            min="10"
                            value={formData.basePrice}
                            onChange={handleBasePriceChange}
                            error={errors.basePrice}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label>Movie *</label>
                        <div className={styles.movieSearch} ref={movieSearchRef}>
                            <Input
                                type="text"
                                value={movieSearchTerm}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder="Click to see available movies or search..."
                                disabled={!formData.startTime}
                            />
                            {!formData.startTime && (
                                <div className={styles.hint}>Select start time first to see available movies</div>
                            )}

                            {showMovieResults && movies.length > 0 && (
                                <div className={styles.movieResults}>
                                    {movies.map(movie => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${selectedMovie?.id === movie.id ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie)}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>
                                                {movie.releaseYear} • {movie.durationMinutes} min
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}

                            {showMovieResults && movies.length === 0 && (
                                <div className={styles.noResults}>No movies available for selected date</div>
                            )}
                        </div>
                        {errors.movieId && <span className={styles.errorText}>{errors.movieId}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label>Hall *</label>
                        <Select
                            value={formData.hallId}
                            onChange={handleHallChange}
                            options={hallOptions}
                            disabled={hallsLoading}
                            placeholder="Select hall"
                        />
                        {errors.hallId && <span className={styles.errorText}>{errors.hallId}</span>}
                    </div>

                    <div className={styles.actions}>
                        <Button
                            type="button"
                            variant="secondary"
                            onClick={onClose}
                            disabled={loading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            variant="success"
                            disabled={loading || hallsLoading}
                            loading={loading}
                        >
                            {session ? 'Update' : 'Create'}
                        </Button>
                    </div>
                </form>
            </Modal>

            {notification && (
                <Notification
                    id="session-modal-notification"
                    message={notification.message}
                    type={notification.type}
                    isVisible={true}
                    onClose={() => setNotification(null)}
                    duration={5000}
                    isStatic={false}
                />
            )}
        </>
    );
};