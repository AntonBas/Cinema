import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import type {
    MovieDetailResponse,
    MovieCreateRequest,
    MovieUpdateRequest,
    AgeRating
} from '@/types/movie';
import type { GenreResponse } from '@/types/genre';
import type { PersonResponse } from '@/types/person';
import { movieApi } from '@/api/movieApi';
import { genreApi } from '@/api/genreApi';
import { toBackendFormat } from '@/utils/dateUtils';
import { PersonSelect } from './PersonSelect/PersonSelect';
import { GenreSearchList } from './GenreSearchList/GenreSearchList';
import { Button, Modal, LoadingSpinner } from '@/components/ui';
import { useNotification } from '@/hooks/common/useNotification';
import styles from './MovieForm.module.css';

interface MovieFormProps {
    movie?: MovieDetailResponse | null;
    onSuccess: (result?: MovieDetailResponse) => void;
    onCancel: () => void;
}

interface MovieFormData {
    title: string;
    trailerUrl: string;
    description: string;
    durationMinutes: number;
    releaseDate: Date | null;
    endShowingDate: Date | null;
    ageRating: AgeRating;
    selectedGenres: number[];
    selectedDirectors: number[];
    selectedScreenwriters: number[];
    selectedActors: number[];
    posterFile?: File;
    removePoster: boolean;
}

const AGE_RATING_OPTIONS = [
    { value: 'PEGI_3', label: 'PEGI 3' },
    { value: 'PEGI_7', label: 'PEGI 7' },
    { value: 'PEGI_12', label: 'PEGI 12' },
    { value: 'PEGI_16', label: 'PEGI 16' },
    { value: 'PEGI_18', label: 'PEGI 18' }
] as const;

const TITLE_MAX_LENGTH = 50;
const DESCRIPTION_MAX_LENGTH = 1000;

export const MovieForm: React.FC<MovieFormProps> = React.memo(({
    movie,
    onSuccess,
    onCancel
}) => {
    const { showNotification } = useNotification();
    const [genres, setGenres] = useState<GenreResponse[]>([]);
    const [selectedGenres, setSelectedGenres] = useState<GenreResponse[]>([]);
    const [selectedActors, setSelectedActors] = useState<PersonResponse[]>([]);
    const [selectedDirectors, setSelectedDirectors] = useState<PersonResponse[]>([]);
    const [selectedScreenwriters, setSelectedScreenwriters] = useState<PersonResponse[]>([]);
    const [isLoadingData, setIsLoadingData] = useState(true);
    const [isUploading, setIsUploading] = useState(false);
    const [posterPreview, setPosterPreview] = useState<string>('');
    const fileInputRef = useRef<HTMLInputElement>(null);
    const initialLoadRef = useRef(false);

    const [formData, setFormData] = useState<MovieFormData>({
        title: '',
        trailerUrl: '',
        description: '',
        durationMinutes: 0,
        releaseDate: null,
        endShowingDate: null,
        ageRating: 'PEGI_12',
        selectedGenres: [],
        selectedDirectors: [],
        selectedScreenwriters: [],
        selectedActors: [],
        posterFile: undefined,
        removePoster: false
    });

    const loadGenres = useCallback(async () => {
        try {
            const genresData = await genreApi.public.getPopular('', 100);
            setGenres(genresData);
        } catch (error) {
            console.error('Error loading genres:', error);
            showNotification('Failed to load genres', 'error');
        }
    }, [showNotification]);

    useEffect(() => {
        if (!initialLoadRef.current) {
            initialLoadRef.current = true;
            loadGenres();
        }
    }, [loadGenres]);

    useEffect(() => {
        if (movie) {
            setSelectedGenres(movie.genres || []);
            setSelectedActors(movie.actors || []);
            setSelectedDirectors(movie.directors || []);
            setSelectedScreenwriters(movie.screenwriters || []);

            setFormData({
                title: movie.title,
                trailerUrl: movie.trailerUrl,
                description: movie.description,
                durationMinutes: movie.durationMinutes,
                releaseDate: movie.releaseDate ? new Date(movie.releaseDate) : null,
                endShowingDate: movie.endShowingDate ? new Date(movie.endShowingDate) : null,
                ageRating: movie.ageRating,
                selectedGenres: movie.genres?.map(g => g.id) || [],
                selectedDirectors: movie.directors?.map(d => d.id) || [],
                selectedScreenwriters: movie.screenwriters?.map(s => s.id) || [],
                selectedActors: movie.actors?.map(a => a.id) || [],
                posterFile: undefined,
                removePoster: false
            });

            if (movie.posterUrl) {
                setPosterPreview(movie.posterUrl);
            }
        }
    }, [movie]);

    const handlePosterSelect = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
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
    }, []);

    const handleRemovePoster = useCallback(() => {
        setPosterPreview('');
        setFormData(prev => ({
            ...prev,
            posterFile: undefined,
            removePoster: true
        }));
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }
    }, []);

    const formatDateForInput = useCallback((date: Date | null): string => {
        if (!date) return '';
        return date.toISOString().split('T')[0];
    }, []);

    const formatDateForBackend = useCallback((date: Date | null): string => {
        if (!date) return '';
        return toBackendFormat(date.toISOString().split('T')[0]);
    }, []);

    const handleSubmit = useCallback(async (e: React.FormEvent) => {
        e.preventDefault();
        setIsUploading(true);

        try {
            let result;
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
                    removePoster: formData.removePoster,
                    posterFile: formData.posterFile
                };
                result = await movieApi.admin.update(movie.id, updateRequest);
            } else {
                if (!formData.posterFile) {
                    showNotification('Poster is required for new movie', 'error');
                    setIsUploading(false);
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
                    posterFile: formData.posterFile
                };
                result = await movieApi.admin.create(createRequest);
            }
            onSuccess(result);
        } catch (error) {
            console.error('Error saving movie:', error);
            showNotification('Failed to save movie', 'error');
        } finally {
            setIsUploading(false);
        }
    }, [movie, formData, formatDateForBackend, onSuccess, showNotification]);

    const handleGenreChange = useCallback((genreId: number) => {
        setFormData(prev => {
            const currentIds = prev.selectedGenres;
            const newIds = currentIds.includes(genreId)
                ? currentIds.filter(id => id !== genreId)
                : [...currentIds, genreId];
            return { ...prev, selectedGenres: newIds };
        });
    }, []);

    const handleActorsChange = useCallback((ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedActors: ids }));
    }, []);

    const handleDirectorsChange = useCallback((ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedDirectors: ids }));
    }, []);

    const handleScreenwritersChange = useCallback((ids: number[]) => {
        setFormData(prev => ({ ...prev, selectedScreenwriters: ids }));
    }, []);

    const selectedActorsIds = useMemo(() => formData.selectedActors, [formData.selectedActors]);
    const selectedDirectorsIds = useMemo(() => formData.selectedDirectors, [formData.selectedDirectors]);
    const selectedScreenwritersIds = useMemo(() => formData.selectedScreenwriters, [formData.selectedScreenwriters]);

    const titleRemaining = TITLE_MAX_LENGTH - formData.title.length;
    const descriptionRemaining = DESCRIPTION_MAX_LENGTH - formData.description.length;

    useEffect(() => {
        return () => {
            setIsLoadingData(false);
            setIsUploading(false);
        };
    }, []);

    if (isLoadingData) {
        return (
            <Modal isOpen={true} onClose={onCancel} title="Loading...">
                <div className={styles.loading}>
                    <LoadingSpinner text="Loading movie data..." />
                </div>
            </Modal>
        );
    }

    return (
        <Modal
            isOpen={true}
            onClose={onCancel}
            title={movie ? 'Edit Movie' : 'Add New Movie'}
            size="large"
        >
            <form onSubmit={handleSubmit} className={styles.form} noValidate>
                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Movie Poster {!movie && <span className={styles.required}>*</span>}
                    </label>
                    <div className={styles.fileUpload}>
                        <input
                            type="file"
                            ref={fileInputRef}
                            onChange={handlePosterSelect}
                            accept="image/jpeg,image/png,image/webp"
                            className={styles.fileInput}
                            required={!movie}
                            aria-label="Upload movie poster"
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
                                <span aria-hidden="true">📷</span>
                                <p>Click to upload poster</p>
                                <small>Recommended: 800x1200px, JPG/PNG/WebP</small>
                            </div>
                        )}
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Title <span className={styles.required}>*</span>
                    </label>
                    <input
                        type="text"
                        value={formData.title}
                        onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
                        required
                        className={styles.input}
                        placeholder="Enter movie title"
                        maxLength={TITLE_MAX_LENGTH}
                        autoFocus={!movie}
                    />
                    <div className={`${styles.charCount} ${titleRemaining < 10 ? styles.warning : ''}`}>
                        {titleRemaining} characters remaining
                    </div>
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Trailer URL <span className={styles.required}>*</span>
                    </label>
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
                    <label className={styles.label}>
                        Description <span className={styles.required}>*</span>
                    </label>
                    <textarea
                        value={formData.description}
                        onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                        required
                        rows={4}
                        className={styles.textarea}
                        maxLength={DESCRIPTION_MAX_LENGTH}
                        placeholder="Describe the movie plot, characters, and key elements"
                    />
                    <div className={`${styles.charCount} ${descriptionRemaining < 100 ? styles.warning : ''}`}>
                        {descriptionRemaining} characters remaining
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Duration (minutes) <span className={styles.required}>*</span>
                        </label>
                        <input
                            type="number"
                            value={formData.durationMinutes}
                            onChange={(e) => setFormData(prev => ({
                                ...prev,
                                durationMinutes: parseInt(e.target.value) || 0
                            }))}
                            required
                            min="1"
                            max="300"
                            className={styles.input}
                            placeholder="e.g., 120"
                        />
                    </div>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Age Rating <span className={styles.required}>*</span>
                        </label>
                        <select
                            value={formData.ageRating}
                            onChange={(e) => setFormData(prev => ({
                                ...prev,
                                ageRating: e.target.value as AgeRating
                            }))}
                            required
                            className={styles.select}
                        >
                            {AGE_RATING_OPTIONS.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className={styles.formRow}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>
                            Release Date <span className={styles.required}>*</span>
                        </label>
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
                        <label className={styles.label}>
                            End Showing Date <span className={styles.required}>*</span>
                        </label>
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
                    <label className={styles.label}>
                        Genres <span className={styles.required}>*</span>
                    </label>
                    <GenreSearchList
                        genres={genres}
                        selectedIds={formData.selectedGenres}
                        selectedGenres={selectedGenres}
                        onChange={handleGenreChange}
                    />
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Actors <span className={styles.required}>*</span>
                    </label>
                    <PersonSelect
                        selectedIds={selectedActorsIds}
                        selectedPersons={selectedActors}
                        onChange={handleActorsChange}
                        role="ACTOR"
                        placeholder="Search actors or add new..."
                    />
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Directors <span className={styles.required}>*</span>
                    </label>
                    <PersonSelect
                        selectedIds={selectedDirectorsIds}
                        selectedPersons={selectedDirectors}
                        onChange={handleDirectorsChange}
                        role="DIRECTOR"
                        placeholder="Search directors or add new..."
                    />
                </div>

                <div className={styles.formGroup}>
                    <label className={styles.label}>
                        Screenwriters <span className={styles.required}>*</span>
                    </label>
                    <PersonSelect
                        selectedIds={selectedScreenwritersIds}
                        selectedPersons={selectedScreenwriters}
                        onChange={handleScreenwritersChange}
                        role="SCREENWRITER"
                        placeholder="Search screenwriters or add new..."
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
                        disabled={isUploading}
                    >
                        Cancel
                    </Button>
                </div>
            </form>
        </Modal>
    );
});

MovieForm.displayName = 'MovieForm';