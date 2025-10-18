import React, { useState, useMemo } from 'react';
import type { GenreDto } from '@/types/Genre';
import styles from './GenreSearchList.module.css';

interface GenreSearchListProps {
    genres?: GenreDto[];
    selectedIds: number[];
    onChange: (genreId: number) => void;
    onSearchChange?: (query: string) => void;
    isLoading?: boolean;
}

export const GenreSearchList: React.FC<GenreSearchListProps> = ({
    genres = [],
    selectedIds,
    onChange,
    onSearchChange,
    isLoading = false
}) => {
    const [localSearchQuery, setLocalSearchQuery] = useState('');

    const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const query = e.target.value;
        setLocalSearchQuery(query);
        onSearchChange?.(query);
    };

    const filteredGenres = useMemo(() => {
        const safeGenres = Array.isArray(genres) ? genres : [];

        if (!localSearchQuery.trim()) {
            return safeGenres;
        }
        return safeGenres.filter(genre =>
            genre.name.toLowerCase().includes(localSearchQuery.toLowerCase())
        );
    }, [genres, localSearchQuery]);

    if (isLoading) {
        return <div className={styles.loading}>Loading genres...</div>;
    }

    return (
        <div className={styles.container}>
            <div className={styles.searchContainer}>
                <input
                    type="text"
                    value={localSearchQuery}
                    onChange={handleSearchChange}
                    placeholder="Type 'comedy' to search genres..."
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

                {filteredGenres.length === 0 && localSearchQuery && (
                    <div className={styles.noResults}>
                        No genres found for "{localSearchQuery}"
                    </div>
                )}

                {filteredGenres.length === 0 && !localSearchQuery && (
                    <div className={styles.noResults}>
                        No genres available
                    </div>
                )}
            </div>
        </div>
    );
};