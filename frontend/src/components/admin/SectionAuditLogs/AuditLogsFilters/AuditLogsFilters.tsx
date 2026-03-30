import React from 'react';
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
    return (
        <div className={styles.filtersContainer}>
            <div className={styles.searchWrapper}>
                <label>Entity Type</label>
                <select value={entityType} onChange={(e) => onEntityTypeChange(e.target.value)}>
                    <option value="">All Entity Types</option>
                    {entityTypes.map(type => (
                        <option key={type} value={type}>{type}</option>
                    ))}
                </select>
            </div>

            <div className={styles.searchWrapper}>
                <label>Action</label>
                <select value={action} onChange={(e) => onActionChange(e.target.value)}>
                    <option value="">All Actions</option>
                    {actions.map(act => (
                        <option key={act} value={act}>{act}</option>
                    ))}
                </select>
            </div>

            <div className={styles.searchWrapper}>
                <label>Changed By</label>
                <input
                    type="text"
                    placeholder="User email"
                    value={changedBy}
                    onChange={(e) => onChangedByChange(e.target.value)}
                />
            </div>

            <button className={styles.clearButton} onClick={onClear}>
                Clear
            </button>
        </div>
    );
};