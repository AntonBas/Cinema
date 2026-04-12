import React from 'react';
import { Select } from '@/components/ui/Select/Select';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import type { SelectOption } from '@/components/ui/Select/Select';
import styles from './PromotionFilters.module.css';

interface PromotionFiltersProps {
    selectedStatus?: string;
    onStatusChange: (status: string) => void;
    onSearch?: (query: string) => void;
}

export const PromotionFilters: React.FC<PromotionFiltersProps> = ({
    selectedStatus,
    onStatusChange,
    onSearch
}) => {
    const statusOptions: SelectOption[] = [
        { value: '', label: 'All Statuses' },
        { value: 'active', label: 'Active' },
        { value: 'upcoming', label: 'Upcoming' },
        { value: 'expired', label: 'Expired' }
    ];

    return (
        <div className={styles.container}>
            {onSearch && (
                <SearchInput
                    onSearch={onSearch}
                    placeholder="Search promotions..."
                    delay={300}
                    className={styles.searchInput}
                />
            )}
            <Select
                options={statusOptions}
                value={selectedStatus || ''}
                onChange={(value) => onStatusChange(value.toString())}
                placeholder="Filter by status"
                className={styles.statusSelect}
            />
        </div>
    );
};