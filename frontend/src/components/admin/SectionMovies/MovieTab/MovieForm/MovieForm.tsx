import React, { useState, useEffect, useRef } from 'react';
import type { MovieDetailResponse, MovieFormData, MovieCreateRequest, MovieUpdateRequest } from '@/types/movie';
import { AgeRating } from '@/types/movie';
import type { GenreResponse } from '@/types/genre';
import { movieApi } from '@/api/movieApi';
import { genreApi } from '@/api/genreApi';
import type { NotificationType } from '@/hooks/common/useNotification';
import { toBackendFormat } from '@/utils/dateUtils';
import { PersonSelect } from '@/components/admin/SectionMovies/MovieTab/MovieForm/PersonSelect/PersonSelect';
import { PersonRole } from '@/types/person';
import { GenreSearchList } from './GenreSearchList';
import { Button, Modal, LoadingSpinner } from '@/components/ui';
import styles from './MovieForm.module.css';

interface MovieFormProps {
    movie?: MovieDetailResponse | null;
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
    const [genres, setGenres] = useState<GenreResponse[]>([]);
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
            console.error('Error saving movie:', error);
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

    const ageRatingOptions = [
        { value: AgeRating.PEGI_3, label: 'PEGI 3' },
        { value: AgeRating.PEGI_7, label: 'PEGI 7' },
        { value: AgeRating.PEGI_12, label: 'PEGI 12' },
        { value: AgeRating.PEGI_16, label: 'PEGI 16' },
        { value: AgeRating.PEGI_18, label: 'PEGI 18' }
    ];

    return (
        <Modal
            isOpen={true}
            onClose={onCancel}
            title={movie ? 'Edit Movie' : 'Add New Movie'}
            size="large"
        >
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
                                    <Button
                                        type="button"
                                        variant="error"
                                        size="small"
                                        onClick={handleRemovePoster}
                                    >
                                        Remove Poster
                                    </Button>
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
                        onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                        required
                        className={styles.input}
                        placeholder="Enter movie title"
                        maxLength={50}
                    />
                    <div className={styles.charCount}>
                        {formData.title.length}/50 characters
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Trailer URL *</label>
                    <input
                        type="url"
                        value={formData.trailerUrl}
                        onChange={(e) => setFormData(prev => ({ ...prev, trailerUrl: e.target.value }))}
                        required
                        className={styles.input}
                        placeholder="https://www.youtube.com/watch?v=..."
                        pattern="https://.*"
                    />
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Description *</label>
                    <textarea
                        value={formData.description}
                        onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                        required
                        rows={4}
                        className={styles.textarea}
                        maxLength={1000}
                        placeholder="Describe the movie plot, characters, and key elements"
                    />
                    <div className={styles.charCount}>
                        {formData.description.length}/1000 characters
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Duration (minutes) *</label>
                        <input
                            type="number"
                            value={formData.durationMinutes}
                            onChange={(e) => setFormData(prev => ({ ...prev, durationMinutes: parseInt(e.target.value) || 0 }))}
                            required
                            min="1"
                            max="300"
                            className={styles.input}
                            placeholder="e.g., 120"
                        />
                    </div>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Age Rating *</label>
                        <select
                            value={formData.ageRating}
                            onChange={(e) => setFormData(prev => ({ ...prev, ageRating: e.target.value as AgeRating }))}
                            required
                            className={styles.select}
                        >
                            {ageRatingOptions.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
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
                                setFormData(prev => ({ ...prev, releaseDate: date }));
                            }}
                            required
                            className={styles.input}
                            min={new Date().toISOString().split('T')[0]}
                        />
                    </div>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>End Showing Date *</label>
                        <input
                            type="date"
                            value={formatDateForInput(formData.endShowingDate)}
                            onChange={(e) => {
                                const date = e.target.value ? new Date(e.target.value) : null;
                                setFormData(prev => ({ ...prev, endShowingDate: date }));
                            }}
                            required
                            className={styles.input}
                            min={formatDateForInput(formData.releaseDate)}
                        />
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>Genres *</label>
                    {isLoadingData ? (
                        <div className={styles.loading}>
                            <LoadingSpinner text="Loading genres..." />
                        </div>
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
                    <Button
                        type="submit"
                        variant="primary"
                        loading={isUploading}
                        disabled={isUploading}
                    >
                        {movie ? 'Update Movie' : 'Create Movie'}
                    </Button>
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={onCancel}
                    >
                        Cancel
                    </Button>
                </div>
            </form>
        </Modal>
    );
};