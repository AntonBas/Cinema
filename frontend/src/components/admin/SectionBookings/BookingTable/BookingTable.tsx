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
} from "../bookingBadges";
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
          <thead>
            <tr>
              <th>Booking</th>
              <th>Created</th>
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
                  <button
                    type="button"
                    className={styles.linkButton}
                    onClick={() => onSelect(booking.id)}
                  >
                    {booking.bookingNumber}
                  </button>
                </td>
                <td data-label="Created">
                  {formatDateTime(booking.createdDate)}
                </td>
                <td data-label="Customer" className={styles.customer}>
                  {booking.userEmail}
                </td>
                <td data-label="Session">
                  <span className={styles.primary}>{booking.movieTitle}</span>
                  <span className={styles.secondary}>
                    {formatDateTime(booking.sessionTime)} · {booking.hallName}
                  </span>
                </td>
                <td data-label="Amount">{formatPrice(booking.finalPrice)}</td>
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
