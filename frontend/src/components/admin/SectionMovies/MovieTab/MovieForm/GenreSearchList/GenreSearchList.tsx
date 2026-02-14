import React, { useState, useMemo } from 'react';
import type { GenreResponse } from '@/types/genre';
import { Input, Badge, LoadingSpinner } from '@/components/ui';
import styles from './GenreSearchList.module.css';

interface GenreSearchListProps {
    genres?: GenreResponse[];
    selectedIds: number[];
    selectedGenres?: GenreResponse[];
    onChange: (genreId: number) => void;
    onSearchChange?: (query: string) => void;
    isLoading?: boolean;
}

export const GenreSearchList: React.FC<GenreSearchListProps> = ({
    genres = [],
    selectedIds,
    selectedGenres = [],
    onChange,
    onSearchChange,
    isLoading = false
}) => {
    const [localSearchQuery, setLocalSearchQuery] = useState('');

    const handleSearchChange = (value: string) => {
        setLocalSearchQuery(value);
        onSearchChange?.(value);
    };

    const filteredGenres = useMemo(() => {
        const safeGenres = Array.isArray(genres) ? genres : [];
        if (!localSearchQuery.trim()) return safeGenres;
        return safeGenres.filter(genre =>
            genre.name.toLowerCase().includes(localSearchQuery.toLowerCase())
        );
    }, [genres, localSearchQuery]);

    const displaySelectedGenres = useMemo(() => {
        return selectedGenres.filter(genre => selectedIds.includes(genre.id));
    }, [selectedGenres, selectedIds]);

    if (isLoading) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading genres..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.searchContainer}>
                <Input
                    type="text"
                    value={localSearchQuery}
                    onChange={handleSearchChange}
                    placeholder="Type 'comedy' to search genres..."
                    className={styles.searchInput}
                />
            </div>

            {displaySelectedGenres.length > 0 && (
                <div className={styles.selectedItems}>
                    {displaySelectedGenres.map(genre => (
                        <Badge
                            key={genre.id}
                            variant="primary"
                            onClick={() => onChange(genre.id)}
                            className={styles.selectedTag}
                            title={`Remove ${genre.name}`}
                        >
                            {genre.name} ×
                        </Badge>
                    ))}
                </div>
            )}

            <div className={styles.genreList}>
                {filteredGenres.map(genre => (
                    <label key={genre.id} className={styles.option}>
                        <input
                            type="checkbox"
                            checked={selectedIds.includes(genre.id)}
                            onChange={() => onChange(genre.id)}
                        />
                        <span className={styles.checkmark}></span>
                        {genre.name}
                        {selectedIds.includes(genre.id) && (
                            <span className={styles.selectedIndicator}>(selected)</span>
                        )}
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