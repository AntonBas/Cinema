import React, { useState, useEffect, useCallback } from 'react';
import styles from './SearchInput.module.css';

export interface SearchInputProps {
    onSearch: (query: string) => void;
    placeholder?: string;
    delay?: number;
    className?: string;
    disabled?: boolean;
}
export const SearchInput: React.FC<SearchInputProps> = ({
    onSearch,
    placeholder = "Search...",
    delay = 300,
    className = '',
    disabled = false
}) => {
    const [query, setQuery] = useState('');
    const [isSearching, setIsSearching] = useState(false);

    const handleSearch = useCallback((searchQuery: string) => {
        onSearch(searchQuery);
        setIsSearching(false);
    }, [onSearch]);

    useEffect(() => {
        if (query.trim() === '') {
            handleSearch('');
            return;
        }

        setIsSearching(true);
        const timeoutId = setTimeout(() => {
            handleSearch(query);
        }, delay);

        return () => clearTimeout(timeoutId);
    }, [query, delay, handleSearch]);

    const handleClear = () => {
        setQuery('');
        handleSearch('');
    };

    return (
        <div className={`${styles.searchContainer} ${className}`}>
            <div className={styles.searchInputWrapper}>
                <span className={styles.searchIcon} aria-hidden="true">🔍</span>
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder={placeholder}
                    className={styles.searchInput}
                    aria-label="Search"
                    disabled={disabled}
                />
                {isSearching && <div className={styles.spinner} aria-label="Searching">⏳</div>}
                {query && !isSearching && (
                    <button
                        type="button"
                        onClick={handleClear}
                        className={styles.clearButton}
                        aria-label="Clear search"
                        disabled={disabled}
                    >
                        ×
                    </button>
                )}
            </div>
        </div>
    );
};