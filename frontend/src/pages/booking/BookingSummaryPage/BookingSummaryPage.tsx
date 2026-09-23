import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ProgressStepper } from "@/components/booking/ProgressStepper/ProgressStepper";
import { BOOKING_STEPS } from "@/components/booking/ProgressStepper/bookingSteps";
import { Layout } from "@/components/layout/Layout/Layout";
import { ConfirmModal } from "@/components/ui/ConfirmModal/ConfirmModal";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useBooking } from "@/hooks/features/booking/useBooking";
import { parseServerInstant } from "@/utils/dateUtils";
import { PageContainer } from "@/components/ui/PageContainer/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { formatFullDate, formatPrice, formatTime } from "@/utils/formatters";
import { Button } from "@/components/ui/Button/Button";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./BookingSummaryPage.module.css";

export const BookingSummaryPage: React.FC = () => {
  const { bookingId } = useParams<{ bookingId: string }>();
  const navigate = useNavigate();
  const [showCancelModal, setShowCancelModal] = useState(false);

  const { booking, loading, getById, cancel } = useBooking();

  useEffect(() => {
    if (bookingId) {
      getById(bookingId);
    }
  }, [bookingId, getById]);

  const handleCancelBooking = async () => {
    if (!bookingId || !booking) return;

    try {
      await cancel(bookingId);
    } catch {
      return;
    } finally {
      setShowCancelModal(false);
    }
    navigate(`/booking/${booking.sessionPublicId}`, { replace: true });
  };

  const handleProceedToPayment = () => {
    if (!booking) return;

    const bookingData = {
      id: booking.id,
      bookingNumber: booking.bookingNumber,
      movieTitle: booking.movieTitle,
      hallName: booking.hallName,
      sessionTime: booking.sessionTime,
      totalPrice: booking.totalPrice,
      finalPrice: booking.finalPrice,
      bonusPointsUsed: booking.bonusPointsUsed,
      bookedSeats: booking.seatReservations.map((seat) => ({
        seatNumber: String(seat.seatNumber),
        seatRow: seat.row,
        ticketType: seat.ticketTypeName,
        seatPrice: seat.seatPrice,
      })),
    };

    navigate(`/booking/payment/${booking.publicId}`, {
      state: { booking: bookingData },
    });
  };

  if (loading) {
    return (
      <Layout>
        <div className={styles.loading}>
          <LoadingSpinner text="Loading booking summary..." />
        </div>
      </Layout>
    );
  }

  if (!booking) {
    return (
      <Layout>
        <PageContainer size="narrow">
          <EmptyState variant="error" title="Booking Not Found" />
        </PageContainer>
      </Layout>
    );
  }

  const expiresAt = parseServerInstant(booking.expiresAt);
  const timeLeft = Math.max(
    0,
    Math.floor((expiresAt.getTime() - Date.now()) / (1000 * 60)),
  );
  const hoursLeft = Math.floor(timeLeft / 60);
  const minutesLeft = timeLeft % 60;

  return (
    <Layout>
      <ConfirmModal
        isOpen={showCancelModal}
        onConfirm={handleCancelBooking}
        onCancel={() => setShowCancelModal(false)}
        title="Cancel Booking"
        message="Are you sure you want to cancel this booking? This action cannot be undone."
        confirmText="Yes, Cancel Booking"
        cancelText="No, Keep Booking"
        variant="error"
      />

      <PageContainer size="narrow">
        <ProgressStepper
          steps={BOOKING_STEPS}
          currentStep={2}
          className={styles.stepper}
        />

        <PageHeader
          align="center"
          divider
          title="Booking Summary"
          subtitle={
            <span className={styles.bookingNumber}>
              Booking #: {booking.bookingNumber}
            </span>
          }
        />

        <div className={styles.content}>
          <div className={styles.movieInfo}>
            <h2>{booking.movieTitle}</h2>
            <div className={styles.sessionDetails}>
              <div className={styles.detailItem}>
                <span className={styles.detailLabel}>Hall:</span>
                <span className={styles.detailValue}>{booking.hallName}</span>
              </div>
              <div className={styles.detailItem}>
                <span className={styles.detailLabel}>Date:</span>
                <span className={styles.detailValue}>
                  {formatFullDate(booking.sessionTime)}
                </span>
              </div>
              <div className={styles.detailItem}>
                <span className={styles.detailLabel}>Time:</span>
                <span className={styles.detailValue}>
                  {formatTime(booking.sessionTime)}
                </span>
              </div>
            </div>
          </div>

          <div className={styles.seatsInfo}>
            <h3>Selected Seats</h3>
            <div className={styles.seatsList}>
              {booking.seatReservations.map((seat) => (
                <div key={seat.id} className={styles.seatItem}>
                  <div className={styles.seatInfo}>
                    <span className={styles.seatLocation}>
                      Row {seat.row}, Seat {seat.seatNumber}
                    </span>
                    <span className={styles.ticketType}>
                      {seat.ticketTypeName}
                    </span>
                  </div>
                  <span className={styles.seatPrice}>
                    {formatPrice(seat.seatPrice)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div className={styles.pricingInfo}>
            <h3>Payment Details</h3>
            <div className={styles.priceBreakdown}>
              {booking.bonusPointsUsed > 0 && (
                <>
                  <div className={styles.priceRow}>
                    <span>Total Price:</span>
                    <span>{formatPrice(booking.totalPrice)}</span>
                  </div>
                  <div className={styles.priceRow}>
                    <span>Bonus discount:</span>
                    <span className={styles.discount}>
                      -{formatPrice(booking.bonusDiscountAmount)}
                    </span>
                  </div>
                </>
              )}
              <div className={styles.finalPriceRow}>
                <span>Amount to pay:</span>
                <span className={styles.finalPrice}>
                  {formatPrice(booking.finalPrice)}
                </span>
              </div>
            </div>
          </div>

          <div className={styles.bookingInfo}>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Booking status:</span>
              <span
                className={`${styles.status} ${styles[booking.status.toLowerCase()]}`}
              >
                {booking.status}
              </span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>Time left:</span>
              <span
                className={`${styles.timeLeft} ${timeLeft < 10 ? styles.warning : ""}`}
              >
                {hoursLeft > 0 ? `${hoursLeft}h ` : ""}
                {minutesLeft}m
              </span>
            </div>
          </div>

          <div className={styles.actionButtons}>
            <Button
              variant="secondary"
              size="large"
              className={styles.cancelButton}
              onClick={() => setShowCancelModal(true)}
              disabled={booking.status !== "PENDING"}
            >
              Cancel Booking
            </Button>
            <Button
              variant="primary"
              size="large"
              className={styles.payButton}
              onClick={handleProceedToPayment}
              disabled={booking.status !== "PENDING"}
            >
              Proceed to Payment
            </Button>
          </div>
        </div>
      </PageContainer>
    </Layout>
  );
};
