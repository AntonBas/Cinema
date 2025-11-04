import React, { useState, useEffect } from 'react';
import { useMovies, useHalls } from '@/hooks/features';
import type { SessionDto, SessionRequest } from '@/types/session';
import styles from './SessionModal.module.css';

interface SessionModalProps {
    isOpen: boolean;
    session: SessionDto | null;
    onSave: (data: SessionRequest) => void;
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
    const { movies, loading: moviesLoading } = useMovies();
    const { halls, loading: hallsLoading } = useHalls();

    const [formData, setFormData] = useState({
        startTime: '',
        price: '',
        movieId: '',
        hallId: ''
    });

    const [errors, setErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        if (session) {
            setFormData({
                startTime: session.startTime.slice(0, 16),
                price: session.price.toString(),
                movieId: session.movie.id.toString(),
                hallId: session.hall.id.toString()
            });
        } else {
            setFormData({
                startTime: '',
                price: '',
                movieId: '',
                hallId: ''
            });
        }
        setErrors({});
    }, [session, isOpen]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));

        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
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

        if (!formData.price || Number(formData.price) < 10) {
            newErrors.price = 'Price must be at least $10';
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

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!validateForm()) return;

        const sessionData: SessionRequest = {
            startTime: formData.startTime + ':00',
            price: Number(formData.price),
            movieId: Number(formData.movieId),
            hallId: Number(formData.hallId)
        };

        onSave(sessionData);
    };

    if (!isOpen) return null;

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modal}>
                <div className={styles.header}>
                    <h2>{session ? 'Edit Session' : 'Create Session'}</h2>
                    <button className={styles.closeButton} onClick={onClose}>×</button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label htmlFor="startTime">Start Time *</label>
                        <input
                            id="startTime"
                            name="startTime"
                            type="datetime-local"
                            value={formData.startTime}
                            onChange={handleChange}
                            className={errors.startTime ? styles.error : ''}
                        />
                        {errors.startTime && <span className={styles.errorText}>{errors.startTime}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="price">Price ($) *</label>
                        <input
                            id="price"
                            name="price"
                            type="number"
                            step="0.01"
                            min="10"
                            value={formData.price}
                            onChange={handleChange}
                            className={errors.price ? styles.error : ''}
                        />
                        {errors.price && <span className={styles.errorText}>{errors.price}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="movieId">Movie *</label>
                        <select
                            id="movieId"
                            name="movieId"
                            value={formData.movieId}
                            onChange={handleChange}
                            className={errors.movieId ? styles.error : ''}
                            disabled={moviesLoading}
                        >
                            <option value="">Select a movie</option>
                            {movies.map(movie => (
                                <option key={movie.id} value={movie.id}>
                                    {movie.title} ({movie.durationMinutes} min)
                                </option>
                            ))}
                        </select>
                        {errors.movieId && <span className={styles.errorText}>{errors.movieId}</span>}
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="hallId">Hall *</label>
                        <select
                            id="hallId"
                            name="hallId"
                            value={formData.hallId}
                            onChange={handleChange}
                            className={errors.hallId ? styles.error : ''}
                            disabled={hallsLoading}
                        >
                            <option value="">Select a hall</option>
                            {halls.map(hall => (
                                <option key={hall.id} value={hall.id}>
                                    {hall.name} ({hall.capacity} seats)
                                </option>
                            ))}
                        </select>
                        {errors.hallId && <span className={styles.errorText}>{errors.hallId}</span>}
                    </div>

                    <div className={styles.actions}>
                        <button
                            type="button"
                            onClick={onClose}
                            className={styles.cancelButton}
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            className={styles.saveButton}
                            disabled={loading || moviesLoading || hallsLoading}
                        >
                            {loading ? 'Saving...' : (session ? 'Update' : 'Create')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};