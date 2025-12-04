import React from 'react';
import { SearchInput, Select } from '@/components/ui';
import styles from './MovieFilters.module.css';

export interface FilterOptions {
    search: string;
    status: 'all' | 'current' | 'upcoming';
    sort: 'title' | 'releaseDate' | 'duration';
}

interface MovieFiltersProps {
    filters: FilterOptions;
    onFiltersChange: (filters: FilterOptions) => void;
}

export const MovieFilters: React.FC<MovieFiltersProps> = ({
    filters,
    onFiltersChange
}) => {
    const handleSearchChange = (search: string) => {
        onFiltersChange({ ...filters, search });
    };

    const handleStatusChange = (value: string | number) => {
        onFiltersChange({
            ...filters,
            status: value as FilterOptions['status']
        });
    };

    const handleSortChange = (value: string | number) => {
        onFiltersChange({
            ...filters,
            sort: value as FilterOptions['sort']
        });
    };

    return (
        <div className={styles.filters}>
            <div className={styles.searchSection}>
                <SearchInput
                    onSearch={handleSearchChange}
                    placeholder="Search movies..."
                    delay={500}
                    className={styles.searchInput}
                />
            </div>

            <div className={styles.filterSection}>
                <Select
                    options={[
                        { value: 'all', label: 'All Movies' },
                        { value: 'current', label: 'Now Playing' },
                        { value: 'upcoming', label: 'Coming Soon' }
                    ]}
                    value={filters.status}
                    onChange={handleStatusChange}
                    className={styles.filterSelect}
                />

                <Select
                    options={[
                        { value: 'title', label: 'Title' },
                        { value: 'releaseDate', label: 'Release Date' },
                        { value: 'duration', label: 'Duration' }
                    ]}
                    value={filters.sort}
                    onChange={handleSortChange}
                    className={styles.filterSelect}
                />
            </div>
        </div>
    );
};