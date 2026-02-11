import React from 'react';
import { SearchInput, Select } from '@/components/ui';
import { UserRoleDisplay, VerificationStatusDisplay } from '@/types/user';
import styles from './UserFilters.module.css';

interface UserFiltersProps {
    onSearchChange: (value: string) => void;
    roleFilter: string;
    onRoleFilterChange: (value: string) => void;
    statusFilter: string;
    onStatusFilterChange: (value: string) => void;
}

export const UserFilters: React.FC<UserFiltersProps> = ({
    onSearchChange,
    roleFilter,
    onRoleFilterChange,
    statusFilter,
    onStatusFilterChange,
}) => {
    const roleOptions = [
        { value: '', label: 'All Roles' },
        ...Object.entries(UserRoleDisplay).map(([value, label]) => ({
            value,
            label,
        }))
    ];

    const statusOptions = [
        { value: '', label: 'All Statuses' },
        ...Object.entries(VerificationStatusDisplay).map(([value, label]) => ({
            value,
            label,
        }))
    ];

    const handleSearch = (query: string) => {
        onSearchChange(query);
    };

    const handleRoleChange = (value: string | number) => {
        onRoleFilterChange(String(value));
    };

    const handleStatusChange = (value: string | number) => {
        onStatusFilterChange(String(value));
    };

    return (
        <div className={styles.filters}>
            <div className={styles.search}>
                <SearchInput
                    onSearch={handleSearch}
                    placeholder="Search by email or name..."
                    delay={500}
                />
            </div>

            <div className={styles.selectors}>
                <Select
                    value={roleFilter}
                    onChange={handleRoleChange}
                    options={roleOptions}
                    placeholder="All Roles"
                    className={styles.select}
                />

                <Select
                    value={statusFilter}
                    onChange={handleStatusChange}
                    options={statusOptions}
                    placeholder="All Statuses"
                    className={styles.select}
                />
            </div>
        </div>
    );
};