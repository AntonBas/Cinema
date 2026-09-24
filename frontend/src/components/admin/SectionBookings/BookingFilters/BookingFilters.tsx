import React from "react";
import { Select } from "@/components/ui/Select/Select";
import { Input } from "@/components/ui/Input/Input";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import type { SelectOption } from "@/components/ui/Select/Select";
import type { AdminBookingFilters, BookingStatus } from "@/types/booking";
import { BookingStatusDisplay } from "@/types/booking";
import type { PaymentStatus } from "@/types/payment";
import { PaymentStatusDisplay } from "@/types/payment";
import styles from "./BookingFilters.module.css";

interface BookingFiltersProps {
  filters: AdminBookingFilters;
  sort?: string;
  onChange: (changes: Partial<AdminBookingFilters>) => void;
  onSortChange: (sort: string) => void;
  onClear: () => void;
}

const BOOKING_STATUSES: BookingStatus[] = [
  "PENDING",
  "CONFIRMED",
  "CANCELLED",
  "EXPIRED",
];

const STATUS_OPTIONS: SelectOption[] = [
  { value: "", label: "All statuses" },
  ...BOOKING_STATUSES.map((status) => ({
    value: status,
    label: BookingStatusDisplay[status],
  })),
];

const PAYMENT_STATUS_OPTIONS: SelectOption[] = [
  { value: "", label: "All payments" },
  ...Object.entries(PaymentStatusDisplay).map(([value, label]) => ({
    value,
    label,
  })),
];

const SORT_OPTIONS: SelectOption[] = [
  { value: "", label: "Newest first" },
  { value: "createdDate,asc", label: "Oldest first" },
  { value: "sessionTime,desc", label: "Session: latest first" },
  { value: "sessionTime,asc", label: "Session: earliest first" },
  { value: "finalPrice,desc", label: "Price: high to low" },
  { value: "finalPrice,asc", label: "Price: low to high" },
];

export const BookingFilters: React.FC<BookingFiltersProps> = ({
  filters,
  sort,
  onChange,
  onSortChange,
  onClear,
}) => {
  const hasActiveFilters = Object.values(filters).some(Boolean) || !!sort;

  return (
    <div className={styles.filters}>
      <div className={styles.search}>
        <SearchInput
          value={filters.query}
          onSearch={(query) => onChange({ query: query || undefined })}
          placeholder="Booking number, email, movie or LiqPay order..."
          delay={500}
        />
      </div>

      <div className={styles.grid}>
        <div className={styles.filterItem}>
          <label className={styles.label}>Status</label>
          <Select
            value={filters.status || ""}
            onChange={(value) =>
              onChange({ status: (value as BookingStatus) || undefined })
            }
            options={STATUS_OPTIONS}
          />
        </div>

        <div className={styles.filterItem}>
          <label className={styles.label}>Payment</label>
          <Select
            value={filters.paymentStatus || ""}
            onChange={(value) =>
              onChange({
                paymentStatus: (value as PaymentStatus) || undefined,
              })
            }
            options={PAYMENT_STATUS_OPTIONS}
          />
        </div>

        <div className={styles.filterItem}>
          <label htmlFor="bookingDateFrom" className={styles.label}>
            Created from
          </label>
          <Input
            id="bookingDateFrom"
            type="date"
            value={filters.dateFrom || ""}
            onChange={(value) => onChange({ dateFrom: value || undefined })}
          />
        </div>

        <div className={styles.filterItem}>
          <label htmlFor="bookingDateTo" className={styles.label}>
            Created to
          </label>
          <Input
            id="bookingDateTo"
            type="date"
            value={filters.dateTo || ""}
            onChange={(value) => onChange({ dateTo: value || undefined })}
            min={filters.dateFrom}
          />
        </div>

        <div className={styles.filterItem}>
          <label className={styles.label}>Sort by</label>
          <Select
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
