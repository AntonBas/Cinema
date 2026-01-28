import React, { useState } from 'react';
import { Button, Input } from '@/components/ui';
import { Filter, Calendar, X } from 'lucide-react';
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
    const [showDateFilters, setShowDateFilters] = useState(false);

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
        setShowDateFilters(false);
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
                    className={styles.searchInput}
                />
                <Button type="submit" variant="secondary" size="medium">
                    <Filter size={16} /> Search
                </Button>
            </form>

            <div className={styles.advancedFilters}>
                <Button
                    variant="outline"
                    size="small"
                    onClick={() => setShowDateFilters(!showDateFilters)}
                >
                    <Calendar size={16} /> {showDateFilters ? 'Hide Date Filters' : 'Date Filters'}
                </Button>

                {showDateFilters && (
                    <div className={styles.dateFilters}>
                        <div className={styles.dateInputs}>
                            <div className={styles.dateGroup}>
                                <label className={styles.dateLabel}>From:</label>
                                <Input
                                    type="date"
                                    value={dateFrom}
                                    onChange={setDateFrom}
                                />
                            </div>
                            <div className={styles.dateGroup}>
                                <label className={styles.dateLabel}>To:</label>
                                <Input
                                    type="date"
                                    value={dateTo}
                                    onChange={setDateTo}
                                />
                            </div>
                            <Button
                                variant="secondary"
                                onClick={handleDateFilter}
                                disabled={!dateFrom || !dateTo}
                                size="small"
                            >
                                Apply Date Filter
                            </Button>
                        </div>
                    </div>
                )}

                {hasFilters && (
                    <Button
                        variant="cancel"
                        onClick={clearAll}
                        size="small"
                    >
                        <X size={16} /> Clear All Filters
                    </Button>
                )}
            </div>
        </div>
    );
};