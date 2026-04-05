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

const SEARCH_DELAY = 300;

export const GenreSearchList: React.FC<GenreSearchListProps> = React.memo(({
    genres = [],
    selectedIds,
    onChange,
    isLoading = false
}) => {
    const [localSearchQuery, setLocalSearchQuery] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const searchTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, []);

    const handleSearchChange = useCallback((value: string) => {
        setLocalSearchQuery(value);
        setIsTyping(true);

        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }

        searchTimeoutRef.current = setTimeout(() => {
            setIsTyping(false);
        }, SEARCH_DELAY);
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

    const handleGenreChange = useCallback((genreId: number) => {
        onChange(genreId);
    }, [onChange]);

    const hasSelected = selectedGenres.length > 0;

    if (isLoading) {
        return (
            <div className={styles.loading} role="status" aria-label="Loading genres">
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
                    aria-label="Search genres"
                />
                {isTyping && <span className={styles.typingIndicator} aria-hidden="true">...</span>}
            </div>

            {hasSelected && (
                <div className={styles.selectedItems} role="list" aria-label="Selected genres">
                    {selectedGenres.map(genre => (
                        <Badge
                            key={genre.id}
                            variant="primary"
                            onClick={() => handleGenreChange(genre.id)}
                            className={styles.selectedTag}
                            title={`Remove ${genre.name}`}
                        >
                            {genre.name} <span aria-hidden="true">×</span>
                        </Badge>
                    ))}
                </div>
            )}

            <div className={styles.genreList} role="listbox" aria-label="Available genres" aria-multiselectable="true">
                {filteredGenres.length > 0 ? (
                    filteredGenres.map(genre => {
                        const isSelected = selectedIds.includes(genre.id);
                        return (
                            <label
                                key={genre.id}
                                className={`${styles.option} ${isSelected ? styles.selected : ''}`}
                                role="option"
                                aria-selected={isSelected}
                            >
                                <input
                                    type="checkbox"
                                    checked={isSelected}
                                    onChange={() => handleGenreChange(genre.id)}
                                    className={styles.checkbox}
                                    aria-label={`Select ${genre.name}`}
                                />
                                <span className={styles.checkmark} aria-hidden="true"></span>
                                <span className={styles.genreName}>{genre.name}</span>
                            </label>
                        );
                    })
                ) : (
                    <div className={styles.noResults} role="status">
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