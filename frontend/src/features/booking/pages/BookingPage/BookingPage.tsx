import React, { useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSeatReservation } from "@/features/booking/hooks/useSeatReservation";
import { useBonus } from "@/features/bonus/hooks/useBonus";
import { useBooking } from "@/features/booking/hooks/useBooking";
import { CinemaHall } from "@/features/cinema/components/CinemaHall/CinemaHall";
import { BookingSidebar } from "@/features/booking/components/BookingSidebar/BookingSidebar";
import { ProgressStepper } from "@/features/booking/components/ProgressStepper/ProgressStepper";
import { BOOKING_STEPS } from "@/features/booking/components/ProgressStepper/bookingSteps";
import { Layout } from "@/components/layout/Layout/Layout";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useNotification } from "@/context/NotificationContext";
import { useAuth } from "@/context/AuthContext";
import type { SeatInfo } from "@/features/booking/types/seatReservation";
import { Button } from "@/components/ui/Button/Button";
import { PageContainer } from "@/components/ui/PageContainer/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { formatFullDate, formatPrice, formatTime } from "@/utils/formatters";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import { useSettledLoad } from "@/hooks/common/useSettledLoad";
import styles from "./BookingPage.module.css";

export const BookingPage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const { showNotification } = useNotification();
  const { isAuthenticated } = useAuth();
  const sidebarRef = useRef<HTMLDivElement>(null);

  const {
    data: seatData,
    loading,
    loadingSeats,
    selectedSeats,
    totalPrice,
    getAvailability,
    selectSeat,
    deselectSeat,
    isSeatSelected,
    updateSeatTicketType,
  } = useSeatReservation(sessionId ?? "");

  const { getBalance } = useBonus();
  const { create, loading: bookingLoading } = useBooking();

  const loadSeats = useCallback(async () => {
    if (!sessionId) return;
    getBalance({ showErrorNotification: false }).catch(() => {});
    await getAvailability();
  }, [sessionId, getAvailability, getBalance]);
  const settled = useSettledLoad(loadSeats);

  const handleSeatClick = useCallback(
    async (seatId: number) => {
      const seat = seatData?.seats.find((s: SeatInfo) => s.id === seatId);
      if (!seat) return;

      try {
        if (isSeatSelected(seatId)) {
          await deselectSeat(seatId);
        } else {
          await selectSeat(seat);
        }
      } catch {
        return;
      }
    },
    [seatData, isSeatSelected, deselectSeat, selectSeat],
  );

  const handleBooking = useCallback(
    async (bonusPointsToUse: number) => {
      if (!selectedSeats.length) {
        showNotification("Please select at least one seat", "warning");
        return;
      }

      if (!isAuthenticated) {
        showNotification("You need to be logged in to book tickets", "warning");
        return;
      }

      if (!sessionId) return;

      const seats = selectedSeats.map((seat) => ({
        seatId: seat.seat.id,
        ticketTypeId: seat.ticketTypeId,
      }));

      try {
        const response = await create({
          sessionId,
          seats,
          bonusPointsToUse: bonusPointsToUse > 0 ? bonusPointsToUse : undefined,
        });

        if (response) {
          navigate(`/booking/summary/${response.publicId}`);
        }
      } catch {
        return;
      }
    },
    [
      selectedSeats,
      sessionId,
      create,
      navigate,
      showNotification,
      isAuthenticated,
    ],
  );

  const scrollToSidebar = useCallback(() => {
    sidebarRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, []);

  if (loading || !settled) {
    return (
      <Layout>{loading && <LoadingSpinner text="Loading seats..." />}</Layout>
    );
  }

  if (!seatData) {
    return (
      <Layout>
        <PageContainer size="narrow">
          <EmptyState variant="error" title="No Seat Data Available" />
        </PageContainer>
      </Layout>
    );
  }

  return (
    <Layout>
      <PageContainer size="wide" className={styles.bookingPage}>
        <ProgressStepper
          steps={BOOKING_STEPS}
          currentStep={1}
          className={styles.stepper}
        />

        <PageHeader
          align="center"
          divider
          title={seatData.movieTitle}
          subtitle={
            <div className={styles.sessionInfo}>
              <span>{formatFullDate(seatData.sessionTime)}</span>
              <span>{formatTime(seatData.sessionTime)}</span>
              <span>{seatData.hallName}</span>
              <span>Available seats: {seatData.availableSeats}</span>
              <span>Base Price: {formatPrice(seatData.basePrice)}</span>
            </div>
          }
        />

        <div className={styles.content}>
          <div className={styles.hallSection}>
            <CinemaHall
              seats={seatData.seats}
              selectedSeats={selectedSeats.map((s) => s.seat.id)}
              loadingSeats={loadingSeats}
              onSeatClick={handleSeatClick}
            />
          </div>

          <div ref={sidebarRef} className={styles.sidebarSection}>
            <BookingSidebar
              selectedSeats={selectedSeats}
              totalPrice={totalPrice}
              onTicketTypeChange={updateSeatTicketType}
              onBooking={handleBooking}
              isBooking={bookingLoading}
            />
          </div>
        </div>

        {selectedSeats.length > 0 && (
          <div className={styles.summaryBar}>
            <div className={styles.summaryText}>
              <span>
                {selectedSeats.length}{" "}
                {selectedSeats.length === 1 ? "seat" : "seats"}
              </span>
              <strong>{formatPrice(totalPrice)}</strong>
            </div>
            <Button variant="primary" size="medium" onClick={scrollToSidebar}>
              Continue
            </Button>
          </div>
        )}
      </PageContainer>
    </Layout>
  );
};
