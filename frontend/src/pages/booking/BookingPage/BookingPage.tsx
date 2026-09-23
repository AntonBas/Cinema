import React, { useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSeatReservation } from "@/hooks/features/seatReservation/useSeatReservation";
import { useBonus } from "@/hooks/features/bonus/useBonus";
import { useBooking } from "@/hooks/features/booking/useBooking";
import { CinemaHall } from "@/components/booking/CinemaHall/CinemaHall";
import { BookingSidebar } from "@/components/booking/BookingSidebar/BookingSidebar";
import { ProgressStepper } from "@/components/booking/ProgressStepper/ProgressStepper";
import { BOOKING_STEPS } from "@/components/booking/ProgressStepper/bookingSteps";
import { Layout } from "@/components/layout/Layout/Layout";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useNotification } from "@/context/NotificationContext";
import { useAuth } from "@/context/AuthContext";
import type { SeatInfo } from "@/types/seatReservation";
import { PageContainer } from "@/components/ui/PageContainer/PageContainer";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { formatPrice } from "@/utils/formatters";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./BookingPage.module.css";

export const BookingPage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const { showNotification } = useNotification();
  const { isAuthenticated } = useAuth();

  const {
    data: seatData,
    loading,
    loadingSeats,
    selectedSeats,
    totalPrice,
    getSeatAvailability,
    selectSeat,
    deselectSeat,
    isSeatSelected,
    updateSeatTicketType,
  } = useSeatReservation(sessionId ?? "");

  const { getMyBalance } = useBonus();
  const { create, loading: bookingLoading } = useBooking();

  useEffect(() => {
    if (sessionId) {
      getSeatAvailability();
      getMyBalance({ showErrorNotification: false }).catch(() => {});
    }
  }, [sessionId, getSeatAvailability, getMyBalance]);

  const handleSeatClick = useCallback(
    async (seatId: number) => {
      const seat = seatData?.seats.find((s: SeatInfo) => s.id === seatId);
      if (!seat) return;

      if (isSeatSelected(seatId)) {
        await deselectSeat(seatId);
      } else {
        await selectSeat(seat);
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

  if (loading) {
    return (
      <Layout>
        <LoadingSpinner text="Loading seats..." />
      </Layout>
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

          <div className={styles.sidebarSection}>
            <BookingSidebar
              selectedSeats={selectedSeats}
              totalPrice={totalPrice}
              onTicketTypeChange={updateSeatTicketType}
              onBooking={handleBooking}
              isBooking={bookingLoading}
            />
          </div>
        </div>
      </PageContainer>
    </Layout>
  );
};
