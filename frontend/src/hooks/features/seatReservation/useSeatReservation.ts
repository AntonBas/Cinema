import { useState, useCallback, useMemo, useRef } from "react";
import { seatReservationApi } from "@/api/seatReservationApi";
import type {
  SeatReservationResponse,
  SeatInfo,
  TicketPriceInfo,
} from "@/types/seatReservation";
import { useApi, type UseApiOptions } from "@/hooks/common/useApi";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { useNotification } from "@/context/NotificationContext";

export interface SelectedSeat {
  seat: SeatInfo;
  ticketTypeId: number;
  price: number;
  ticketTypeName: string;
}

export const useSeatReservation = (sessionId: string, maxSeats?: number) => {
  const [selectedSeats, setSelectedSeats] = useState<SelectedSeat[]>([]);
  const [pendingSeatIds, setPendingSeatIds] = useState<number[]>([]);

  const { showNotification } = useNotification();
  const seatApi = useApi<SeatReservationResponse>();

  const seatApiRef = useRef(seatApi);
  const pendingSeatIdsRef = useRef(new Set<number>());
  const selectedSeatsRef = useRef<SelectedSeat[]>([]);

  seatApiRef.current = seatApi;
  selectedSeatsRef.current = selectedSeats;

  const loading = useDelayedLoading(seatApi.loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const getAvailability = useCallback(
    async (options?: UseApiOptions<SeatReservationResponse>) => {
      return seatApiRef.current.execute(
        () => seatReservationApi.getAvailability(sessionId),
        options,
      );
    },
    [sessionId],
  );

  const updateSeatLocally = useCallback(
    (seatId: number, updates: Partial<SeatInfo>) => {
      seatApiRef.current.updateData((current) => {
        if (!current) return current;

        const updatedSeats = current.seats.map((seat) =>
          seat.id === seatId ? { ...seat, ...updates } : seat,
        );

        return {
          ...current,
          seats: updatedSeats,
          availableSeats: updatedSeats.filter((seat) => seat.available).length,
        };
      });
    },
    [],
  );

  const markPending = useCallback((seatId: number) => {
    if (pendingSeatIdsRef.current.has(seatId)) return false;
    pendingSeatIdsRef.current.add(seatId);
    setPendingSeatIds(Array.from(pendingSeatIdsRef.current));
    return true;
  }, []);

  const unmarkPending = useCallback((seatId: number) => {
    pendingSeatIdsRef.current.delete(seatId);
    setPendingSeatIds(Array.from(pendingSeatIdsRef.current));
  }, []);

  const addSelectedSeat = useCallback((selectedSeat: SelectedSeat) => {
    selectedSeatsRef.current = [...selectedSeatsRef.current, selectedSeat];
    setSelectedSeats(selectedSeatsRef.current);
  }, []);

  const removeSelectedSeat = useCallback((seatId: number) => {
    selectedSeatsRef.current = selectedSeatsRef.current.filter(
      (selected) => selected.seat.id !== seatId,
    );
    setSelectedSeats(selectedSeatsRef.current);
  }, []);

  const notifyError = useCallback(
    (error: unknown) => {
      const message =
        error instanceof Error && error.message
          ? error.message
          : "Operation failed";
      showNotification(message, "error");
    },
    [showNotification],
  );

  const releaseHold = useCallback(
    async (seatId: number) => {
      try {
        await seatReservationApi.release(sessionId, seatId);
        removeSelectedSeat(seatId);
        updateSeatLocally(seatId, {
          available: true,
          temporarilyReserved: false,
        });
      } catch (error) {
        notifyError(error);
      }
    },
    [sessionId, removeSelectedSeat, updateSeatLocally, notifyError],
  );

  const getTicketPrice = useCallback(
    (seat: SeatInfo, ticketTypeId?: number): TicketPriceInfo | null => {
      if (!seat.ticketPrices?.length) return null;
      if (ticketTypeId) {
        return (
          seat.ticketPrices.find((tp) => tp.ticketTypeId === ticketTypeId) ||
          null
        );
      }
      return seat.ticketPrices[0] || null;
    },
    [],
  );

  const selectSeat = useCallback(
    async (seat: SeatInfo, ticketTypeId?: number) => {
      if (pendingSeatIdsRef.current.has(seat.id)) return;
      if (selectedSeatsRef.current.some((s) => s.seat.id === seat.id)) return;

      if (maxSeats && selectedSeatsRef.current.length >= maxSeats) {
        showNotification(`Maximum ${maxSeats} seats allowed`, "warning");
        return;
      }

      if (!seat.available || seat.temporarilyReserved) {
        showNotification("This seat is not available", "warning");
        return;
      }

      const ticketPrice = getTicketPrice(seat, ticketTypeId);
      if (!ticketPrice) {
        showNotification("No ticket price available for this seat", "error");
        return;
      }

      markPending(seat.id);
      addSelectedSeat({
        seat,
        ticketTypeId: ticketPrice.ticketTypeId,
        price: parseFloat(ticketPrice.finalPrice),
        ticketTypeName: ticketPrice.ticketTypeName,
      });

      try {
        await seatReservationApi.hold(sessionId, seat.id);
        updateSeatLocally(seat.id, {
          available: false,
          temporarilyReserved: true,
        });
      } catch (error) {
        removeSelectedSeat(seat.id);
        notifyError(error);
      } finally {
        unmarkPending(seat.id);
      }
    },
    [
      sessionId,
      maxSeats,
      getTicketPrice,
      markPending,
      unmarkPending,
      addSelectedSeat,
      removeSelectedSeat,
      updateSeatLocally,
      notifyError,
      showNotification,
    ],
  );

  const deselectSeat = useCallback(
    async (seatId: number) => {
      if (!markPending(seatId)) return;
      try {
        await releaseHold(seatId);
      } finally {
        unmarkPending(seatId);
      }
    },
    [markPending, unmarkPending, releaseHold],
  );

  const updateSeatTicketType = useCallback(
    (seatId: number, ticketTypeId: number) => {
      const seat = seatApi.data?.seats.find((s) => s.id === seatId);
      if (!seat) return;

      const ticketPrice = getTicketPrice(seat, ticketTypeId);
      if (!ticketPrice) return;

      setSelectedSeats((prev) =>
        prev.map((selected) =>
          selected.seat.id === seatId
            ? {
                ...selected,
                ticketTypeId,
                price: parseFloat(ticketPrice.finalPrice),
                ticketTypeName: ticketPrice.ticketTypeName,
              }
            : selected,
        ),
      );
    },
    [seatApi.data, getTicketPrice],
  );

  const clearSelection = useCallback(() => {
    selectedSeatsRef.current.forEach((selected) => {
      deselectSeat(selected.seat.id);
    });
  }, [deselectSeat]);

  const isSeatSelected = useCallback(
    (seatId: number) => {
      return selectedSeats.some((s) => s.seat.id === seatId);
    },
    [selectedSeats],
  );

  const totalPrice = useMemo(() => {
    return selectedSeats.reduce((sum, seat) => sum + seat.price, 0);
  }, [selectedSeats]);

  return {
    data: seatApi.data,
    loading,
    loadingSeats: pendingSeatIds,
    error: seatApi.error,
    selectedSeats,
    totalPrice,
    totalSelected: selectedSeats.length,
    getAvailability,
    reset: seatApi.reset,
    selectSeat,
    deselectSeat,
    updateSeatTicketType,
    clearSelection,
    isSeatSelected,
    availableSeatsCount: seatApi.data?.availableSeats ?? 0,
    seats: seatApi.data?.seats || [],
    hallName: seatApi.data?.hallName,
    movieTitle: seatApi.data?.movieTitle,
    basePrice: seatApi.data?.basePrice,
    hasData: !!seatApi.data,
    isSoldOut: (seatApi.data?.availableSeats ?? 0) === 0,
  };
};
