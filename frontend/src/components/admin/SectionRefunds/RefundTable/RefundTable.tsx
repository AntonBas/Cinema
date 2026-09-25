import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import { Button } from "@/components/ui/Button/Button";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import styles from "@/components/admin/SectionBookings/BookingTable/BookingTable.module.css";
import { REFUND_STATUS_VARIANT } from "@/components/admin/shared/statusBadges";
import { formatDateTime, formatPrice } from "@/utils/formatters";
import type { AdminRefundListResponse } from "@/types/refund";
import { RefundStatusDisplay } from "@/types/refund";
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
          <thead>
            <tr>
              <th>Created</th>
              <th>Customer</th>
              <th>Booking</th>
              <th>Ticket</th>
              <th>Amount</th>
              <th>Status</th>
              {isAdmin && <th>History</th>}
            </tr>
          </thead>
          <tbody>
            {refunds.map((refund) => (
              <tr key={refund.id}>
                <td data-label="Created">
                  <span className={styles.primary}>
                    {formatDateTime(refund.createdDate)}
                  </span>
                  <span className={styles.secondary}>
                    Updated {formatDateTime(refund.lastModifiedDate)}
                  </span>
                </td>
                <td data-label="Customer" className={styles.customer}>
                  {refund.userEmail}
                </td>
                <td data-label="Booking">
                  <button
                    type="button"
                    className={styles.linkButton}
                    onClick={() => onOpenBooking(refund.bookingId)}
                  >
                    {refund.bookingNumber}
                  </button>
                  <span className={styles.secondary}>
                    {refund.movieTitle} · {formatDateTime(refund.sessionTime)}
                  </span>
                </td>
                <td data-label="Ticket">{refund.ticketCode || "—"}</td>
                <td data-label="Amount">
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
                      className={`${styles.secondary} ${styles.mono}`}
                      title="LiqPay order ID"
                    >
                      {refund.liqpayOrderId}
                    </span>
                  )}
                </td>
                <td data-label="Status">
                  <Badge variant={REFUND_STATUS_VARIANT[refund.status]}>
                    {RefundStatusDisplay[refund.status]}
                  </Badge>
                  {refund.reason && (
                    <span className={styles.secondary}>{refund.reason}</span>
                  )}
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
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
