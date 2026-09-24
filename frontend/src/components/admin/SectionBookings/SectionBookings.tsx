import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useAdminBookings } from "@/hooks/features/booking/useAdminBookings";
import {
  parseOptionalEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { BookingFilters } from "./BookingFilters/BookingFilters";
import { BookingTable } from "./BookingTable/BookingTable";
import { BookingDetailsModal } from "./BookingDetailsModal/BookingDetailsModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import {
  BookingStatusDisplay,
  type AdminBookingFilters,
  type BookingStatus,
} from "@/types/booking";
import { PaymentStatusDisplay, type PaymentStatus } from "@/types/payment";
import styles from "./SectionBookings.module.css";

const BOOKING_STATUSES = Object.keys(BookingStatusDisplay) as BookingStatus[];
const PAYMENT_STATUSES = Object.keys(PaymentStatusDisplay) as PaymentStatus[];

export const SectionBookings: React.FC = () => {
  const {
    page,
    query,
    sort,
    getParam,
    setFilters,
    clearParams,
    setPage,
    setSort,
  } = useUrlParams();
  const statusParam = getParam("status");
  const paymentParam = getParam("payment");
  const dateFrom = getParam("from");
  const dateTo = getParam("to");

  const filters = useMemo<AdminBookingFilters>(
    () => ({
      query,
      status: parseOptionalEnumParam(statusParam, BOOKING_STATUSES),
      paymentStatus: parseOptionalEnumParam(paymentParam, PAYMENT_STATUSES),
      dateFrom,
      dateTo,
    }),
    [query, statusParam, paymentParam, dateFrom, dateTo],
  );

  const { bookings, pagination, loading, refresh } = useAdminBookings({
    filters,
    page,
    sort,
  });

  const applyFilters = useCallback(
    (changes: Partial<AdminBookingFilters>) => {
      const next = { ...filters, ...changes };
      setFilters(
        {
          q: next.query,
          status: toUrlEnumValue(next.status),
          payment: toUrlEnumValue(next.paymentStatus),
          from: next.dateFrom,
          to: next.dateTo,
        },
        { replace: "query" in changes },
      );
    },
    [filters, setFilters],
  );
  const [selectedBookingId, setSelectedBookingId] = useState<number | null>(
    null,
  );

  useEffect(() => {
    refresh();
  }, [refresh]);

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
        onClear={clearParams}
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
