import React, { useState, useCallback, useRef } from "react";
import styles from "./SearchInput.module.css";

export interface SearchInputProps {
  onSearch: (query: string) => void;
  value?: string;
  placeholder?: string;
  delay?: number;
  className?: string;
  disabled?: boolean;
  id?: string;
}

export const SearchInput: React.FC<SearchInputProps> = ({
  onSearch,
  value = "",
  placeholder = "Search...",
  delay = 300,
  className = "",
  disabled = false,
  id,
}) => {
  const [query, setQuery] = useState(value);
  const [syncedValue, setSyncedValue] = useState(value);

  if (value !== syncedValue) {
    setSyncedValue(value);
    if (value !== query.trim()) {
      setQuery(value);
    }
  }
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const newQuery = e.target.value;
      setQuery(newQuery);

      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }

      timerRef.current = setTimeout(() => {
        onSearch(newQuery);
      }, delay);
    },
    [delay, onSearch],
  );

  const handleClear = useCallback(() => {
    setQuery("");
    onSearch("");

    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
  }, [onSearch]);

  return (
    <div className={`${styles.searchContainer} ${className}`}>
      <div className={styles.searchInputWrapper}>
        <input
          id={id}
          type="text"
          value={query}
          onChange={handleChange}
          placeholder={placeholder}
          className={styles.searchInput}
          aria-label="Search"
          disabled={disabled}
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
