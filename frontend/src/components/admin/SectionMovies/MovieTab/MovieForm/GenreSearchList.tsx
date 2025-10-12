import React, { useState, useMemo } from 'react';
import type { GenreDto } from '@/types/Genre';
import styles from './GenreSearchList.module.css';

interface GenreSearchListProps {
    genres: GenreDto[];
    selectedIds: number[];
    onChange: (genreId: number) => void;
    isLoading?: boolean;
}

export const GenreSearchList: React.FC<GenreSearchListProps> = ({
    genres,
    selectedIds,
    onChange,
    isLoading = false
}) => {
    const [searchQuery, setSearchQuery] = useState('');

    const filteredGenres = useMemo(() => {
        if (!searchQuery.trim()) {
            return genres;
        }
        return genres.filter(genre =>
            genre.name.toLowerCase().includes(searchQuery.toLowerCase())
        );
    }, [genres, searchQuery]);

    if (isLoading) {
        return <div className={styles.loading}>Loading genres...</div>;
    }

    return (
        <div className={styles.container}>
            <div className={styles.searchContainer}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search genres..."
                    className={styles.searchInput}
                />
            </div>

            {selectedIds.length > 0 && (
                <div className={styles.selectedItems}>
                    {selectedIds.map(genreId => {
                        const genre = genres.find(g => g.id === genreId);
                        return genre ? (
                            <span key={genreId} className={styles.selectedTag}>
                                {genre.name}
                                <button
                                    type="button"
                                    onClick={() => onChange(genreId)}
                                    className={styles.removeTag}
                                >
                                    ×
                                </button>
                            </span>
                        ) : null;
                    })}
                </div>
            )}

            <div className={styles.genreList}>
                {filteredGenres.map(genre => (
                    <label key={genre.id} className={styles.option}>
                        <input
                            type="checkbox"
                            checked={selectedIds.includes(genre.id!)}
                            onChange={() => onChange(genre.id!)}
                        />
                        <span className={styles.checkmark}></span>
                        {genre.name}
                    </label>
                ))}

                {filteredGenres.length === 0 && searchQuery && (
                    <div className={styles.noResults}>
                        No genres found for "{searchQuery}"
                    </div>
                )}
            </div>
        </div>
    );
};