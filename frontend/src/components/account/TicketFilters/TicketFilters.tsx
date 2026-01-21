import React, { useState } from 'react';
import { Button, Input } from '@/components/ui';
import styles from './TicketFilters.module.css';

interface TicketFiltersProps {
    onSearch: (query: string) => void;
    onDateRangeChange: (from: string, to: string) => void;
    onClearFilters: () => void;
    hasFilters: boolean;
}

export const TicketFilters: React.FC<TicketFiltersProps> = ({
    onSearch,
    onDateRangeChange,
    onClearFilters,
    hasFilters
}) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo] = useState('');

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        onSearch(searchQuery);
    };

    const handleDateFilter = () => {
        if (dateFrom && dateTo) {
            onDateRangeChange(dateFrom, dateTo);
        }
    };

    const clearAll = () => {
        setSearchQuery('');
        setDateFrom('');
        setDateTo('');
        onClearFilters();
    };

    return (
        <div className={styles.filters}>
            <form onSubmit={handleSearch} className={styles.searchForm}>
                <Input
                    type="text"
                    placeholder="Search by movie, hall, ticket code..."
                    value={searchQuery}
                    onChange={setSearchQuery}
                />
                <Button type="submit" variant="primary" size="medium">
                    Search
                </Button>
            </form>

            <div className={styles.dateFilters}>
                <div className={styles.dateInputs}>
                    <Input
                        type="date"
                        placeholder="From"
                        value={dateFrom}
                        onChange={setDateFrom}
                    />
                    <Input
                        type="date"
                        placeholder="To"
                        value={dateTo}
                        onChange={setDateTo}
                    />
                    <Button
                        variant="secondary"
                        onClick={handleDateFilter}
                        disabled={!dateFrom || !dateTo}
                    >
                        Filter by Date
                    </Button>
                </div>
            </div>

            {hasFilters && (
                <Button
                    variant="cancel"
                    onClick={clearAll}
                    className={styles.clearButton}
                >
                    Clear All Filters
                </Button>
            )}
        </div>
    );
};