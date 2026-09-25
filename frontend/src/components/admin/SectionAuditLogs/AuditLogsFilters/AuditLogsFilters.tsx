import React from "react";
import { Select } from "@/components/ui/Select/Select";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Button } from "@/components/ui/Button/Button";
import type { SelectOption } from "@/components/ui/Select/Select";
import { getActionDisplay, getEntityTypeDisplay } from "@/types/audit";
import styles from "./AuditLogsFilters.module.css";

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
  actions,
}) => {
  const hasActiveFilters =
    entityType !== "" || action !== "" || changedBy !== "";

  const entityTypeOptions: SelectOption[] = [
    { value: "", label: "All Types" },
    ...entityTypes.map((type) => ({
      value: type,
      label: getEntityTypeDisplay(type),
    })),
  ];

  const actionOptions: SelectOption[] = [
    { value: "", label: "All Actions" },
    ...actions.map((act) => ({ value: act, label: getActionDisplay(act) })),
  ];

  return (
    <div className={styles.filtersContainer}>
      <div className={styles.filterItem}>
        <label htmlFor="audit-logs-filters-type" className={styles.label}>
          Type
        </label>
        <Select
          id="audit-logs-filters-type"
          options={entityTypeOptions}
          value={entityType}
          onChange={(value) => onEntityTypeChange(value.toString())}
          placeholder="Select type"
        />
      </div>

      <div className={styles.filterItem}>
        <label htmlFor="audit-logs-filters-action" className={styles.label}>
          Action
        </label>
        <Select
          id="audit-logs-filters-action"
          options={actionOptions}
          value={action}
          onChange={(value) => onActionChange(value.toString())}
          placeholder="Select action"
        />
      </div>

      <div className={styles.filterItem}>
        <label htmlFor="audit-logs-filters-changed-by" className={styles.label}>
          Changed By
        </label>
        <SearchInput
          id="audit-logs-filters-changed-by"
          value={changedBy}
          onSearch={onChangedByChange}
          placeholder="User email"
        />
      </div>

      {hasActiveFilters && (
        <Button
          variant="secondary"
          onClick={onClear}
          className={styles.clearButton}
        >
          Clear
        </Button>
      )}
    </div>
  );
};
