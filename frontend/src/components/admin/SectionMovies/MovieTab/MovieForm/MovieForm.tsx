import React, { useState, useEffect, useRef } from 'react';
import type { MovieDto, MovieFormData } from '@/types/Movie';
import { MovieStatus, AgeRating } from '@/types/Movie';
import type { GenreDto, PersonDto } from '@/types';
import { movieApi } from '@/api/movieApi';
import type { NotificationType } from '@/hooks/useNotification';
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
    const [persons, setPersons] = useState<PersonDto[]>([]);
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
        status: MovieStatus.ACTIVE,
        ageRating: AgeRating.G,
        genreIds: [],
        directorIds: [],
        screenwriterIds: [],
        castIds: []
    });

    useEffect(() => {
        const loadFormData = async () => {
            try {
                const [genresData, personsData] = await Promise.all([
                    movieApi.getGenres(),
                    movieApi.getPersons()
                ]);
                setGenres(genresData);
                setPersons(personsData);
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
                releaseDate: movie.releaseDate,
                endShowingDate: movie.endShowingDate,
                status: movie.status,
                ageRating: movie.ageRating,
                genreIds: movie.genreIds || [],
                directorIds: movie.directorIds || [],
                screenwriterIds: movie.screenwriterIds || [],
                castIds: movie.castIds || []
            });
            setPosterPreview(movie.posterFileName ? `${movieApi.getPoster(movie.id!)}?t=${Date.now()}` : '');
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
            const formDataToSend = new FormData();

            const movieData = {
                title: formData.title,
                slug: formData.slug,
                trailerUrl: formData.trailerUrl,
                description: formData.description,
                durationMinutes: formData.durationMinutes,
                releaseDate: formData.releaseDate,
                endShowingDate: formData.endShowingDate,
                status: formData.status,
                ageRating: formData.ageRating,
                genreIds: formData.genreIds,
                directorIds: formData.directorIds,
                screenwriterIds: formData.screenwriterIds,
                castIds: formData.castIds
            };

            formDataToSend.append('movie', new Blob([JSON.stringify(movieData)], {
                type: 'application/json'
            }));

            const posterFile = fileInputRef.current?.files?.[0];
            if (posterFile) {
                formDataToSend.append('posterFile', posterFile);
            }

            if (movie?.id) {
                await movieApi.update(movie.id, formDataToSend);
                showNotification('Movie updated successfully!', 'success');
            } else {
                await movieApi.create(formDataToSend);
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
            const currentIds = prev.genreIds || [];
            const newIds = currentIds.includes(genreId)
                ? currentIds.filter(id => id !== genreId)
                : [...currentIds, genreId];
            return { ...prev, genreIds: newIds };
        });
    };

    const handlePersonChange = (personId: number, field: 'castIds' | 'directorIds' | 'screenwriterIds') => {
        setFormData(prev => {
            const currentIds = prev[field] || [];
            const newIds = currentIds.includes(personId)
                ? currentIds.filter(id => id !== personId)
                : [...currentIds, personId];
            return { ...prev, [field]: newIds };
        });
    };

    const getActors = () => persons.filter(person => person.role === 'ACTOR');
    const getDirectors = () => persons.filter(person => person.role === 'DIRECTOR');
    const getScreenwriters = () => persons.filter(person => person.role === 'SCREENWRITER');

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
                                    <img src={posterPreview} alt="Poster preview" />
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
                                <option value={AgeRating.G}>G - General Audiences</option>
                                <option value={AgeRating.PG}>PG - Parental Guidance</option>
                                <option value={AgeRating.PG13}>PG-13 - Parents Strongly Cautioned</option>
                                <option value={AgeRating.R}>R - Restricted</option>
                                <option value={AgeRating.NC17}>NC-17 - Adults Only</option>
                            </select>
                        </div>
                    </div>

                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>Release Date *</label>
                            <input
                                type="date"
                                value={formData.releaseDate}
                                onChange={(e) => setFormData({ ...formData, releaseDate: e.target.value })}
                                required
                                className={styles.input}
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.label}>End Showing Date *</label>
                            <input
                                type="date"
                                value={formData.endShowingDate}
                                onChange={(e) => setFormData({ ...formData, endShowingDate: e.target.value })}
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
                            <option value={MovieStatus.ACTIVE}>🎬 Active - Currently Showing</option>
                            <option value={MovieStatus.UPCOMING}>📅 Upcoming - Coming Soon</option>
                            <option value={MovieStatus.ARCHIVED}>📦 Archived - No Longer Showing</option>
                        </select>
                    </div>

                    <div className={styles.formGroup}>
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
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Cast *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading cast...</div>
                        ) : (
                            <div className={styles.multiSelect}>
                                <div className={styles.selectedItems}>
                                    {formData.castIds?.map(personId => {
                                        const person = persons.find(p => p.id === personId);
                                        return person ? (
                                            <span key={personId} className={styles.selectedTag}>
                                                {person.name}
                                                <button
                                                    type="button"
                                                    onClick={() => handlePersonChange(personId, 'castIds')}
                                                    className={styles.removeTag}
                                                >
                                                    ×
                                                </button>
                                            </span>
                                        ) : null;
                                    })}
                                </div>
                                <div className={styles.selectOptions}>
                                    {getActors().map(person => (
                                        <label key={person.id} className={styles.option}>
                                            <input
                                                type="checkbox"
                                                checked={formData.castIds?.includes(person.id!) || false}
                                                onChange={() => handlePersonChange(person.id!, 'castIds')}
                                            />
                                            <span className={styles.checkmark}></span>
                                            {person.name}
                                        </label>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Directors *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading directors...</div>
                        ) : (
                            <div className={styles.multiSelect}>
                                <div className={styles.selectedItems}>
                                    {formData.directorIds?.map(personId => {
                                        const person = persons.find(p => p.id === personId);
                                        return person ? (
                                            <span key={personId} className={styles.selectedTag}>
                                                {person.name}
                                                <button
                                                    type="button"
                                                    onClick={() => handlePersonChange(personId, 'directorIds')}
                                                    className={styles.removeTag}
                                                >
                                                    ×
                                                </button>
                                            </span>
                                        ) : null;
                                    })}
                                </div>
                                <div className={styles.selectOptions}>
                                    {getDirectors().map(person => (
                                        <label key={person.id} className={styles.option}>
                                            <input
                                                type="checkbox"
                                                checked={formData.directorIds?.includes(person.id!) || false}
                                                onChange={() => handlePersonChange(person.id!, 'directorIds')}
                                            />
                                            <span className={styles.checkmark}></span>
                                            {person.name}
                                        </label>
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>

                    <div className={styles.formGroup}>
                        <label className={styles.label}>Screenwriters *</label>
                        {isLoadingData ? (
                            <div className={styles.loading}>Loading screenwriters...</div>
                        ) : (
                            <div className={styles.multiSelect}>
                                <div className={styles.selectedItems}>
                                    {formData.screenwriterIds?.map(personId => {
                                        const person = persons.find(p => p.id === personId);
                                        return person ? (
                                            <span key={personId} className={styles.selectedTag}>
                                                {person.name}
                                                <button
                                                    type="button"
                                                    onClick={() => handlePersonChange(personId, 'screenwriterIds')}
                                                    className={styles.removeTag}
                                                >
                                                    ×
                                                </button>
                                            </span>
                                        ) : null;
                                    })}
                                </div>
                                <div className={styles.selectOptions}>
                                    {getScreenwriters().map(person => (
                                        <label key={person.id} className={styles.option}>
                                            <input
                                                type="checkbox"
                                                checked={formData.screenwriterIds?.includes(person.id!) || false}
                                                onChange={() => handlePersonChange(person.id!, 'screenwriterIds')}
                                            />
                                            <span className={styles.checkmark}></span>
                                            {person.name}
                                        </label>
                                    ))}
                                </div>
                            </div>
                        )}
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