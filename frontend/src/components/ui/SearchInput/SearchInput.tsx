import React, { useState, useEffect } from 'react';
import styles from './SearchInput.module.css';

export interface SearchInputProps {
    onSearch: (query: string) => void;
    placeholder?: string;
    delay?: number;
    className?: string;
}

export const SearchInput: React.FC<SearchInputProps> = ({
    onSearch,
    placeholder = "Search...",
    delay = 300,
    className = ''
}) => {
    const [query, setQuery] = useState('');
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        if (query.trim() === '') {
            onSearch('');
            setIsSearching(false);
            return;
        }

        setIsSearching(true);
        const timeoutId = setTimeout(() => {
            onSearch(query);
            setIsSearching(false);
        }, delay);

        return () => clearTimeout(timeoutId);
    }, [query, delay, onSearch]);

    const handleClear = () => {
        setQuery('');
        onSearch('');
    };

    return (
        <div className={`${styles.searchContainer} ${className}`}>
            <div className={styles.searchInputWrapper}>
                <span className={styles.searchIcon}>🔍</span>
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder={placeholder}
                    className={styles.searchInput}
                />
                {isSearching && <div className={styles.spinner}>⏳</div>}
                {query && !isSearching && (
                    <button
                        type="button"
                        onClick={handleClear}
                        className={styles.clearButton}
                    >
                        ×
                    </button>
                )}
            </div>
        </div>
    );
};