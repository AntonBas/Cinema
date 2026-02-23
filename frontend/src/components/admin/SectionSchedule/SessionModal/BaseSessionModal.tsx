import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { Input, Select, Button, Modal } from '@/components/ui';
import type { SessionCreateRequest, SessionUpdateRequest } from '@/types/session';
import type { MovieSessionSearchResponse } from '@/types/movie';
import type { CinemaHallResponse } from '@/types/cinemaHall';
import styles from './SessionModal.module.css';

interface BaseSessionModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (data: SessionCreateRequest | SessionUpdateRequest) => Promise<void>;
    loading: boolean;
    initialData?: {
        startTime?: string;
        basePrice?: string;
        movieId?: string;
        hallId?: string;
        movieTitle?: string;
        movieDuration?: number;
    };
    title: string;
    submitText: string;
    isEditing?: boolean;
    session?: any;
    halls: CinemaHallResponse[];
    hallsLoading: boolean;
}

export const BaseSessionModal: React.FC<BaseSessionModalProps> = ({
    isOpen,
    onClose,
    onSave,
    loading,
    initialData,
    title,
    submitText,
    isEditing = false,
    session,
    halls,
    hallsLoading
}) => {
    const { searchMoviesForSession } = useMovies();

    const [formData, setFormData] = useState({
        startTime: initialData?.startTime || '',
        basePrice: initialData?.basePrice || '',
        movieId: initialData?.movieId || '',
        hallId: initialData?.hallId || ''
    });

    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(
        initialData?.movieId ? {
            id: Number(initialData.movieId),
            title: initialData.movieTitle || '',
            durationMinutes: initialData.movieDuration || 0
        } : null
    );
    const [movieResults, setMovieResults] = useState<MovieSessionSearchResponse[]>([]);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [movieSearchTerm, setMovieSearchTerm] = useState(initialData?.movieTitle || '');
    const [isSearching, setIsSearching] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    const canEditTime = !session || session.status === 'SCHEDULED';
    const canEditPrice = !session || session.status === 'SCHEDULED';
    const canEditMovie = !session || session.status === 'SCHEDULED';
    const canEditHall = !session || session.status === 'SCHEDULED';

    const initialFormDataRef = useRef(initialData);
    const isFirstRender = useRef(true);

    useEffect(() => {
        if (isOpen && isFirstRender.current) {
            setFormData({
                startTime: initialData?.startTime || '',
                basePrice: initialData?.basePrice || '',
                movieId: initialData?.movieId || '',
                hallId: initialData?.hallId || ''
            });

            setMovieSearchTerm(initialData?.movieTitle || '');

            if (initialData?.movieId) {
                setSelectedMovie({
                    id: Number(initialData.movieId),
                    title: initialData.movieTitle || '',
                    durationMinutes: initialData.movieDuration || 0
                });
            } else {
                setSelectedMovie(null);
            }

            setErrors({});
            setShowMovieResults(false);
            initialFormDataRef.current = initialData;
            isFirstRender.current = false;
        }

        if (!isOpen) {
            isFirstRender.current = true;
        }
    }, [isOpen, initialData]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (movieSearchRef.current && !movieSearchRef.current.contains(event.target as Node)) {
                setShowMovieResults(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleStartTimeChange = useCallback((value: string) => {
        setFormData(prev => ({ ...prev, startTime: value }));
        if (errors.startTime) setErrors(prev => ({ ...prev, startTime: '' }));
        if (value && selectedMovie?.id.toString() === formData.movieId) {
            setSelectedMovie(null);
            setFormData(prev => ({ ...prev, movieId: '' }));
            setMovieSearchTerm('');
            setMovieResults([]);
        }
    }, [errors.startTime, selectedMovie?.id, formData.movieId]);

    const handleBasePriceChange = useCallback((value: string) => {
        setFormData(prev => ({ ...prev, basePrice: value }));
        if (errors.basePrice) setErrors(prev => ({ ...prev, basePrice: '' }));
    }, [errors.basePrice]);

    const handleMovieInputClick = useCallback(async () => {
        if (formData.startTime) {
            const date = formData.startTime.split('T')[0];
            setIsSearching(true);
            try {
                const results = await searchMoviesForSession(date);
                setMovieResults(results || []);
                setShowMovieResults(results && results.length > 0);
            } catch (error) {
                setMovieResults([]);
                setShowMovieResults(false);
            } finally {
                setIsSearching(false);
            }
        }
    }, [formData.startTime, searchMoviesForSession]);

    const handleMovieInputChange = useCallback(async (value: string) => {
        setMovieSearchTerm(value);
        if (formData.startTime) {
            const date = formData.startTime.split('T')[0];
            setIsSearching(true);
            try {
                const searchTerm = value.trim() || undefined;
                const results = await searchMoviesForSession(searchTerm || date);
                setMovieResults(results || []);
                setShowMovieResults(true);
            } catch (error) {
                setMovieResults([]);
                setShowMovieResults(false);
            } finally {
                setIsSearching(false);
            }
        }
    }, [formData.startTime, searchMoviesForSession]);

    const handleMovieSelect = useCallback((movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setFormData(prev => ({ ...prev, movieId: movie.id.toString() }));
        setMovieSearchTerm(movie.title);
        setShowMovieResults(false);
        if (errors.movieId) setErrors(prev => ({ ...prev, movieId: '' }));
    }, [errors.movieId]);

    const handleHallChange = useCallback((value: string | number) => {
        setFormData(prev => ({ ...prev, hallId: value.toString() }));
        if (errors.hallId) setErrors(prev => ({ ...prev, hallId: '' }));
    }, [errors.hallId]);

    const validateForm = useCallback((): boolean => {
        const newErrors: Record<string, string> = {};

        if (!isEditing || formData.startTime) {
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
        }

        if (!isEditing || formData.basePrice) {
            const basePrice = Number(formData.basePrice);
            if (!formData.basePrice) {
                newErrors.basePrice = 'Price is required';
            } else if (isNaN(basePrice) || basePrice < 10) {
                newErrors.basePrice = 'Price must be at least 10 UAH';
            }
        }

        if (!isEditing || formData.movieId) {
            if (!formData.movieId) newErrors.movieId = 'Movie is required';
        }

        if (!isEditing || formData.hallId) {
            if (!formData.hallId) newErrors.hallId = 'Hall is required';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    }, [isEditing, formData]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            if (isEditing) {
                const updateData: SessionUpdateRequest = {};
                if (formData.startTime && formData.startTime !== initialFormDataRef.current?.startTime) {
                    updateData.startTime = formData.startTime;
                }
                if (formData.basePrice && formData.basePrice !== initialFormDataRef.current?.basePrice) {
                    updateData.basePrice = formData.basePrice;
                }
                if (formData.movieId && formData.movieId !== initialFormDataRef.current?.movieId) {
                    updateData.movieId = Number(formData.movieId);
                }
                if (formData.hallId && formData.hallId !== initialFormDataRef.current?.hallId) {
                    updateData.hallId = Number(formData.hallId);
                }

                if (Object.keys(updateData).length === 0) {
                    onClose();
                    return;
                }
                await onSave(updateData);
            } else {
                const createData: SessionCreateRequest = {
                    startTime: formData.startTime,
                    basePrice: formData.basePrice,
                    movieId: Number(formData.movieId),
                    hallId: Number(formData.hallId)
                };
                await onSave(createData);
            }
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : `Failed to ${isEditing ? 'update' : 'create'} session`;
            setErrors(prev => ({ ...prev, _form: errorMessage }));
        }
    }, [validateForm, isEditing, formData, onSave, onClose]);

    const hallOptions = useMemo(() => [
        { value: '', label: 'Select a hall' },
        ...halls.map(hall => ({
            value: hall.id.toString(),
            label: `${hall.name} (${hall.capacity} seats)`
        }))
    ], [halls]);

    const minDateTime = useMemo(() => {
        const now = new Date();
        now.setMinutes(now.getMinutes() + 30);
        return now.toISOString().slice(0, 16);
    }, []);

    const hasChanges = useMemo(() => {
        if (!isEditing || !initialFormDataRef.current) return false;
        return Object.keys(formData).some(key =>
            formData[key as keyof typeof formData] !== initialFormDataRef.current![key as keyof typeof formData]
        );
    }, [isEditing, formData]);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="large">
            <form onSubmit={handleSubmit} className={styles.form}>
                {session && (
                    <div className={styles.sessionInfo}>
                        <div className={styles.infoItem}>
                            <span className={styles.infoLabel}>Current Status:</span>
                            <span className={`${styles.statusBadge} ${styles[session.status.toLowerCase()]}`}>
                                {session.status}
                            </span>
                        </div>
                        {session.ticketsSold > 0 && (
                            <div className={styles.infoItem}>
                                <span className={styles.infoLabel}>Statistics:</span>
                                <span className={styles.infoValue}>
                                    {session.ticketsSold} tickets sold, Revenue: {Number(session.totalRevenue).toFixed(2)} UAH
                                </span>
                            </div>
                        )}
                    </div>
                )}

                {errors._form && (
                    <div className={styles.formError}>
                        <span className={styles.errorIcon}>⚠️</span>
                        <span>{errors._form}</span>
                    </div>
                )}

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Start Time {!isEditing && <span className={styles.required}>*</span>}
                        </label>
                        <Input
                            type="datetime-local"
                            value={formData.startTime}
                            onChange={handleStartTimeChange}
                            error={errors.startTime}
                            min={minDateTime}
                            className={styles.input}
                            disabled={!canEditTime}
                        />
                        {isEditing && !canEditTime && (
                            <div className={styles.disabledHint}>
                                Cannot change start time for {session?.status.toLowerCase()} session
                            </div>
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Base Price (UAH) {!isEditing && <span className={styles.required}>*</span>}
                        </label>
                        <Input
                            type="number"
                            step="0.01"
                            min="10"
                            value={formData.basePrice}
                            onChange={handleBasePriceChange}
                            error={errors.basePrice}
                            placeholder="10.00"
                            className={styles.input}
                            disabled={!canEditPrice}
                        />
                        {isEditing && !canEditPrice && (
                            <div className={styles.disabledHint}>
                                Cannot change price for {session?.status.toLowerCase()} session
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Movie {!isEditing && <span className={styles.required}>*</span>}
                    </label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <Input
                            type="text"
                            value={movieSearchTerm}
                            onChange={handleMovieInputChange}
                            onClick={handleMovieInputClick}
                            placeholder={formData.startTime ? "Search movie..." : "Select start time first"}
                            disabled={!canEditMovie || !formData.startTime}
                            className={styles.movieInput}
                            error={errors.movieId && !showMovieResults ? errors.movieId : undefined}
                        />

                        {selectedMovie && (
                            <div className={styles.selectedMovieInfo}>
                                <div className={styles.selectedMovieTitle}>{selectedMovie.title}</div>
                                <div className={styles.selectedMovieDetails}>
                                    {selectedMovie.durationMinutes} min
                                </div>
                            </div>
                        )}

                        {showMovieResults && (
                            <div className={styles.movieResults}>
                                {isSearching ? (
                                    <div className={styles.loadingResults}>Loading movies...</div>
                                ) : movieResults.length > 0 ? (
                                    movieResults.map(movie => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${selectedMovie?.id === movie.id ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie)}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>
                                                {movie.durationMinutes} min
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults}>No movies available for selected date</div>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Hall {!isEditing && <span className={styles.required}>*</span>}
                    </label>
                    <Select
                        value={formData.hallId}
                        onChange={handleHallChange}
                        options={hallOptions}
                        disabled={hallsLoading || !canEditHall}
                    />
                    {errors.hallId && <span className={styles.errorText}>{errors.hallId}</span>}
                    {isEditing && !canEditHall && (
                        <div className={styles.disabledHint}>
                            Cannot change hall for {session?.status.toLowerCase()} session
                        </div>
                    )}
                </div>

                <div className={styles.formActions}>
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
                        disabled={loading || hallsLoading || (isEditing && !hasChanges)}
                        loading={loading}
                        className={styles.submitButton}
                    >
                        {submitText}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};