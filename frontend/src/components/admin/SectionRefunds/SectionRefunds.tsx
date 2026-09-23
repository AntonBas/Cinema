import React, { useCallback, useEffect, useState } from "react";
import { useAdminRefunds } from "@/hooks/features/refund/useAdminRefunds";
import { RefundFilters } from "./RefundFilters/RefundFilters";
import { RefundTable } from "./RefundTable/RefundTable";
import { BookingDetailsModal } from "@/components/admin/SectionBookings/BookingDetailsModal/BookingDetailsModal";
import { EntityHistoryModal } from "@/components/admin/SectionAuditLogs/EntityHistoryModal/EntityHistoryModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "@/components/admin/SectionBookings/SectionBookings.module.css";

export const SectionRefunds: React.FC = () => {
  const {
    refunds,
    pagination,
    loading,
    filters,
    sort,
    setPage,
    setSort,
    applyFilters,
    clearFilters,
    refresh,
  } = useAdminRefunds({ needsAttention: true });
  const [bookingId, setBookingId] = useState<number | null>(null);
  const [historyRefundId, setHistoryRefundId] = useState<number | null>(null);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const handleClear = useCallback(() => {
    clearFilters();
    setSort("");
  }, [clearFilters, setSort]);

  if (loading && !refunds.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading refunds..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Refunds"
        subtitle="Monitor refunds and spot ones stuck in processing or rejected by the gateway"
      />

      <RefundFilters
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
          of {pagination.totalElements} refunds
        </div>
      )}

      <RefundTable
        refunds={refunds}
        onOpenBooking={setBookingId}
        onOpenHistory={setHistoryRefundId}
      />

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

      {bookingId !== null && (
        <BookingDetailsModal
          bookingId={bookingId}
          onClose={() => setBookingId(null)}
        />
      )}

      {historyRefundId !== null && (
        <EntityHistoryModal
          entityType="Refund"
          entityId={historyRefundId}
          onClose={() => setHistoryRefundId(null)}
        />
      )}
    </div>
  );
};
