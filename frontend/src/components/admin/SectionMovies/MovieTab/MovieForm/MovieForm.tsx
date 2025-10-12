import React, { useState, useEffect, useRef } from 'react';
import type { MovieDto, MovieFormData } from '@/types/Movie';
import { MovieStatus, AgeRating } from '@/types/Movie';
import type { GenreDto } from '@/types';
import { movieApi, movieFormHelper } from '@/api/movieApi';
import type { NotificationType } from '@/hooks/useNotification';
import { toBackendFormat, toDisplayFormat } from '@/utils/dateUtils';
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
        slug: '',
        trailerUrl: '',
        description: '',
        durationMinutes: 0,
        releaseDate: '',
        endShowingDate: '',
        status: MovieStatus.UPCOMING,
        ageRating: AgeRating.PEGI_12,
        genreIds: [],
        directorIds: [],
        screenwriterIds: [],
        castIds: []
    });

    useEffect(() => {
        const loadFormData = async () => {
            try {
                const genresData = await movieApi.getGenres();
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
                slug: movie.slug,
                trailerUrl: movie.trailerUrl,
                description: movie.description,
                durationMinutes: movie.durationMinutes,
                releaseDate: toDisplayFormat(movie.releaseDate),
                endShowingDate: toDisplayFormat(movie.endShowingDate),
                status: movie.status,
                ageRating: movie.ageRating,
                genreIds: movie.genreIds || [],
                directorIds: movie.directorIds || [],
                screenwriterIds: movie.screenwriterIds || [],
                castIds: movie.castIds || []
            });

            if (movie.posterFileName && movie.id) {
                const posterUrl = movieApi.getPosterUrl(movie.id);
                console.log('🖼️ Poster URL:', posterUrl);
                setPosterPreview(posterUrl);
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
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsUploading(true);

        try {
            const posterFile = fileInputRef.current?.files?.[0];
            const hasNewPoster = !!posterFile;

            if (movie?.id) {
                if (hasNewPoster) {
                    const movieData: MovieDto = {
                        ...movie,
                        ...formData,
                        releaseDate: toBackendFormat(formData.releaseDate),
                        endShowingDate: toBackendFormat(formData.endShowingDate),
                        posterFile: posterFile
                    };
                    const formDataForUpdate = movieFormHelper.updateFormData(movieData);
                    await movieApi.updateWithPoster(movie.id, formDataForUpdate);
                } else {
                    const movieData: MovieDto = {
                        ...movie,
                        ...formData,
                        releaseDate: toBackendFormat(formData.releaseDate),
                        endShowingDate: toBackendFormat(formData.endShowingDate)
                    };
                    await movieApi.update(movie.id, movieData);
                }
                showNotification('Movie updated successfully!', 'success');
            } else {
                const movieCreateData = {
                    ...formData,
                    releaseDate: toBackendFormat(formData.releaseDate),
                    endShowingDate: toBackendFormat(formData.endShowingDate),
                    posterFile: posterFile
                };
                const formDataForCreate = movieFormHelper.createFormData(movieCreateData);
                await movieApi.create(formDataForCreate);
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
            const currentIds = prev.genreIds || [];
            const newIds = currentIds.includes(genreId)
                ? currentIds.filter(id => id !== genreId)
                : [...currentIds, genreId];
            return { ...prev, genreIds: newIds };
        });
    };

    const handleCastChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, castIds: ids }));
    };

    const handleDirectorsChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, directorIds: ids }));
    };

    const handleScreenwritersChange = (ids: number[]) => {
        setFormData(prev => ({ ...prev, screenwriterIds: ids }));
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
                        <label className={styles.label}>Movie Poster</label>
                        <div className={styles.fileUpload}>
                            <input
                                type="file"
                                ref={fileInputRef}
                                onChange={handlePosterSelect}
                                accept="image/*"
                                className={styles.fileInput}
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
                                    <div className={styles.posterInfo}>
                                        {movie?.posterFileName && (
                                            <small>Current: {movie.posterFileName}</small>
                                        )}
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

                    <div className={styles.formRow}>
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
                            <label className={styles.label}>Slug *</label>
                            <input
                                type="text"
                                value={formData.slug}
                                onChange={(e) => setFormData({ ...formData, slug: e.target.value })}
                                required
                                pattern="[a-z0-9-]+"
                                className={styles.input}
                                title="Only lowercase letters, numbers and hyphens allowed"
                            />
                        </div>
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
                                value={toBackendFormat(formData.releaseDate)}
                                onChange={(e) => {
                                    const backendDate = e.target.value;
                                    const displayDate = toDisplayFormat(backendDate);
                                    setFormData({ ...formData, releaseDate: displayDate });
                                }}
                                required
                                className={styles.input}
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>End Showing Date *</label>
                            <input
                                type="date"
                                value={toBackendFormat(formData.endShowingDate)}
                                onChange={(e) => {
                                    const backendDate = e.target.value;
                                    const displayDate = toDisplayFormat(backendDate);
                                    setFormData({ ...formData, endShowingDate: displayDate });
                                }}
                                required
                                className={styles.input}
                            />
                        </div>
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Status *</label>
                        <select
                            value={formData.status}
                            onChange={(e) => setFormData({ ...formData, status: e.target.value as MovieStatus })}
                            required
                            className={styles.select}
                        >
                            <option value={MovieStatus.CURRENT}>🎬 Current - Currently Showing</option>
                            <option value={MovieStatus.UPCOMING}>📅 Upcoming - Coming Soon</option>
                            <option value={MovieStatus.ARCHIVED}>📦 Archived - No Longer Showing</option>
                        </select>
                    </div>

                    {/* <div className={styles.formGroup}>
                        <label className={styles.label}>Genres *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading genres...</div>
                        ) : (
                            <div className={styles.multiSelect}>
                                <div className={styles.selectedItems}>
                                    {formData.genreIds?.map(genreId => {
                                        const genre = genres.find(g => g.id === genreId);
                                        return genre ? (
                                            <span key={genreId} className={styles.selectedTag}>
                                                {genre.name}
                                                <button
                                                    type="button"
                                                    onClick={() => handleGenreChange(genreId)}
                                                    className={styles.removeTag}
                                                >
                                                    ×
                                                </button>
                                            </span>
                                        ) : null;
                                    })}
                                </div>
                                <div className={styles.selectOptions}>
                                    {genres.map(genre => (
                                        <label key={genre.id} className={styles.option}>
                                            <input
                                                type="checkbox"
                                                checked={formData.genreIds?.includes(genre.id!) || false}
                                                onChange={() => handleGenreChange(genre.id!)}
                                            />
                                            <span className={styles.checkmark}></span>
                                            {genre.name}
                                        </label>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div> */}

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Genres *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading genres...</div>
                        ) : (
                            <GenreSearchList
                                genres={genres}
                                selectedIds={formData.genreIds || []}
                                onChange={handleGenreChange}
                            />
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Cast *</label>
                        <PersonSelect
                            selectedIds={formData.castIds}
                            onChange={handleCastChange}
                            role={PersonRole.ACTOR}
                            placeholder="Search actors or add new..."
                            showNotification={showNotification}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Directors *</label>
                        <PersonSelect
                            selectedIds={formData.directorIds}
                            onChange={handleDirectorsChange}
                            role={PersonRole.DIRECTOR}
                            placeholder="Search directors or add new..."
                            showNotification={showNotification}
                        />
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Screenwriters *</label>
                        <PersonSelect
                            selectedIds={formData.screenwriterIds}
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