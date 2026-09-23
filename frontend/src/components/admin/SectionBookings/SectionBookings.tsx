import React, { useCallback, useEffect, useState } from "react";
import { useAdminBookings } from "@/hooks/features/booking/useAdminBookings";
import { BookingFilters } from "./BookingFilters/BookingFilters";
import { BookingTable } from "./BookingTable/BookingTable";
import { BookingDetailsModal } from "./BookingDetailsModal/BookingDetailsModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionBookings.module.css";

export const SectionBookings: React.FC = () => {
  const {
    bookings,
    pagination,
    loading,
    filters,
    sort,
    setPage,
    setSort,
    applyFilters,
    clearFilters,
    refresh,
  } = useAdminBookings();
  const [selectedBookingId, setSelectedBookingId] = useState<number | null>(
    null,
  );

  useEffect(() => {
    refresh();
  }, [refresh]);

  const handleClear = useCallback(() => {
    clearFilters();
    setSort("");
  }, [clearFilters, setSort]);

  if (loading && !bookings.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading bookings..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Bookings"
        subtitle="Look up bookings, payments and issued tickets"
      />

      <BookingFilters
        filters={filters}
        sort={sort}
        onChange={applyFilters}
        onSortChange={setSort}
        onClear={handleClear}
      />

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} bookings
        </div>
      )}

      <BookingTable bookings={bookings} onSelect={setSelectedBookingId} />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={pagination.number}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={setPage}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

      {selectedBookingId !== null && (
        <BookingDetailsModal
          bookingId={selectedBookingId}
          onClose={() => setSelectedBookingId(null)}
        />
      )}
    </div>
  );
};
