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
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { useNotification } from "@/context/NotificationContext";
import type { SeatInfo } from "@/types/seatReservation";
import styles from "./BookingPage.module.css";

export const BookingPage: React.FC = () => {
  const { sessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();
  const sessionIdNum = parseInt(sessionId || "0");

  const { showNotification } = useNotification();

  const {
    data: seatData,
    loading,
    selectedSeats,
    totalPrice,
    getSeatAvailability,
    selectSeat,
    deselectSeat,
    isSeatSelected,
    updateSeatTicketType,
  } = useSeatReservation(sessionIdNum);

  const { getMyBalance } = useBonus();
  const { create, loading: bookingLoading } = useBooking();

  useEffect(() => {
    if (sessionIdNum) {
      getSeatAvailability();
      getMyBalance();
    }
  }, [sessionIdNum]);

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

      const token = localStorage.getItem("authToken");
      if (!token) {
        showNotification("You need to be logged in to book tickets", "warning");
        return;
      }

      const seats = selectedSeats.map((seat) => ({
        seatId: seat.seat.id,
        ticketTypeId: seat.ticketTypeId,
      }));

      const response = await create({
        sessionId: sessionIdNum,
        seats,
        bonusPointsToUse: bonusPointsToUse > 0 ? bonusPointsToUse : undefined,
      });

      if (response) {
        navigate(`/booking/summary/${response.id}`);
      }
    },
    [selectedSeats, sessionIdNum, create, navigate, showNotification],
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
        <div className={styles.error}>No seat data available</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className={styles.bookingPage}>
        <ProgressStepper
          steps={BOOKING_STEPS}
          currentStep={1}
          className={styles.stepper}
        />

        <div className={styles.header}>
          <h1>{seatData.movieTitle}</h1>
          <div className={styles.sessionInfo}>
            <span>{seatData.hallName}</span>
            <span>Available seats: {seatData.availableSeats}</span>
            <span>
              Base Price: {parseFloat(seatData.basePrice).toFixed(2)}₴
            </span>
          </div>
        </div>

        <div className={styles.content}>
          <div className={styles.hallSection}>
            <CinemaHall
              seats={seatData.seats}
              selectedSeats={selectedSeats.map((s) => s.seat.id)}
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
      </div>
    </Layout>
  );
};
