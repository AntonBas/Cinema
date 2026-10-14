import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import { formatDateTime, formatPrice } from "@/utils/formatters";
import type { AdminBookingListResponse } from "@/types/booking";
import { BookingStatusDisplay } from "@/types/booking";
import { PaymentStatusDisplay } from "@/types/payment";
import {
  BOOKING_STATUS_VARIANT,
  PAYMENT_STATUS_VARIANT,
} from "@/components/admin/shared/statusBadges";
import styles from "./BookingTable.module.css";

interface BookingTableProps {
  bookings: AdminBookingListResponse[];
  onSelect: (bookingId: number) => void;
}

export const BookingTable: React.FC<BookingTableProps> = ({
  bookings,
  onSelect,
}) => {
  if (bookings.length === 0) {
    return (
      <EmptyState
        title="No Bookings Found"
        message="Try changing the filters or search query."
      />
    );
  }

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            <col className={styles.colBooking} />
            <col className={styles.colCustomer} />
            <col className={styles.colSession} />
            <col className={styles.colAmount} />
            <col className={styles.colStatus} />
            <col className={styles.colPayment} />
          </colgroup>
          <thead>
            <tr>
              <th>Booking</th>
              <th>Customer</th>
              <th>Session</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Payment</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <tr key={booking.id}>
                <td data-label="Booking">
                  <div className={styles.stack}>
                    <button
                      type="button"
                      className={styles.linkButton}
                      onClick={() => onSelect(booking.id)}
                    >
                      {booking.bookingNumber}
                    </button>
                    <span className={`${styles.secondary} ${styles.nowrap}`}>
                      {formatDateTime(booking.createdDate)}
                    </span>
                  </div>
                </td>
                <td data-label="Customer">
                  <span className={styles.truncate} title={booking.userEmail}>
                    {booking.userEmail}
                  </span>
                </td>
                <td data-label="Session">
                  <div className={styles.stack}>
                    <span className={styles.primary}>{booking.movieTitle}</span>
                    <span className={styles.secondary}>
                      {formatDateTime(booking.sessionTime)} ·{" "}
                      <span className={styles.nowrap}>{booking.hallName}</span>
                    </span>
                  </div>
                </td>
                <td data-label="Amount">
                  <span className={styles.nowrap}>
                    {formatPrice(booking.finalPrice)}
                  </span>
                </td>
                <td data-label="Status">
                  <Badge variant={BOOKING_STATUS_VARIANT[booking.status]}>
                    {BookingStatusDisplay[booking.status]}
                  </Badge>
                </td>
                <td data-label="Payment">
                  {booking.paymentStatus ? (
                    <Badge
                      variant={PAYMENT_STATUS_VARIANT[booking.paymentStatus]}
                    >
                      {PaymentStatusDisplay[booking.paymentStatus]}
                    </Badge>
                  ) : (
                    <span className={styles.secondary}>—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
