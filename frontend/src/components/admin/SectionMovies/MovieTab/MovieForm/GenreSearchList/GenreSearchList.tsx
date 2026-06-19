import React, { useState, useMemo, useCallback, useRef, useEffect } from 'react';
import type { GenreResponse } from '@/types/genre';
import { Input } from '@/components/ui/Input/Input';
import { Badge } from '@/components/ui/Badge/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './GenreSearchList.module.css';

interface GenreSearchListProps {
    genres?: GenreResponse[];
    selectedIds: number[];
    onChange: (genreId: number) => void;
    isLoading?: boolean;
}

export const GenreSearchList: React.FC<GenreSearchListProps> = React.memo(({
    genres = [],
    selectedIds,
    onChange,
    isLoading = false
}) => {
    const [localSearchQuery, setLocalSearchQuery] = useState('');
    const timeoutRef = useRef<number | null>(null);

    useEffect(() => {
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    const handleSearchChange = useCallback((value: string) => {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = window.setTimeout(() => {
            setLocalSearchQuery(value);
        }, 300);
    }, []);

    const filteredGenres = useMemo(() => {
        if (!localSearchQuery.trim()) return genres;
        const searchLower = localSearchQuery.toLowerCase();
        return genres.filter(genre =>
            genre.name.toLowerCase().includes(searchLower)
        );
    }, [genres, localSearchQuery]);

    const selectedGenres = useMemo(() => {
        return genres.filter(genre => selectedIds.includes(genre.id));
    }, [genres, selectedIds]);

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
                    placeholder="Type to filter genres..."
                    className={styles.searchInput}
                />
            </div>

            {selectedGenres.length > 0 && (
                <div className={styles.selectedItems}>
                    {selectedGenres.map(genre => (
                        <Badge
                            key={genre.id}
                            variant="primary"
                            onClick={() => onChange(genre.id)}
                            className={styles.selectedTag}
                        >
                            {genre.name} <span>×</span>
                        </Badge>
                    ))}
                </div>
            )}

            <div className={styles.genreList}>
                {filteredGenres.length > 0 ? (
                    filteredGenres.map(genre => {
                        const isSelected = selectedIds.includes(genre.id);
                        return (
                            <label
                                key={genre.id}
                                className={`${styles.option} ${isSelected ? styles.selected : ''}`}
                            >
                                <input
                                    type="checkbox"
                                    checked={isSelected}
                                    onChange={() => onChange(genre.id)}
                                    className={styles.checkbox}
                                />
                                <span className={styles.checkmark}></span>
                                <span className={styles.genreName}>{genre.name}</span>
                            </label>
                        );
                    })
                ) : (
                    <div className={styles.noResults}>
                        {localSearchQuery
                            ? `No genres found for "${localSearchQuery}"`
                            : 'No genres available'
                        }
                    </div>
                )}
            </div>
        </div>
    );
});

GenreSearchList.displayName = 'GenreSearchList';