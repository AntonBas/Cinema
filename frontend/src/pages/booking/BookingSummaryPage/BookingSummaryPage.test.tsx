import { render, screen } from "@testing-library/react";
import type { ReactNode } from "react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { useBooking } from "@/hooks/features/booking/useBooking";
import type { BookingResponse } from "@/types/booking";
import { BookingSummaryPage } from "./BookingSummaryPage";

vi.mock("@/components/layout/Layout/Layout", () => ({
  Layout: ({ children }: { children: ReactNode }) => <div>{children}</div>,
}));

vi.mock("@/hooks/common/useSettledLoad", () => ({
  useSettledLoad: () => true,
}));

vi.mock("@/hooks/features/booking/useBooking", () => ({
  useBooking: vi.fn(),
}));

const NOW = Date.parse("2026-10-15T12:00:00Z");

const booking = (
  overrides: Partial<BookingResponse> = {},
): BookingResponse => ({
  id: 1,
  publicId: "booking-public-id",
  bookingNumber: "BK-2026-00001",
  status: "PENDING",
  sessionId: 5,
  sessionPublicId: "session-public-id",
  sessionTime: "2026-10-20T15:30:00Z",
  movieTitle: "Inception",
  hallName: "Hall A",
  totalPrice: "300.00",
  bonusPointsUsed: 0,
  bonusDiscountAmount: "0.00",
  finalPrice: "300.00",
  expiresAt: new Date(NOW + 15 * 60_000).toISOString(),
  seatReservations: [
    {
      id: 1,
      seatId: 11,
      row: 3,
      seatNumber: 7,
      ticketTypeName: "Adult",
      seatPrice: "180.00",
    },
    {
      id: 2,
      seatId: 12,
      row: 3,
      seatNumber: 8,
      ticketTypeName: "Student",
      seatPrice: "120.00",
    },
  ],
  ...overrides,
});

const renderPage = (bookingResponse: BookingResponse) => {
  vi.mocked(useBooking).mockReturnValue({
    booking: bookingResponse,
    loading: false,
    getById: vi.fn(),
    cancel: vi.fn(),
  } as unknown as ReturnType<typeof useBooking>);

  return render(
    <MemoryRouter initialEntries={["/booking/summary/booking-public-id"]}>
      <Routes>
        <Route
          path="/booking/summary/:bookingId"
          element={<BookingSummaryPage />}
        />
      </Routes>
    </MemoryRouter>,
  );
};

const priceRow = (label: string) =>
  screen.getByText(label).parentElement as HTMLElement;

describe("BookingSummaryPage", () => {
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(NOW);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("shows seat prices and the amount to pay as returned by the backend", () => {
    renderPage(booking());

    expect(priceRow("Row 3, Seat 7").parentElement).toHaveTextContent(
      "180.00 ₴",
    );
    expect(priceRow("Row 3, Seat 8").parentElement).toHaveTextContent(
      "120.00 ₴",
    );
    expect(priceRow("Amount to pay:")).toHaveTextContent("300.00 ₴");
    expect(screen.queryByText("Bonus discount:")).not.toBeInTheDocument();
  });

  it("shows the bonus discount breakdown from backend values", () => {
    renderPage(
      booking({
        bonusPointsUsed: 50,
        bonusDiscountAmount: "50.00",
        finalPrice: "250.00",
      }),
    );

    expect(priceRow("Total Price:")).toHaveTextContent("300.00 ₴");
    expect(priceRow("Bonus discount:")).toHaveTextContent("-50.00 ₴");
    expect(priceRow("Amount to pay:")).toHaveTextContent("250.00 ₴");
  });

  it("shows the time left without a warning when there is enough time", () => {
    renderPage(booking());

    const timeLeft = screen.getByText("15m");
    expect(timeLeft.className).not.toMatch(/warning/);
  });

  it("warns when less than ten minutes are left", () => {
    renderPage(
      booking({ expiresAt: new Date(NOW + 5 * 60_000).toISOString() }),
    );

    expect(screen.getByText("5m").className).toMatch(/warning/);
  });

  it("shows Expired once the booking window has passed", () => {
    renderPage(booking({ expiresAt: new Date(NOW - 60_000).toISOString() }));

    expect(screen.getByText("Expired").className).toMatch(/warning/);
  });

  it("disables booking actions when the booking is no longer pending", () => {
    renderPage(booking({ status: "CANCELLED" }));

    expect(
      screen.getByRole("button", { name: "Proceed to Payment" }),
    ).toBeDisabled();
    expect(
      screen.getByRole("button", { name: "Cancel Booking" }),
    ).toBeDisabled();
  });
});
