import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useAdminRefunds } from "@/hooks/features/refund/useAdminRefunds";
import {
  parseOptionalEnumParam,
  toUrlEnumValue,
  useUrlParams,
} from "@/hooks/common/useUrlParams";
import { RefundFilters } from "./RefundFilters/RefundFilters";
import { RefundTable } from "./RefundTable/RefundTable";
import { BookingDetailsModal } from "@/components/admin/SectionBookings/BookingDetailsModal/BookingDetailsModal";
import { EntityHistoryModal } from "@/components/admin/SectionAuditLogs/EntityHistoryModal/EntityHistoryModal";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import {
  RefundStatusDisplay,
  type AdminRefundFilters,
  type RefundStatus,
} from "@/types/refund";
import styles from "@/components/admin/SectionBookings/SectionBookings.module.css";

const REFUND_STATUSES = Object.keys(RefundStatusDisplay) as RefundStatus[];
const ALL_STATUSES_PARAM = "all";

const toStatusParam = (filters: AdminRefundFilters): string | undefined => {
  if (filters.needsAttention) return undefined;
  return toUrlEnumValue(filters.status) ?? ALL_STATUSES_PARAM;
};

export const SectionRefunds: React.FC = () => {
  const { page, query, sort, getParam, setFilters, setPage, setSort } =
    useUrlParams();
  const statusParam = getParam("status");
  const dateFrom = getParam("from");
  const dateTo = getParam("to");

  const filters = useMemo<AdminRefundFilters>(() => {
    const status = parseOptionalEnumParam(statusParam, REFUND_STATUSES);
    return {
      query,
      status,
      needsAttention: !statusParam || undefined,
      dateFrom,
      dateTo,
    };
  }, [query, statusParam, dateFrom, dateTo]);

  const { refunds, pagination, loading, refresh } = useAdminRefunds({
    filters,
    page,
    sort,
  });

  const applyFilters = useCallback(
    (changes: Partial<AdminRefundFilters>) => {
      const next = { ...filters, ...changes };
      setFilters(
        {
          q: next.query,
          status: toStatusParam(next),
          from: next.dateFrom,
          to: next.dateTo,
        },
        { replace: "query" in changes },
      );
    },
    [filters, setFilters],
  );

  const handleClear = useCallback(() => {
    setFilters({ status: ALL_STATUSES_PARAM }, { reset: true });
  }, [setFilters]);

  const [bookingId, setBookingId] = useState<number | null>(null);
  const [historyRefundId, setHistoryRefundId] = useState<number | null>(null);

  useEffect(() => {
    refresh();
  }, [refresh]);

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
