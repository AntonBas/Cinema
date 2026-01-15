import React from 'react';
import { SearchInput } from '@/components/ui/SearchInput';
import { Badge } from '@/components/ui/Badge';
import styles from './TicketTypeFilters.module.css';

interface TicketTypeFiltersProps {
    searchQuery: string;
    onSearchChange: (query: string) => void;
    statusFilter: 'all' | 'active' | 'inactive';
    onStatusChange: (filter: 'all' | 'active' | 'inactive') => void;
    activeCount?: number;
    inactiveCount?: number;
}

const TicketTypeFilters: React.FC<TicketTypeFiltersProps> = ({
    onSearchChange,
    statusFilter,
    onStatusChange,
    activeCount = 0,
    inactiveCount = 0
}) => {
    return (
        <div className={styles.filters}>
            <div className={styles.topRow}>
                <div className={styles.search}>
                    <SearchInput
                        onSearch={onSearchChange}
                        placeholder="Search by name, code or category..."
                        delay={300}
                    />
                </div>

                <div className={styles.filterButtons}>
                    <button
                        className={`${styles.filterButton} ${statusFilter === 'all' ? styles.active : ''}`}
                        onClick={() => onStatusChange('all')}
                    >
                        All
                    </button>
                    <button
                        className={`${styles.filterButton} ${statusFilter === 'active' ? styles.active : ''}`}
                        onClick={() => onStatusChange('active')}
                    >
                        Active
                    </button>
                    <button
                        className={`${styles.filterButton} ${statusFilter === 'inactive' ? styles.active : ''}`}
                        onClick={() => onStatusChange('inactive')}
                    >
                        Inactive
                    </button>
                </div>
            </div>

            <div className={styles.stats}>
                <span>Showing:</span>
                <Badge variant={statusFilter === 'all' ? 'primary' : 'outline'}>
                    All: {activeCount + inactiveCount}
                </Badge>
                <Badge variant={statusFilter === 'active' ? 'success' : 'outline'}>
                    Active: {activeCount}
                </Badge>
                <Badge variant={statusFilter === 'inactive' ? 'error' : 'outline'}>
                    Inactive: {inactiveCount}
                </Badge>
            </div>
        </div>
    );
};

export default TicketTypeFilters;