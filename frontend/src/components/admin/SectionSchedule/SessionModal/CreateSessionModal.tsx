import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useCinemaHalls, useMovieSessionSearch } from '@/hooks/features';
import { Input, Select, Button, Modal } from '@/components/ui';
import type { SessionAdminResponse, SessionCreateRequest } from '@/types/session';
import type { MovieSessionSearchResponse } from '@/types/movie';
import styles from './SessionModal.module.css';

interface CreateSessionModalProps {
    isOpen: boolean;
    session: SessionAdminResponse | null;
    onSave: (data: SessionCreateRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
}

interface FormData {
    startTime: string;
    basePrice: string;
    movieId: string;
    hallId: string;
}

export const CreateSessionModal: React.FC<CreateSessionModalProps> = ({
    isOpen,
    session,
    onSave,
    onClose,
    loading
}) => {
    const { allHalls: halls, loading: hallsLoading } = useCinemaHalls();
    const { movies, searchMoviesForSession, loading: moviesLoading } = useMovieSessionSearch();

    const [formData, setFormData] = useState<FormData>({
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
    const [isSearching, setIsSearching] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    const isEditing = !!session;

    const handleClickOutside = useCallback((event: MouseEvent) => {
        if (movieSearchRef.current && !movieSearchRef.current.contains(event.target as Node)) {
            setShowMovieResults(false);
        }
    }, []);

    useEffect(() => {
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [handleClickOutside]);

    useEffect(() => {
        if (isOpen) {
            if (session) {
                const startDateTime = new Date(session.startTime);
                const formattedTime = startDateTime.toISOString().slice(0, 16);

                setFormData({
                    startTime: formattedTime,
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
                resetForm();
            }
        }
    }, [session, isOpen]);

    const resetForm = () => {
        setFormData({
            startTime: '',
            basePrice: '',
            movieId: '',
            hallId: ''
        });
        setSelectedMovie(null);
        setMovieSearchTerm('');
        setErrors({});
        setNotification(null);
        setShowMovieResults(false);
        setIsSearching(false);
    };

    const showNotification = (message: string, type: 'success' | 'error') => {
        setNotification({ message, type });

        setTimeout(() => {
            setNotification(null);
        }, 5000);
    };

    const handleStartTimeChange = (value: string) => {
        const newFormData = { ...formData, startTime: value };
        setFormData(newFormData);

        if (errors.startTime) {
            setErrors(prev => ({ ...prev, startTime: '' }));
        }

        if (value && selectedMovie?.id.toString() === formData.movieId) {
            setSelectedMovie(null);
            setFormData(prev => ({ ...prev, movieId: '' }));
            setMovieSearchTerm('');
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
            const date = formData.startTime.split('T')[0];
            setIsSearching(true);
            await searchMoviesForSession(date, '');
            setIsSearching(false);
            setShowMovieResults(true);
        }
    };

    const handleMovieInputChange = async (value: string) => {
        setMovieSearchTerm(value);
        if (formData.startTime) {
            const date = formData.startTime.split('T')[0];
            setIsSearching(true);
            await searchMoviesForSession(date, value);
            setIsSearching(false);
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
            const now = new Date();
            const minTime = new Date(now.getTime() + 30 * 60000);

            if (selectedTime < minTime) {
                newErrors.startTime = 'Session must start at least 30 minutes from now';
            }
        }

        const basePrice = Number(formData.basePrice);
        if (!formData.basePrice || basePrice < 10) {
            newErrors.basePrice = 'Price must be at least 10 UAH';
        } else if (isNaN(basePrice)) {
            newErrors.basePrice = 'Price must be a valid number';
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

        const sessionData: SessionCreateRequest = {
            startTime: formData.startTime,
            basePrice: formData.basePrice,
            movieId: Number(formData.movieId),
            hallId: Number(formData.hallId)
        };

        try {
            await onSave(sessionData);
            showNotification(
                isEditing ? 'Session updated successfully' : 'Session created successfully',
                'success'
            );
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to save session';
            showNotification(errorMessage, 'error');
        }
    };

    const hallOptions = halls.map(hall => ({
        value: hall.id.toString(),
        label: `${hall.name} (${hall.capacity} seats)`
    }));

    const calculateMinDateTime = () => {
        const now = new Date();
        now.setMinutes(now.getMinutes() + 30);
        return now.toISOString().slice(0, 16);
    };

    const hasError = (field: keyof FormData): boolean => {
        return !!errors[field];
    };

    return (
        <>
            <Modal
                isOpen={isOpen}
                onClose={onClose}
                title={isEditing ? 'Edit Session' : 'Create New Session'}
                size="large"
            >
                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>Start Time *</label>
                            <Input
                                type="datetime-local"
                                value={formData.startTime}
                                onChange={handleStartTimeChange}
                                error={hasError('startTime') ? errors.startTime : undefined}
                                min={calculateMinDateTime()}
                                className={styles.input}
                            />
                            {hasError('startTime') && <span className={styles.errorText}>{errors.startTime}</span>}
                        </div>

                        <div className={styles.formGroup}>
                            <label className={styles.label}>Base Price (UAH) *</label>
                            <Input
                                type="number"
                                step="0.01"
                                min="10"
                                value={formData.basePrice}
                                onChange={handleBasePriceChange}
                                error={hasError('basePrice') ? errors.basePrice : undefined}
                                placeholder="10.00"
                                className={styles.input}
                            />
                            {hasError('basePrice') && <span className={styles.errorText}>{errors.basePrice}</span>}
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Movie *</label>
                        <div className={styles.movieSearch} ref={movieSearchRef}>
                            <Input
                                type="text"
                                value={movieSearchTerm}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder={formData.startTime ? "Select or search movie..." : "Select start time first"}
                                disabled={!formData.startTime}
                                className={styles.movieInput}
                                error={hasError('movieId') && !showMovieResults ? errors.movieId : undefined}
                            />
                            {!formData.startTime && (
                                <div className={styles.hint}>Please select start time first to see available movies</div>
                            )}

                            {selectedMovie && (
                                <div className={styles.selectedMovieInfo}>
                                    <div className={styles.selectedMovieTitle}>{selectedMovie.title}</div>
                                    <div className={styles.selectedMovieDetails}>
                                        {selectedMovie.releaseYear} • {selectedMovie.durationMinutes} minutes
                                    </div>
                                </div>
                            )}

                            {showMovieResults && (
                                <div className={styles.movieResults}>
                                    {isSearching || moviesLoading ? (
                                        <div className={styles.loadingResults}>Loading movies...</div>
                                    ) : movies.length > 0 ? (
                                        movies.map(movie => (
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
                                        ))
                                    ) : (
                                        <div className={styles.noResults}>No movies available for selected date</div>
                                    )}
                                </div>
                            )}
                        </div>
                        {hasError('movieId') && !showMovieResults && <span className={styles.errorText}>{errors.movieId}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Hall *</label>
                        <Select
                            value={formData.hallId}
                            onChange={handleHallChange}
                            options={hallOptions}
                            disabled={hallsLoading}
                            placeholder="Select hall"
                        />
                        {hasError('hallId') && <span className={styles.errorText}>{errors.hallId}</span>}
                    </div>

                    <div className={styles.formActions}>
                        <div className={styles.actions}>
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={onClose}
                                disabled={loading}
                                className={styles.cancelButton}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                variant="success"
                                disabled={loading || hallsLoading}
                                loading={loading}
                                className={styles.submitButton}
                            >
                                {isEditing ? 'Update Session' : 'Create Session'}
                            </Button>
                        </div>
                    </div>
                </form>
            </Modal>

            {notification && (
                <div className={styles.notificationWrapper}>
                    <div className={`${styles.customNotification} ${notification.type === 'success' ? styles.success : styles.error}`}>
                        <span className={styles.notificationIcon}>
                            {notification.type === 'success' ? '✅' : '❌'}
                        </span>
                        <span className={styles.notificationMessage}>
                            {notification.message}
                        </span>
                        <button
                            className={styles.notificationClose}
                            onClick={() => setNotification(null)}
                        >
                            ×
                        </button>
                    </div>
                </div>
            )}
        </>
    );
};