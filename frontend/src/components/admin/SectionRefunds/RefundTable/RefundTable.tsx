import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import { Button } from "@/components/ui/Button/Button";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import styles from "@/components/admin/SectionBookings/BookingTable/BookingTable.module.css";
import refundStyles from "./RefundTable.module.css";
import { REFUND_STATUS_VARIANT } from "@/components/admin/shared/statusBadges";
import { formatDateTime, formatPrice } from "@/utils/formatters";
import type { AdminRefundListResponse } from "@/types/refund";
import { RefundReasonDisplay, RefundStatusDisplay } from "@/types/refund";
import { useAuth } from "@/context/AuthContext";

interface RefundTableProps {
  refunds: AdminRefundListResponse[];
  onOpenBooking: (bookingId: number) => void;
  onOpenHistory: (refundId: number) => void;
}

export const RefundTable: React.FC<RefundTableProps> = ({
  refunds,
  onOpenBooking,
  onOpenHistory,
}) => {
  const { isAdmin } = useAuth();

  if (refunds.length === 0) {
    return (
      <EmptyState
        title="No Refunds Found"
        message="Try changing the filters or search query."
      />
    );
  }

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            <col className={refundStyles.colCreated} />
            <col className={refundStyles.colCustomer} />
            <col className={refundStyles.colBooking} />
            <col className={refundStyles.colAmount} />
            <col className={refundStyles.colStatus} />
            {isAdmin && <col className={refundStyles.colHistory} />}
          </colgroup>
          <thead>
            <tr>
              <th>Created</th>
              <th>Customer</th>
              <th>Booking</th>
              <th>Amount</th>
              <th>Status</th>
              {isAdmin && <th>History</th>}
            </tr>
          </thead>
          <tbody>
            {refunds.map((refund) => {
              const createdAt = formatDateTime(refund.createdDate);
              const updatedAt = formatDateTime(refund.lastModifiedDate);

              return (
                <tr key={refund.id}>
                  <td data-label="Created">
                    <div className={styles.stack}>
                      <span className={`${styles.primary} ${styles.nowrap}`}>
                        {createdAt}
                      </span>
                      {updatedAt !== createdAt && (
                        <span
                          className={`${styles.secondary} ${styles.nowrap}`}
                        >
                          Updated {updatedAt}
                        </span>
                      )}
                    </div>
                  </td>
                  <td data-label="Customer">
                    <span className={styles.truncate} title={refund.userEmail}>
                      {refund.userEmail}
                    </span>
                  </td>
                  <td data-label="Booking">
                    <div className={styles.stack}>
                      <button
                        type="button"
                        className={styles.linkButton}
                        onClick={() => onOpenBooking(refund.bookingId)}
                      >
                        {refund.bookingNumber}
                      </button>
                      {refund.ticketCode && (
                        <span
                          className={`${styles.secondary} ${styles.mono} ${styles.truncate}`}
                          title={refund.ticketCode}
                        >
                          {refund.ticketCode}
                        </span>
                      )}
                      <span className={styles.secondary}>
                        {refund.movieTitle} ·{" "}
                        {formatDateTime(refund.sessionTime)}
                      </span>
                    </div>
                  </td>
                  <td data-label="Amount">
                    <div className={styles.stack}>
                      <span className={styles.primary}>
                        {formatPrice(refund.totalAmount)}
                      </span>
                      {refund.totalBonusPointsToDeduct > 0 && (
                        <span className={styles.secondary}>
                          −{refund.totalBonusPointsToDeduct} pts
                        </span>
                      )}
                      {refund.liqpayOrderId && (
                        <span
                          className={`${styles.secondary} ${styles.mono} ${styles.truncate}`}
                          title={`LiqPay order ID: ${refund.liqpayOrderId}`}
                        >
                          {refund.liqpayOrderId}
                        </span>
                      )}
                    </div>
                  </td>
                  <td data-label="Status">
                    <div className={styles.stack}>
                      <Badge variant={REFUND_STATUS_VARIANT[refund.status]}>
                        {RefundStatusDisplay[refund.status]}
                      </Badge>
                      {refund.reason && (
                        <span className={styles.secondary}>
                          {RefundReasonDisplay[refund.reason] ?? refund.reason}
                        </span>
                      )}
                    </div>
                  </td>
                  {isAdmin && (
                    <td data-label="History">
                      <Button
                        variant="secondary"
                        size="small"
                        onClick={() => onOpenHistory(refund.id)}
                      >
                        View
                      </Button>
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
