// src/components/admin/SectionSchedule/SessionUpdateModal/SessionUpdateModal.tsx
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useCinemaHalls, useMovieSessionSearch } from '@/hooks/features';
import { Input, Select, Button, Modal } from '@/components/ui';
import type { SessionAdminResponse, SessionUpdateRequest } from '@/types/session';
import type { MovieSessionSearchResponse } from '@/types/movie';
import styles from './SessionUpdateModal.module.css';

interface SessionUpdateModalProps {
    isOpen: boolean;
    session: SessionAdminResponse;
    onSave: (id: number, data: SessionUpdateRequest) => Promise<void>;
    onClose: () => void;
    loading: boolean;
}

interface FormData {
    startTime: string;
    basePrice: string;
    movieId: string;
    hallId: string;
}

export const SessionUpdateModal: React.FC<SessionUpdateModalProps> = ({
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
        hallId: '',
    });

    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(null);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [isSearching, setIsSearching] = useState(false);
    const [hasChanges, setHasChanges] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    const initialFormData = useRef<FormData | null>(null);

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
        if (isOpen && session) {
            const startDateTime = new Date(session.startTime);
            const formattedTime = startDateTime.toISOString().slice(0, 16);

            const newFormData = {
                startTime: formattedTime,
                basePrice: session.basePrice.toString(),
                movieId: session.movieId.toString(),
                hallId: session.hallId.toString(),
            };

            setFormData(newFormData);

            if (!initialFormData.current) {
                initialFormData.current = { ...newFormData };
            }

            setSelectedMovie({
                id: session.movieId,
                title: session.movieTitle,
                releaseYear: new Date(session.startTime).getFullYear(),
                durationMinutes: session.movieDuration
            });
            setMovieSearchTerm(session.movieTitle);
            setErrors({});
            setShowMovieResults(false);
            setIsSearching(false);
        }
    }, [session, isOpen]);

    useEffect(() => {
        if (initialFormData.current) {
            const hasAnyChanges = Object.keys(formData).some(key =>
                formData[key as keyof FormData] !== initialFormData.current![key as keyof FormData]
            );
            setHasChanges(hasAnyChanges);
        }
    }, [formData]);

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

        if (formData.startTime) {
            const selectedTime = new Date(formData.startTime);
            const now = new Date();

            if (selectedTime < now) {
                newErrors.startTime = 'Start time must be in the future';
            } else if (selectedTime < new Date(now.getTime() + 30 * 60000)) {
                newErrors.startTime = 'Session must start at least 30 minutes from now';
            }
        }

        if (formData.basePrice) {
            const basePrice = Number(formData.basePrice);
            if (basePrice < 10) {
                newErrors.basePrice = 'Price must be at least 10 UAH';
            } else if (isNaN(basePrice)) {
                newErrors.basePrice = 'Price must be a valid number';
            }
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        const updateData: SessionUpdateRequest = {};

        if (formData.startTime && formData.startTime !== initialFormData.current?.startTime) {
            updateData.startTime = formData.startTime;
        }
        if (formData.basePrice && formData.basePrice !== initialFormData.current?.basePrice) {
            updateData.basePrice = formData.basePrice;
        }
        if (formData.movieId && formData.movieId !== initialFormData.current?.movieId) {
            updateData.movieId = Number(formData.movieId);
        }
        if (formData.hallId && formData.hallId !== initialFormData.current?.hallId) {
            updateData.hallId = Number(formData.hallId);
        }

        if (Object.keys(updateData).length === 0) {
            onClose();
            return;
        }

        try {
            await onSave(session.id, updateData);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to update session';
            setErrors(prev => ({ ...prev, _form: errorMessage }));
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

    const getSessionInfo = () => {
        const info = [];
        if (session.ticketsSold > 0) {
            info.push(`${session.ticketsSold} tickets sold`);
        }
        const totalRevenueNum = parseFloat(session.totalRevenue);
        if (totalRevenueNum > 0) {
            info.push(`Revenue: ${totalRevenueNum.toFixed(2)} UAH`);
        }
        return info.length > 0 ? info.join(', ') : 'No tickets sold yet';
    };

    const canEditTime = session.status === 'SCHEDULED';
    const canEditPrice = session.status === 'SCHEDULED';
    const canEditMovie = session.status === 'SCHEDULED';
    const canEditHall = session.status === 'SCHEDULED';

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Edit Session"
            size="large"
        >
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.sessionInfo}>
                    <div className={styles.infoItem}>
                        <span className={styles.infoLabel}>Current Status:</span>
                        <span className={`${styles.statusBadge} ${styles[session.status.toLowerCase()]}`}>
                            {session.status}
                        </span>
                    </div>
                    <div className={styles.infoItem}>
                        <span className={styles.infoLabel}>Movie:</span>
                        <span className={styles.infoValue}>{session.movieTitle}</span>
                    </div>
                    <div className={styles.infoItem}>
                        <span className={styles.infoLabel}>Hall:</span>
                        <span className={styles.infoValue}>{session.hallName}</span>
                    </div>
                    <div className={styles.infoItem}>
                        <span className={styles.infoLabel}>Statistics:</span>
                        <span className={styles.infoValue}>{getSessionInfo()}</span>
                    </div>
                </div>

                {errors._form && (
                    <div className={styles.formError}>
                        <span className={styles.errorIcon}>⚠️</span>
                        <span>{errors._form}</span>
                    </div>
                )}

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Start Time</label>
                        <Input
                            type="datetime-local"
                            value={formData.startTime}
                            onChange={handleStartTimeChange}
                            error={errors.startTime}
                            min={calculateMinDateTime()}
                            className={styles.input}
                            disabled={!canEditTime}
                        />
                        {!canEditTime && (
                            <div className={styles.disabledHint}>Cannot change start time for {session.status.toLowerCase()} session</div>
                        )}
                        {errors.startTime && <span className={styles.errorText}>{errors.startTime}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Base Price (UAH)</label>
                        <Input
                            type="number"
                            step="0.01"
                            min="10"
                            value={formData.basePrice}
                            onChange={handleBasePriceChange}
                            error={errors.basePrice}
                            className={styles.input}
                            disabled={!canEditPrice}
                        />
                        {!canEditPrice && (
                            <div className={styles.disabledHint}>Cannot change price for {session.status.toLowerCase()} session</div>
                        )}
                        {errors.basePrice && <span className={styles.errorText}>{errors.basePrice}</span>}
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Movie</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <Input
                            type="text"
                            value={movieSearchTerm}
                            onChange={handleMovieInputChange}
                            onClick={handleMovieInputClick}
                            placeholder={formData.startTime ? "Select or search movie..." : "Select start time first"}
                            disabled={!canEditMovie || !formData.startTime}
                            className={styles.movieInput}
                            error={errors.movieId && !showMovieResults ? errors.movieId : undefined}
                        />
                        {(!canEditMovie || !formData.startTime) && (
                            <div className={styles.hint}>
                                {!canEditMovie
                                    ? `Cannot change movie for ${session.status.toLowerCase()} session`
                                    : 'Please select start time first to see available movies'}
                            </div>
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
                    {errors.movieId && !showMovieResults && <span className={styles.errorText}>{errors.movieId}</span>}
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall</label>
                    <Select
                        value={formData.hallId}
                        onChange={handleHallChange}
                        options={hallOptions}
                        disabled={hallsLoading || !canEditHall}
                        placeholder="Select hall"
                    />
                    {!canEditHall && (
                        <div className={styles.disabledHint}>Cannot change hall for {session.status.toLowerCase()} session</div>
                    )}
                    {errors.hallId && <span className={styles.errorText}>{errors.hallId}</span>}
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
                            disabled={loading || hallsLoading || !hasChanges}
                            loading={loading}
                            className={styles.submitButton}
                        >
                            Update Session
                        </Button>
                    </div>
                    {hasChanges && (
                        <div className={styles.changesHint}>
                            <span className={styles.changesIcon}>📝</span>
                            <span>You have unsaved changes</span>
                        </div>
                    )}
                </div>
            </form>
        </Modal>
    );
};