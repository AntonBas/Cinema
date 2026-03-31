import React from 'react';
import { Select, Input, Button } from '@/components/ui';
import type { SelectOption } from '@/components/ui/Select/Select';
import { ActionDisplay } from '@/types/audit';
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
        ...actions.map(act => ({
            value: act,
            label: ActionDisplay[act] || act
        }))
    ];

    return (
        <div className={styles.filtersContainer}>
            <div className={styles.searchWrapper}>
                <Select
                    options={entityTypeOptions}
                    value={entityType}
                    onChange={(value) => onEntityTypeChange(value.toString())}
                    placeholder="Select type"
                    label="Type"
                />
            </div>

            <div className={styles.searchWrapper}>
                <Select
                    options={actionOptions}
                    value={action}
                    onChange={(value) => onActionChange(value.toString())}
                    placeholder="Select action"
                    label="Action"
                />
            </div>

            <div className={styles.searchWrapper}>
                <Input
                    value={changedBy}
                    onChange={onChangedByChange}
                    placeholder="User email"
                    label="Changed By"
                />
            </div>

            {hasActiveFilters && (
                <Button
                    variant="error"
                    onClick={onClear}
                    className={styles.clearButton}
                >
                    Clear
                </Button>
            )}
        </div>
    );
};