import { act, renderHook } from "@testing-library/react";
import type { AxiosResponse } from "axios";
import type { ReactNode } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { seatReservationApi } from "@/api/seatReservationApi";
import {
  NotificationContext,
  type NotificationContextType,
} from "@/context/NotificationContext";
import { SeatType } from "@/types/seat";
import type {
  SeatInfo,
  SeatReservationResponse,
} from "@/types/seatReservation";
import { useSeatReservation } from "./useSeatReservation";

vi.mock("@/api/seatReservationApi", () => ({
  seatReservationApi: {
    getAvailability: vi.fn(),
    hold: vi.fn(),
    release: vi.fn(),
  },
}));

const SESSION_ID = "session-1";

const seat = (overrides: Partial<SeatInfo>): SeatInfo => ({
  id: 1,
  row: 1,
  seatNumber: 1,
  seatType: SeatType.STANDARD,
  x: 0,
  y: 0,
  available: true,
  temporarilyReserved: false,
  active: true,
  ticketPrices: [
    {
      ticketTypeId: 10,
      ticketTypeName: "Adult",
      finalPrice: "150.00",
      requiresDocument: false,
    },
    {
      ticketTypeId: 11,
      ticketTypeName: "Student",
      finalPrice: "120.50",
      requiresDocument: true,
    },
  ],
  ...overrides,
});

const availability = (seats: SeatInfo[]): SeatReservationResponse => ({
  sessionId: 1,
  movieTitle: "Inception",
  basePrice: "150.00",
  hallName: "Hall A",
  sessionTime: "2026-10-20T18:30:00Z",
  availableSeats: seats.filter((s) => s.available).length,
  seats,
});

const axiosResponse = <T,>(data: T) => ({ data }) as AxiosResponse<T>;

const showNotification = vi.fn();
const notificationContext: NotificationContextType = {
  notifications: [],
  showNotification,
  hideNotification: vi.fn(),
};

const wrapper = ({ children }: { children: ReactNode }) => (
  <NotificationContext.Provider value={notificationContext}>
    {children}
  </NotificationContext.Provider>
);

const renderSeatReservation = (maxSeats?: number) =>
  renderHook(() => useSeatReservation(SESSION_ID, maxSeats), { wrapper });

describe("useSeatReservation", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(seatReservationApi.hold).mockResolvedValue(
      axiosResponse(undefined),
    );
    vi.mocked(seatReservationApi.release).mockResolvedValue(
      axiosResponse(undefined),
    );
  });

  it("holds a selected seat and takes its price from the backend", async () => {
    const { result } = renderSeatReservation();

    await act(() => result.current.selectSeat(seat({ id: 1 })));

    expect(seatReservationApi.hold).toHaveBeenCalledWith(SESSION_ID, 1);
    expect(result.current.selectedSeats).toHaveLength(1);
    expect(result.current.selectedSeats[0]).toMatchObject({
      ticketTypeId: 10,
      ticketTypeName: "Adult",
      price: 150,
    });
    expect(result.current.totalPrice).toBe(150);
  });

  it("uses the price of the requested ticket type", async () => {
    const { result } = renderSeatReservation();

    await act(() => result.current.selectSeat(seat({ id: 1 }), 11));

    expect(result.current.selectedSeats[0]).toMatchObject({
      ticketTypeId: 11,
      price: 120.5,
    });
  });

  it("does not select more seats than the limit", async () => {
    const { result } = renderSeatReservation(1);

    await act(() => result.current.selectSeat(seat({ id: 1 })));
    await act(() => result.current.selectSeat(seat({ id: 2 })));

    expect(result.current.totalSelected).toBe(1);
    expect(seatReservationApi.hold).toHaveBeenCalledTimes(1);
    expect(showNotification).toHaveBeenCalledWith(
      "Maximum 1 seats allowed",
      "warning",
    );
  });

  it.each([
    ["booked", { available: false }],
    ["temporarily reserved", { temporarilyReserved: true }],
  ])("refuses a %s seat", async (_, overrides) => {
    const { result } = renderSeatReservation();

    await act(() => result.current.selectSeat(seat(overrides)));

    expect(result.current.totalSelected).toBe(0);
    expect(seatReservationApi.hold).not.toHaveBeenCalled();
    expect(showNotification).toHaveBeenCalledWith(
      "This seat is not available",
      "warning",
    );
  });

  it("refuses a seat without a ticket price", async () => {
    const { result } = renderSeatReservation();

    await act(() => result.current.selectSeat(seat({ ticketPrices: [] })));

    expect(result.current.totalSelected).toBe(0);
    expect(showNotification).toHaveBeenCalledWith(
      "No ticket price available for this seat",
      "error",
    );
  });

  it("rolls back the selection when the hold fails", async () => {
    vi.mocked(seatReservationApi.hold).mockRejectedValue(
      new Error("Seat already taken"),
    );
    const { result } = renderSeatReservation();

    await act(() => result.current.selectSeat(seat({ id: 1 })));

    expect(result.current.totalSelected).toBe(0);
    expect(result.current.loadingSeats).toEqual([]);
    expect(showNotification).toHaveBeenCalledWith(
      "Seat already taken",
      "error",
    );
  });

  it("marks a held seat as temporarily reserved and frees it on deselect", async () => {
    vi.mocked(seatReservationApi.getAvailability).mockResolvedValue(
      axiosResponse(availability([seat({ id: 1 }), seat({ id: 2 })])),
    );
    const { result } = renderSeatReservation();
    await act(() => result.current.getAvailability());

    await act(() => result.current.selectSeat(result.current.seats[0]));

    expect(result.current.seats[0]).toMatchObject({
      available: false,
      temporarilyReserved: true,
    });
    expect(result.current.availableSeatsCount).toBe(1);

    await act(() => result.current.deselectSeat(1));

    expect(seatReservationApi.release).toHaveBeenCalledWith(SESSION_ID, 1);
    expect(result.current.totalSelected).toBe(0);
    expect(result.current.seats[0]).toMatchObject({
      available: true,
      temporarilyReserved: false,
    });
  });

  it("updates the price when the ticket type changes", async () => {
    vi.mocked(seatReservationApi.getAvailability).mockResolvedValue(
      axiosResponse(availability([seat({ id: 1 })])),
    );
    const { result } = renderSeatReservation();
    await act(() => result.current.getAvailability());
    await act(() => result.current.selectSeat(result.current.seats[0]));

    act(() => result.current.updateSeatTicketType(1, 11));

    expect(result.current.selectedSeats[0]).toMatchObject({
      ticketTypeId: 11,
      ticketTypeName: "Student",
      price: 120.5,
    });
    expect(result.current.totalPrice).toBe(120.5);
  });
});
