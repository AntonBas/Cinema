import React from 'react';
import { Select } from '@/components/ui/Select/Select';
import type { SelectOption } from '@/components/ui/Select/Select';
import styles from './PromotionFilters.module.css';

interface PromotionFiltersProps {
    selectedStatus?: string;
    onStatusChange: (status: string) => void;
}

const PromotionFilters: React.FC<PromotionFiltersProps> = ({
    selectedStatus,
    onStatusChange
}) => {
    const statusOptions: SelectOption[] = [
        { value: '', label: 'All Statuses' },
        { value: 'active', label: 'Active' },
        { value: 'upcoming', label: 'Upcoming' },
        { value: 'expired', label: 'Expired' }
    ];

    const handleChange = (value: string | number) => {
        onStatusChange(value.toString());
    };

    return (
        <div className={styles.container}>
            <Select
                options={statusOptions}
                value={selectedStatus || ''}
                onChange={handleChange}
                placeholder="Filter by status"
            />
        </div>
    );
};

export default PromotionFilters;