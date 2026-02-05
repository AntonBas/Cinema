import React, { useState, useEffect } from 'react';
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
    const [timer, setTimer] = useState<NodeJS.Timeout | null>(null);

    useEffect(() => {
        return () => {
            if (timer) {
                clearTimeout(timer);
            }
        };
    }, [timer]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newQuery = e.target.value;
        setQuery(newQuery);

        if (timer) {
            clearTimeout(timer);
        }

        const newTimer = setTimeout(() => {
            onSearch(newQuery);
        }, delay);

        setTimer(newTimer);
    };

    const handleClear = () => {
        setQuery('');
        onSearch('');
    };

    const inputId = `search-input-${Math.random().toString(36).substr(2, 9)}`;

    return (
        <div className={`${styles.searchContainer} ${className}`}>
            <div className={styles.searchInputWrapper}>
                <span className={styles.searchIcon} aria-hidden="true">🔍</span>
                <input
                    id={inputId}
                    type="text"
                    value={query}
                    onChange={handleChange}
                    placeholder={placeholder}
                    className={styles.searchInput}
                    aria-label="Search"
                    disabled={disabled}
                    name={inputId}
                    autoComplete="off"
                />
                {query && (
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