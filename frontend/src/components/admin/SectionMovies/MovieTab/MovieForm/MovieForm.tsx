import React, { useState, useEffect, useRef } from 'react';
import type { MovieDto, MovieFormData, MovieCreateRequest, MovieUpdateRequest } from '@/types/Movie';
import { AgeRating } from '@/types/Movie';
import type { GenreDto } from '@/types/Genre';
import { movieApi } from '@/api/movieApi';
import { genreApi } from '@/api/genreApi';
import type { NotificationType } from '@/hooks/useNotification';
import { toBackendFormat } from '@/utils/dateUtils';
import { PersonSelect } from '@/components/common/PersonSelect/PersonSelect';
import { PersonRole } from '@/types/Person';
import { GenreSearchList } from './GenreSearchList';
import styles from './MovieForm.module.css';

interface MovieFormProps {
    movie?: MovieDto | null;
    onSuccess: () => void;
    onCancel: () => void;
    showNotification: (message: string, type?: NotificationType) => void;
}

export const MovieForm: React.FC<MovieFormProps> = ({
    movie,
    onSuccess,
    onCancel,
    showNotification
}) => {
    const [genres, setGenres] = useState<GenreDto[]>([]);
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isUploading, setIsUploading] = useState(false);
    const [posterPreview, setPosterPreview] = useState<string>('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    const [formData, setFormData] = useState<MovieFormData>({
        title: '',
        trailerUrl: '',
        description: '',
        durationMinutes: 0,
        releaseDate: null,
        endShowingDate: null,
        ageRating: AgeRating.PEGI_12,
        selectedGenres: [],
        selectedDirectors: [],
        selectedScreenwriters: [],
        selectedActors: [],
        posterFile: undefined,
        removePoster: false
    });

    useEffect(() => {
        const loadFormData = async () => {
            try {
                const genresData = await genreApi.getAll();
                setGenres(genresData);
            } catch (error) {
                console.error('Error loading form data:', error);
                showNotification('Failed to load form data', 'error');
            } finally {
                setIsLoadingData(false);
            }
        };

        loadFormData();
    }, [showNotification]);

    useEffect(() => {
        if (movie) {
            setFormData({
                title: movie.title,
                trailerUrl: movie.trailerUrl,
                description: movie.description,
                durationMinutes: movie.durationMinutes,
                releaseDate: movie.releaseDate ? new Date(movie.releaseDate) : null,
                endShowingDate: movie.endShowingDate ? new Date(movie.endShowingDate) : null,
                ageRating: movie.ageRating,
                selectedGenres: movie.genreIds || [],
                selectedDirectors: movie.directorIds || [],
                selectedScreenwriters: movie.screenwriterIds || [],
                selectedActors: movie.actorIds || [],
                posterFile: undefined,
                removePoster: false
            });

            if (movie.posterUrl) {
                setPosterPreview(movie.posterUrl);
            } else {
                setPosterPreview('');
            }
        }
    }, [movie]);

    const handlePosterSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            const previewUrl = URL.createObjectURL(file);
            setPosterPreview(previewUrl);
            setFormData(prev => ({
                ...prev,
                posterFile: file,
                removePoster: false
            }));
        }
    };

    const handleRemovePoster = () => {
        setPosterPreview('');
        setFormData(prev => ({
            ...prev,
            posterFile: undefined,
            removePoster: true
        }));
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    };

    const formatDateForInput = (date: Date | null): string => {
        if (!date) return '';
        return date.toISOString().split('T')[0];
    };

    const formatDateForBackend = (date: Date | null): string => {
        if (!date) return '';
        return toBackendFormat(date.toISOString().split('T')[0]);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsUploading(true);

        try {
            const posterFile = fileInputRef.current?.files?.[0];
            const hasNewPoster = !!posterFile;

            if (movie?.id) {
                const updateRequest: MovieUpdateRequest = {
                    title: formData.title,
                    trailerUrl: formData.trailerUrl,
                    description: formData.description,
                    durationMinutes: formData.durationMinutes,
                    releaseDate: formatDateForBackend(formData.releaseDate),
                    endShowingDate: formatDateForBackend(formData.endShowingDate),
                    ageRating: formData.ageRating,
                    genreIds: formData.selectedGenres,
                    directorIds: formData.selectedDirectors,
                    screenwriterIds: formData.selectedScreenwriters,
                    actorIds: formData.selectedActors,
                    removePoster: formData.removePoster
                };

                await movieApi.update(movie.id, updateRequest, posterFile);
                showNotification('Movie updated successfully!', 'success');
            } else {
                if (!posterFile) {
                    showNotification('Poster is required for new movie', 'error');
                    return;
                }

                const createRequest: MovieCreateRequest = {
                    title: formData.title,
                    trailerUrl: formData.trailerUrl,
                    description: formData.description,
                    durationMinutes: formData.durationMinutes,
                    releaseDate: formatDateForBackend(formData.releaseDate),
                    endShowingDate: formatDateForBackend(formData.endShowingDate),
                    ageRating: formData.ageRating,
                    genreIds: formData.selectedGenres,
                    directorIds: formData.selectedDirectors,
                    screenwriterIds: formData.selectedScreenwriters,
                    actorIds: formData.selectedActors,
                    posterFile: posterFile
                };
                await movieApi.create(createRequest, posterFile);
                showNotification('Movie created successfully!', 'success');
            }

            onSuccess();
        } catch (error) {
            console.error('❌ Error saving movie:', error);
            showNotification('Error saving movie. Please try again.', 'error');
        } finally {
            setIsUploading(false);
        }
    };

    const handleGenreChange = (genreId: number) => {
        setFormData(prev => {
            const currentIds = prev.selectedGenres;
            const newIds = currentIds.includes(genreId)
                ? currentIds.filter(id => id !== genreId)
                : [...currentIds, genreId];
            return { ...prev, selectedGenres: newIds };
        });
    };

    const handleActorsChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedActors: ids }));
    };

    const handleDirectorsChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedDirectors: ids }));
    };

    const handleScreenwritersChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedScreenwriters: ids }));
    };

    return (
        <div className={styles.overlay}>
            <div className={styles.modal}>
                <div className={styles.header}>
                    <h2>{movie ? 'Edit Movie' : 'Add New Movie'}</h2>
                    <button className={styles.closeButton} onClick={onCancel}>×</button>
                </div>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Movie Poster {!movie && '*'}</label>
                        <div className={styles.fileUpload}>
                            <input
                                type="file"
                                ref={fileInputRef}
                                onChange={handlePosterSelect}
                                accept="image/*"
                                className={styles.fileInput}
                                required={!movie}
                            />
                            {posterPreview ? (
                                <div className={styles.posterPreview}>
                                    <img
                                        src={posterPreview}
                                        alt="Poster preview"
                                        onError={(e) => {
                                            e.currentTarget.src = '/images/default-movie-poster.svg';
                                        }}
                                    />
                                    <div className={styles.posterActions}>
                                        <button
                                            type="button"
                                            onClick={handleRemovePoster}
                                            className={styles.removePosterButton}
                                        >
                                            Remove Poster
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <div className={styles.uploadPlaceholder}>
                                    <span>📷</span>
                                    <p>Click to upload poster</p>
                                    <small>Recommended: 800x1200px, JPG/PNG</small>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Title *</label>
                        <input
                            type="text"
                            value={formData.title}
                            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                            required
                            className={styles.input}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Trailer URL *</label>
                        <input
                            type="url"
                            value={formData.trailerUrl}
                            onChange={(e) => setFormData({ ...formData, trailerUrl: e.target.value })}
                            required
                            className={styles.input}
                            placeholder="https://www.youtube.com/watch?v=..."
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Description *</label>
                        <textarea
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            required
                            rows={4}
                            className={styles.textarea}
                            maxLength={1000}
                        />
                        <div className={styles.charCount}>{formData.description.length}/1000</div>
                    </div>

                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>Duration (minutes) *</label>
                            <input
                                type="number"
                                value={formData.durationMinutes}
                                onChange={(e) => setFormData({ ...formData, durationMinutes: parseInt(e.target.value) || 0 })}
                                required
                                min="1"
                                className={styles.input}
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>Age Rating *</label>
                            <select
                                value={formData.ageRating}
                                onChange={(e) => setFormData({ ...formData, ageRating: e.target.value as AgeRating })}
                                required
                                className={styles.select}
                            >
                                <option value={AgeRating.PEGI_3}>PEGI 3 - Suitable for all ages</option>
                                <option value={AgeRating.PEGI_7}>PEGI 7 - May contain mild violence</option>
                                <option value={AgeRating.PEGI_12}>PEGI 12 - Recommended for 12+</option>
                                <option value={AgeRating.PEGI_16}>PEGI 16 - Suitable only for 16+</option>
                                <option value={AgeRating.PEGI_18}>PEGI 18 - Adults only (18+)</option>
                            </select>
                        </div>
                    </div>

                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>Release Date *</label>
                            <input
                                type="date"
                                value={formatDateForInput(formData.releaseDate)}
                                onChange={(e) => {
                                    const date = e.target.value ? new Date(e.target.value) : null;
                                    setFormData({ ...formData, releaseDate: date });
                                }}
                                required
                                className={styles.input}
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>End Showing Date *</label>
                            <input
                                type="date"
                                value={formatDateForInput(formData.endShowingDate)}
                                onChange={(e) => {
                                    const date = e.target.value ? new Date(e.target.value) : null;
                                    setFormData({ ...formData, endShowingDate: date });
                                }}
                                required
                                className={styles.input}
                            />
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Genres *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading genres...</div>
                        ) : (
                            <GenreSearchList
                                genres={genres}
                                selectedIds={formData.selectedGenres}
                                onChange={handleGenreChange}
                            />
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Actors *</label>
                        <PersonSelect
                            selectedIds={formData.selectedActors}
                            onChange={handleActorsChange}
                            role={PersonRole.ACTOR}
                            placeholder="Search actors or add new..."
                            showNotification={showNotification}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Directors *</label>
                        <PersonSelect
                            selectedIds={formData.selectedDirectors}
                            onChange={handleDirectorsChange}
                            role={PersonRole.DIRECTOR}
                            placeholder="Search directors or add new..."
                            showNotification={showNotification}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Screenwriters *</label>
                        <PersonSelect
                            selectedIds={formData.selectedScreenwriters}
                            onChange={handleScreenwritersChange}
                            role={PersonRole.SCREENWRITER}
                            placeholder="Search screenwriters or add new..."
                            showNotification={showNotification}
                        />
                    </div>

                    <div className={styles.actions}>
                        <button type="submit" className={styles.primaryButton} disabled={isUploading}>
                            {isUploading ? '⏳ Saving...' : (movie ? '💾 Update Movie' : '🎬 Create Movie')}
                        </button>
                        <button type="button" onClick={onCancel} className={styles.secondaryButton}>
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};