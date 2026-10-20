import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { SeatType } from "@/features/cinema/types/seat";
import type { SeatInfo } from "@/features/booking/types/seatReservation";
import { CinemaHall } from "./CinemaHall";

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
  ticketPrices: [],
  ...overrides,
});

const seats: SeatInfo[] = [
  seat({ id: 1, seatNumber: 1 }),
  seat({ id: 2, seatNumber: 2, x: 60, available: false }),
  seat({ id: 3, seatNumber: 3, x: 120, temporarilyReserved: true }),
  seat({ id: 4, seatNumber: 4, x: 180, active: false }),
  seat({ id: 5, seatNumber: 5, x: 240, seatType: SeatType.VIP }),
  seat({ id: 6, seatNumber: 6, x: 300, seatType: SeatType.COUPLE }),
];

const seatButton = (seatNumber: number) =>
  screen.getByRole("button", { name: String(seatNumber) });

describe("CinemaHall", () => {
  it("disables booked, temporarily reserved and inactive seats", () => {
    render(
      <CinemaHall seats={seats} selectedSeats={[]} onSeatClick={vi.fn()} />,
    );

    expect(seatButton(1)).toBeEnabled();
    expect(seatButton(2)).toBeDisabled();
    expect(seatButton(3)).toBeDisabled();
    expect(seatButton(4)).toBeDisabled();
    expect(seatButton(5)).toBeEnabled();
  });

  it("describes seat type and status in a tooltip on hover", async () => {
    const user = userEvent.setup();
    render(
      <CinemaHall seats={seats} selectedSeats={[1]} onSeatClick={vi.fn()} />,
    );

    expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();

    const descriptions: Array<[number, string]> = [
      [1, "Row 1, Seat 1 (Standard) - Selected"],
      [2, "Row 1, Seat 2 (Standard) - Booked"],
      [3, "Row 1, Seat 3 (Standard) - Temporarily reserved"],
      [4, "Row 1, Seat 4 (Standard) - Unavailable"],
      [5, "Row 1, Seat 5 (VIP) - Available"],
      [6, "Row 1, Seat 6 (Couple) - Available"],
    ];

    for (const [seatNumber, description] of descriptions) {
      const seat = seatButton(seatNumber);
      await user.hover(seat);
      expect(await screen.findByRole("tooltip")).toHaveTextContent(description);
      await user.unhover(seat);
      expect(screen.queryByRole("tooltip")).not.toBeInTheDocument();
    }
  });

  it("renders couple seats twice as wide as standard seats", () => {
    render(
      <CinemaHall seats={seats} selectedSeats={[]} onSeatClick={vi.fn()} />,
    );

    expect(seatButton(6).style.width).toBe("104px");
    expect(seatButton(1).style.width).toBe("44px");
  });

  it("reports clicks on available seats only", async () => {
    const onSeatClick = vi.fn();
    render(
      <CinemaHall seats={seats} selectedSeats={[]} onSeatClick={onSeatClick} />,
    );

    await userEvent.click(seatButton(1));
    await userEvent.click(seatButton(2));

    expect(onSeatClick).toHaveBeenCalledTimes(1);
    expect(onSeatClick).toHaveBeenCalledWith(1);
  });

  it("disables a seat while its hold request is pending", () => {
    render(
      <CinemaHall
        seats={seats}
        selectedSeats={[]}
        loadingSeats={[1]}
        onSeatClick={vi.fn()}
      />,
    );

    expect(screen.getAllByRole("button")[0]).toBeDisabled();
  });
});
