import React from 'react';
import { SearchInput } from '@/components/ui/SearchInput';
import { Select } from '@/components/ui/Select';
import type { TicketTypeCategory } from '@/types/ticketType';
import { TicketTypeCategoryDisplay } from '@/types/ticketType';
import styles from './TicketTypeFilters.module.css';

interface TicketTypeFiltersProps {
    searchQuery: string;
    onSearchChange: (query: string) => void;
    statusFilter: 'all' | 'active' | 'inactive';
    onStatusChange: (filter: 'all' | 'active' | 'inactive') => void;
    categoryFilter: TicketTypeCategory | 'all';
    onCategoryChange: (category: TicketTypeCategory | 'all') => void;
    activeCount?: number;
    inactiveCount?: number;
}

const TicketTypeFilters: React.FC<TicketTypeFiltersProps> = ({
    onSearchChange,
    statusFilter,
    onStatusChange,
    categoryFilter,
    onCategoryChange
}) => {
    const statusOptions = [
        { value: 'all', label: 'All Statuses' },
        { value: 'active', label: 'Active' },
        { value: 'inactive', label: 'Inactive' }
    ];

    const categoryOptions = [
        { value: 'all', label: 'All Categories' },
        ...Object.entries(TicketTypeCategoryDisplay).map(([value, label]) => ({
            value: value as string,
            label
        }))
    ];

    const handleStatusChange = (value: string | number) => {
        onStatusChange(value as 'all' | 'active' | 'inactive');
    };

    const handleCategoryChange = (value: string | number) => {
        onCategoryChange(value === 'all' ? 'all' : (value as TicketTypeCategory));
    };

    return (
        <div className={styles.filters}>
            <div className={styles.search}>
                <SearchInput
                    onSearch={onSearchChange}
                    placeholder="Search ticket types..."
                    delay={300}
                    className={styles.searchInput}
                />
            </div>

            <div className={styles.selectFilters}>
                <div className={styles.filterGroup}>
                    <Select
                        options={statusOptions}
                        value={statusFilter}
                        onChange={handleStatusChange}
                        placeholder="Status"
                        disabled={false}
                        className={styles.select}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <Select
                        options={categoryOptions}
                        value={categoryFilter}
                        onChange={handleCategoryChange}
                        placeholder="Category"
                        disabled={false}
                        className={styles.select}
                    />
                </div>
            </div>
        </div>
    );
};

export default TicketTypeFilters;