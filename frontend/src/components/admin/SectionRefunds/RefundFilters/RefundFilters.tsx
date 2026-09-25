import React from "react";
import { Select } from "@/components/ui/Select/Select";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import type { SelectOption } from "@/components/ui/Select/Select";
import type { AdminRefundFilters, RefundStatus } from "@/types/refund";
import { RefundStatusDisplay } from "@/types/refund";
import styles from "@/components/admin/SectionBookings/BookingFilters/BookingFilters.module.css";

interface RefundFiltersProps {
  filters: AdminRefundFilters;
  sort?: string;
  onChange: (changes: Partial<AdminRefundFilters>) => void;
  onSortChange: (sort: string) => void;
  onClear: () => void;
}

const NEEDS_ATTENTION = "NEEDS_ATTENTION";

const STATUS_OPTIONS: SelectOption[] = [
  { value: "", label: "All statuses" },
  { value: NEEDS_ATTENTION, label: "Needs attention" },
  ...Object.entries(RefundStatusDisplay).map(([value, label]) => ({
    value,
    label,
  })),
];

const SORT_OPTIONS: SelectOption[] = [
  { value: "", label: "Newest first" },
  { value: "createdDate,asc", label: "Oldest first" },
  { value: "totalAmount,desc", label: "Amount: high to low" },
  { value: "totalAmount,asc", label: "Amount: low to high" },
];

const toStatusValue = (filters: AdminRefundFilters): string =>
  filters.needsAttention ? NEEDS_ATTENTION : filters.status || "";

export const RefundFilters: React.FC<RefundFiltersProps> = ({
  filters,
  sort,
  onChange,
  onSortChange,
  onClear,
}) => {
  const hasActiveFilters = Object.values(filters).some(Boolean) || !!sort;

  const handleStatusChange = (value: string) => {
    if (value === NEEDS_ATTENTION) {
      onChange({ status: undefined, needsAttention: true });
      return;
    }
    onChange({
      status: (value as RefundStatus) || undefined,
      needsAttention: undefined,
    });
  };

  return (
    <div className={styles.filters}>
      <div className={styles.search}>
        <SearchInput
          value={filters.query}
          onSearch={(query) => onChange({ query: query || undefined })}
          placeholder="Booking number, email, ticket code or LiqPay order..."
          delay={500}
        />
      </div>

      <div className={styles.grid}>
        <div className={styles.filterItem}>
          <label htmlFor="refund-filters-status" className={styles.label}>
            Status
          </label>
          <Select
            id="refund-filters-status"
            value={toStatusValue(filters)}
            onChange={(value) => handleStatusChange(String(value))}
            options={STATUS_OPTIONS}
          />
        </div>

        <div className={styles.filterItem}>
          <label htmlFor="refundDateFrom" className={styles.label}>
            Created from
          </label>
          <Input
            id="refundDateFrom"
            type="date"
            value={filters.dateFrom || ""}
            onChange={(value) => onChange({ dateFrom: value || undefined })}
          />
        </div>

        <div className={styles.filterItem}>
          <label htmlFor="refundDateTo" className={styles.label}>
            Created to
          </label>
          <Input
            id="refundDateTo"
            type="date"
            value={filters.dateTo || ""}
            onChange={(value) => onChange({ dateTo: value || undefined })}
            min={filters.dateFrom}
          />
        </div>

        <div className={styles.filterItem}>
          <label htmlFor="refund-filters-sort-by" className={styles.label}>
            Sort by
          </label>
          <Select
            id="refund-filters-sort-by"
            value={sort || ""}
            onChange={(value) => onSortChange(String(value))}
            options={SORT_OPTIONS}
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
    </div>
  );
};
