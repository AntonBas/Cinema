import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Button } from '@/components/ui/Button/Button';
import { Modal } from '@/components/ui/Modal/Modal';
import type { SessionRequest } from '@/types/session';
import type { MovieSessionSearchResponse } from '@/types/movie';
import type { CinemaHallListResponse } from '@/types/cinemaHall';
import styles from './SessionModal.module.css';

interface BaseSessionModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSave: (data: SessionRequest) => Promise<void>;
    loading: boolean;
    initialData?: {
        startTime?: string;
        basePrice?: number;
        movieId?: number;
        hallId?: number;
        movieTitle?: string;
    };
    title: string;
    submitText: string;
    halls: CinemaHallListResponse[];
}

export const BaseSessionModal: React.FC<BaseSessionModalProps> = ({
    isOpen,
    onClose,
    onSave,
    loading,
    initialData,
    title,
    submitText,
    halls,
}) => {
    const { search } = useMovies();

    const [formData, setFormData] = useState({
        startTime: initialData?.startTime || '',
        basePrice: initialData?.basePrice?.toString() || '',
        movieId: initialData?.movieId?.toString() || '',
        hallId: initialData?.hallId?.toString() || ''
    });

    const [selectedMovie, setSelectedMovie] = useState<MovieSessionSearchResponse | null>(null);
    const [movieResults, setMovieResults] = useState<MovieSessionSearchResponse[]>([]);
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [movieSearchTerm, setMovieSearchTerm] = useState(initialData?.movieTitle || '');
    const [isSearching, setIsSearching] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);
    const hasLoadedRef = useRef(false);

    useEffect(() => {
        if (isOpen) {
            setFormData({
                startTime: initialData?.startTime || '',
                basePrice: initialData?.basePrice?.toString() || '',
                movieId: initialData?.movieId?.toString() || '',
                hallId: initialData?.hallId?.toString() || ''
            });
            setMovieSearchTerm(initialData?.movieTitle || '');
            setSelectedMovie(null);
            setErrors({});
            setShowMovieResults(false);
            hasLoadedRef.current = false;
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
        setSelectedMovie(null);
        setFormData(prev => ({ ...prev, movieId: '' }));
        setMovieSearchTerm('');
        setMovieResults([]);
        setShowMovieResults(false);
        hasLoadedRef.current = false;
        setErrors(prev => ({ ...prev, movieId: '' }));
    }, []);

    const handleMovieSearch = useCallback(async (query?: string) => {
        if (!formData.startTime) {
            setMovieResults([]);
            setShowMovieResults(true);
            return;
        }

        setIsSearching(true);
        try {
            const date = formData.startTime.split('T')[0];
            const results = await search(query || '', date);
            setMovieResults(results || []);
            setShowMovieResults(true);
        } catch {
            setMovieResults([]);
        } finally {
            setIsSearching(false);
        }
    }, [formData.startTime, search]);

    const handleMovieClick = useCallback(async () => {
        if (!formData.startTime) return;

        setShowMovieResults(true);

        if (!hasLoadedRef.current) {
            await handleMovieSearch('');
            hasLoadedRef.current = true;
        }
    }, [formData.startTime, handleMovieSearch]);

    const handleMovieSelect = useCallback((movie: MovieSessionSearchResponse) => {
        setSelectedMovie(movie);
        setFormData(prev => ({ ...prev, movieId: movie.id.toString() }));
        setMovieSearchTerm(movie.title);
        setShowMovieResults(false);
        setErrors(prev => ({ ...prev, movieId: '' }));
    }, []);

    const validateForm = useCallback((): boolean => {
        const newErrors: Record<string, string> = {};

        if (!formData.startTime) {
            newErrors.startTime = 'Start time is required';
        }

        const basePrice = Number(formData.basePrice);
        if (!formData.basePrice) {
            newErrors.basePrice = 'Price is required';
        } else if (isNaN(basePrice) || basePrice < 10) {
            newErrors.basePrice = 'Price must be at least 10 UAH';
        }

        if (!formData.movieId) newErrors.movieId = 'Movie is required';
        if (!formData.hallId) newErrors.hallId = 'Hall is required';

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    }, [formData]);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        const data: SessionRequest = {
            startTime: formData.startTime,
            basePrice: Number(formData.basePrice),
            movieId: Number(formData.movieId),
            hallId: Number(formData.hallId)
        };

        await onSave(data);
    }, [validateForm, formData, onSave]);

    const hallOptions = useMemo(() => [
        { value: '', label: 'Select a hall' },
        ...halls.map(hall => ({ value: hall.id.toString(), label: `${hall.name} (${hall.capacity} seats)` }))
    ], [halls]);

    const minDateTime = useMemo(() => {
        const now = new Date();
        now.setMinutes(now.getMinutes() + 30);
        return now.toISOString().slice(0, 16);
    }, []);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title} size="large">
            <form onSubmit={handleSubmit} className={styles.form}>
                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Start Time *</label>
                        <Input
                            type="datetime-local"
                            value={formData.startTime}
                            onChange={handleStartTimeChange}
                            error={errors.startTime}
                            min={minDateTime}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Base Price (UAH) *</label>
                        <Input
                            type="number"
                            step="0.01"
                            min="10"
                            value={formData.basePrice}
                            onChange={(value) => setFormData(prev => ({ ...prev, basePrice: value }))}
                            error={errors.basePrice}
                            placeholder="10.00"
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Movie *</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <Input
                            type="text"
                            value={movieSearchTerm}
                            onChange={(value) => {
                                setMovieSearchTerm(value);
                                handleMovieSearch(value);
                                hasLoadedRef.current = true;
                            }}
                            onClick={handleMovieClick}
                            placeholder={formData.startTime ? "Search movie..." : "Select start time first"}
                            disabled={!formData.startTime}
                            error={errors.movieId}
                        />

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
                                            <div className={styles.movieDetails}>{movie.durationMinutes} min</div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults}>
                                        {!formData.startTime
                                            ? 'Select start time first'
                                            : movieSearchTerm
                                                ? `No movies found for "${movieSearchTerm}"`
                                                : 'No movies available for this date'}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Hall *</label>
                    <Select
                        value={formData.hallId}
                        onChange={(value) => setFormData(prev => ({ ...prev, hallId: value.toString() }))}
                        options={hallOptions}
                    />
                    {errors.hallId && <span className={styles.errorText}>{errors.hallId}</span>}
                </div>

                <div className={styles.formActions}>
                    <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="success" disabled={loading} loading={loading}>
                        {submitText}
                    </Button>
                </div>
            </form>
        </Modal>
    );
};