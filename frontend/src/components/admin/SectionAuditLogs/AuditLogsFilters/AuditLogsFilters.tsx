import React from 'react';
import { Select, Input, Button } from '@/components/ui';
import type { SelectOption } from '@/components/ui/Select/Select';
import styles from './AuditLogsFilters.module.css';

interface AuditLogsFiltersProps {
    entityType: string;
    action: string;
    changedBy: string;
    onEntityTypeChange: (value: string) => void;
    onActionChange: (value: string) => void;
    onChangedByChange: (value: string) => void;
    onClear: () => void;
    entityTypes: string[];
    actions: string[];
}

export const AuditLogsFilters: React.FC<AuditLogsFiltersProps> = ({
    entityType,
    action,
    changedBy,
    onEntityTypeChange,
    onActionChange,
    onChangedByChange,
    onClear,
    entityTypes,
    actions
}) => {
    const hasActiveFilters = entityType !== '' || action !== '' || changedBy !== '';

    const entityTypeOptions: SelectOption[] = [
        { value: '', label: 'All Types' },
        ...entityTypes.map(type => ({ value: type, label: type }))
    ];

    const actionOptions: SelectOption[] = [
        { value: '', label: 'All Actions' },
        ...actions.map(act => ({ value: act, label: act }))
    ];

    return (
        <div className={styles.filtersContainer}>
            <div className={styles.filterItem}>
                <label className={styles.label}>Type</label>
                <Select
                    options={entityTypeOptions}
                    value={entityType}
                    onChange={(value) => onEntityTypeChange(value.toString())}
                    placeholder="Select type"
                />
            </div>

            <div className={styles.filterItem}>
                <label className={styles.label}>Action</label>
                <Select
                    options={actionOptions}
                    value={action}
                    onChange={(value) => onActionChange(value.toString())}
                    placeholder="Select action"
                />
            </div>

            <div className={styles.filterItem}>
                <label className={styles.label}>Changed By</label>
                <Input
                    value={changedBy}
                    onChange={onChangedByChange}
                    placeholder="User email"
                />
            </div>

            {hasActiveFilters && (
                <Button variant="secondary" onClick={onClear} className={styles.clearButton}>
                    Clear
                </Button>
            )}
        </div>
    );
};