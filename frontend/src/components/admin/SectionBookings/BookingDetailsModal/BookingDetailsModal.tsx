import React, { useEffect, useState } from "react";
import { Modal } from "@/components/ui/Modal/Modal";
import { Badge } from "@/components/ui/Badge/Badge";
import { Button } from "@/components/ui/Button/Button";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { useAdminBookingDetails } from "@/hooks/features/booking/useAdminBookingDetails";
import { EntityHistoryModal } from "@/components/admin/SectionAuditLogs/EntityHistoryModal/EntityHistoryModal";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import { formatDateTime, formatPrice } from "@/utils/formatters";
import { BookingStatusDisplay } from "@/types/booking";
import { PaymentStatusDisplay } from "@/types/payment";
import { TicketStatusDisplay } from "@/types/ticket";
import {
  BOOKING_STATUS_VARIANT,
  PAYMENT_STATUS_VARIANT,
  TICKET_STATUS_VARIANT,
} from "../bookingBadges";
import styles from "./BookingDetailsModal.module.css";

interface BookingDetailsModalProps {
  bookingId: number;
  onClose: () => void;
}

interface HistoryTarget {
  entityType: string;
  entityId: number;
}

export const BookingDetailsModal: React.FC<BookingDetailsModalProps> = ({
  bookingId,
  onClose,
}) => {
  const { booking, loading, error, getBooking } = useAdminBookingDetails();
  const showLoading = useDelayedLoading(loading);
  const [historyTarget, setHistoryTarget] = useState<HistoryTarget | null>(
    null,
  );

  useEffect(() => {
    getBooking(bookingId);
  }, [bookingId, getBooking]);

  const renderBody = () => {
    if (showLoading) {
      return <LoadingSpinner text="Loading booking..." />;
    }
    if (loading) {
      return null;
    }
    if (error || !booking) {
      return (
        <EmptyState
          title="Booking Unavailable"
          message="Could not load the booking."
        />
      );
    }

    const { payment } = booking;

    return (
      <>
        <div className={styles.header}>
          <Badge variant={BOOKING_STATUS_VARIANT[booking.status]}>
            {BookingStatusDisplay[booking.status]}
          </Badge>
          <Button
            variant="secondary"
            size="small"
            onClick={() =>
              setHistoryTarget({ entityType: "Booking", entityId: booking.id })
            }
          >
            Booking history
          </Button>
        </div>

        <section className={styles.grid}>
          <div className={styles.card}>
            <h4 className={styles.cardTitle}>Customer</h4>
            <p className={styles.value}>
              {booking.userFirstName} {booking.userLastName}
            </p>
            <p className={styles.muted}>{booking.userEmail}</p>
          </div>

          <div className={styles.card}>
            <h4 className={styles.cardTitle}>Session</h4>
            <p className={styles.value}>{booking.movieTitle}</p>
            <p className={styles.muted}>
              {formatDateTime(booking.sessionTime)} · {booking.hallName}
            </p>
          </div>

          <div className={styles.card}>
            <h4 className={styles.cardTitle}>Amount</h4>
            <p className={styles.value}>{formatPrice(booking.finalPrice)}</p>
            <p className={styles.muted}>
              Total {formatPrice(booking.totalPrice)}
              {booking.bonusPointsUsed > 0 &&
                ` · −${formatPrice(booking.bonusDiscountAmount)} (${booking.bonusPointsUsed} pts)`}
            </p>
          </div>

          <div className={styles.card}>
            <h4 className={styles.cardTitle}>Timeline</h4>
            <p className={styles.muted}>
              Created {formatDateTime(booking.createdDate)}
            </p>
            <p className={styles.muted}>
              Expires {formatDateTime(booking.expiresAt)}
            </p>
          </div>
        </section>

        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <h4 className={styles.sectionTitle}>Payment</h4>
            {payment && (
              <Button
                variant="secondary"
                size="small"
                onClick={() =>
                  setHistoryTarget({
                    entityType: "Payment",
                    entityId: payment.id,
                  })
                }
              >
                Payment history
              </Button>
            )}
          </div>
          {payment ? (
            <dl className={styles.details}>
              <dt>Status</dt>
              <dd>
                <Badge variant={PAYMENT_STATUS_VARIANT[payment.status]}>
                  {PaymentStatusDisplay[payment.status]}
                </Badge>
              </dd>
              <dt>Amount</dt>
              <dd>{formatPrice(payment.amount)}</dd>
              <dt>Paid at</dt>
              <dd>
                {payment.paymentTime ? formatDateTime(payment.paymentTime) : "—"}
              </dd>
              <dt>LiqPay order</dt>
              <dd className={styles.mono}>{payment.liqpayOrderId || "—"}</dd>
              <dt>Card</dt>
              <dd className={styles.mono}>{payment.cardMask || "—"}</dd>
              {(payment.errorCode || payment.errorDescription) && (
                <>
                  <dt>Error</dt>
                  <dd className={styles.error}>
                    {[payment.errorCode, payment.errorDescription]
                      .filter(Boolean)
                      .join(" — ")}
                  </dd>
                </>
              )}
            </dl>
          ) : (
            <p className={styles.muted}>No payment was started.</p>
          )}
        </section>

        <section className={styles.section}>
          <h4 className={styles.sectionTitle}>
            {booking.tickets.length > 0 ? "Tickets" : "Seats"}
          </h4>
          <div className={tableStyles.container}>
            <table className={tableStyles.table}>
              <thead>
                <tr>
                  <th>Seat</th>
                  <th>Type</th>
                  <th>Price</th>
                  {booking.tickets.length > 0 && (
                    <>
                      <th>Ticket</th>
                      <th>Status</th>
                    </>
                  )}
                </tr>
              </thead>
              <tbody>
                {booking.tickets.length > 0
                  ? booking.tickets.map((ticket) => (
                      <tr key={ticket.id}>
                        <td data-label="Seat">
                          Row {ticket.row}, seat {ticket.seatNumber}
                        </td>
                        <td data-label="Type">{ticket.ticketType}</td>
                        <td data-label="Price">{formatPrice(ticket.price)}</td>
                        <td data-label="Ticket" className={styles.mono}>
                          {ticket.ticketCode}
                        </td>
                        <td data-label="Status">
                          <Badge variant={TICKET_STATUS_VARIANT[ticket.status]}>
                            {TicketStatusDisplay[ticket.status]}
                          </Badge>
                        </td>
                      </tr>
                    ))
                  : booking.seatReservations.map((seat) => (
                      <tr key={seat.id}>
                        <td data-label="Seat">
                          Row {seat.row}, seat {seat.seatNumber}
                        </td>
                        <td data-label="Type">{seat.ticketTypeName}</td>
                        <td data-label="Price">
                          {formatPrice(seat.seatPrice)}
                        </td>
                      </tr>
                    ))}
              </tbody>
            </table>
          </div>
        </section>
      </>
    );
  };

  return (
    <>
      <Modal
        isOpen={true}
        onClose={onClose}
        title={booking?.bookingNumber ?? "Booking"}
        size="large"
      >
        <div className={styles.body}>{renderBody()}</div>
      </Modal>

      {historyTarget && (
        <EntityHistoryModal
          entityType={historyTarget.entityType}
          entityId={historyTarget.entityId}
          onClose={() => setHistoryTarget(null)}
        />
      )}
    </>
  );
};
