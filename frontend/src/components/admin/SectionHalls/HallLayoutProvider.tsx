import React, { useCallback, useRef, useState } from "react";
import type {
  CinemaHallResponse,
  HallLayoutResponse,
  SeatLayoutItem,
} from "@/types/cinemaHall";
import { SeatType } from "@/types/seat";
import { useCinemaHall } from "@/hooks/features/cinemaHall/useCinemaHall";
import { CELL_WIDTH, CELL_HEIGHT, GRID_COLS } from "@/utils/hallLayoutGrid";
import { HallLayoutContext, type DraftSeat } from "./HallLayoutContext";

const nextSeatType = (type: SeatType): SeatType => {
  const order: SeatType[] = [SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE];
  return order[(order.indexOf(type) + 1) % order.length];
};

const toDraftSeats = (layout: HallLayoutResponse | null): DraftSeat[] => {
  if (!layout) return [];
  return layout.rows
    .flatMap((row) => row.seats)
    .map((seat) => ({
      key: `seat-${seat.id}`,
      id: seat.id,
      col: Math.round(seat.x / CELL_WIDTH),
      gridRow: Math.round(seat.y / CELL_HEIGHT),
      seatType: seat.seatType,
      active: seat.active,
    }));
};

export const HallLayoutProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [currentHall, setCurrentHall] = useState<CinemaHallResponse | null>(
    null,
  );
  const [layout, setLayout] = useState<HallLayoutResponse | null>(null);
  const [seats, setSeats] = useState<DraftSeat[]>([]);
  const [isDirty, setIsDirty] = useState(false);
  const [saving, setSaving] = useState(false);
  const [layoutSaveCount, setLayoutSaveCount] = useState(0);
  const localKeyCounter = useRef(0);

  const { getLayout, updateLayout } = useCinemaHall();

  const openLayout = useCallback(
    async (hall: CinemaHallResponse) => {
      setCurrentHall(hall);
      try {
        const layoutData = await getLayout(hall.id);
        if (!layoutData) {
          setCurrentHall(null);
          return;
        }
        setLayout(layoutData);
        setSeats(toDraftSeats(layoutData));
        setIsDirty(false);
      } catch {
        setCurrentHall(null);
      }
    },
    [getLayout],
  );

  const closeLayout = useCallback(() => {
    setCurrentHall(null);
    setLayout(null);
    setSeats([]);
    setIsDirty(false);
  }, []);

  const isCellOccupied = useCallback(
    (
      col: number,
      gridRow: number,
      ignoreKey?: string,
      all: DraftSeat[] = seats,
    ) => {
      return all.some((seat) => {
        if (seat.key === ignoreKey) return false;
        if (seat.gridRow !== gridRow) return false;
        const occupiedCols =
          seat.seatType === SeatType.COUPLE
            ? [seat.col, seat.col + 1]
            : [seat.col];
        return occupiedCols.includes(col);
      });
    },
    [seats],
  );

  const addSeat = useCallback(
    (col: number, gridRow: number) => {
      setSeats((prev) => {
        if (isCellOccupied(col, gridRow, undefined, prev)) return prev;
        localKeyCounter.current += 1;
        const newSeat: DraftSeat = {
          key: `new-${localKeyCounter.current}`,
          id: null,
          col,
          gridRow,
          seatType: SeatType.STANDARD,
          active: true,
        };
        return [...prev, newSeat];
      });
      setIsDirty(true);
    },
    [isCellOccupied],
  );

  const moveSeat = useCallback(
    (key: string, col: number, gridRow: number) => {
      setSeats((prev) => {
        const seat = prev.find((s) => s.key === key);
        if (!seat) return prev;
        const targetCols =
          seat.seatType === SeatType.COUPLE ? [col, col + 1] : [col];
        const blocked = targetCols.some(
          (targetCol) =>
            targetCol >= GRID_COLS ||
            isCellOccupied(targetCol, gridRow, key, prev),
        );
        if (blocked) return prev;
        return prev.map((seat) =>
          seat.key === key ? { ...seat, col, gridRow } : seat,
        );
      });
      setIsDirty(true);
    },
    [isCellOccupied],
  );

  const removeSeat = useCallback((key: string) => {
    setSeats((prev) => prev.filter((seat) => seat.key !== key));
    setIsDirty(true);
  }, []);

  const cycleSeatType = useCallback(
    (key: string) => {
      setSeats((prev) => {
        const seat = prev.find((s) => s.key === key);
        if (!seat) return prev;
        const type = nextSeatType(seat.seatType);
        if (type === SeatType.COUPLE) {
          const rightCol = seat.col + 1;
          if (
            rightCol >= GRID_COLS ||
            isCellOccupied(rightCol, seat.gridRow, key, prev)
          ) {
            return prev;
          }
        }
        return prev.map((s) => (s.key === key ? { ...s, seatType: type } : s));
      });
      setIsDirty(true);
    },
    [isCellOccupied],
  );

  const toggleSeatActive = useCallback((key: string) => {
    setSeats((prev) =>
      prev.map((seat) =>
        seat.key === key ? { ...seat, active: !seat.active } : seat,
      ),
    );
    setIsDirty(true);
  }, []);

  const saveLayout = useCallback(async () => {
    if (!currentHall) return false;
    setSaving(true);
    try {
      const byRow = new Map<number, DraftSeat[]>();
      seats.forEach((seat) => {
        const rowSeats = byRow.get(seat.gridRow) ?? [];
        rowSeats.push(seat);
        byRow.set(seat.gridRow, rowSeats);
      });

      const payload: SeatLayoutItem[] = [];
      byRow.forEach((rowSeats, gridRow) => {
        [...rowSeats]
          .sort((a, b) => a.col - b.col)
          .forEach((seat, index) => {
            payload.push({
              id: seat.id,
              row: gridRow + 1,
              number: index + 1,
              seatType: seat.seatType,
              x: seat.col * CELL_WIDTH,
              y: gridRow * CELL_HEIGHT,
              active: seat.active,
            });
          });
      });

      const updated = await updateLayout(currentHall.id, { seats: payload });
      if (updated) {
        setLayout(updated);
        setSeats(toDraftSeats(updated));
        setIsDirty(false);
        setLayoutSaveCount((count) => count + 1);
        return true;
      }
      return false;
    } catch {
      return false;
    } finally {
      setSaving(false);
    }
  }, [currentHall, seats, updateLayout]);

  const loading = !!(currentHall && !layout);

  return (
    <HallLayoutContext.Provider
      value={{
        currentHall,
        layout,
        seats,
        isDirty,
        saving,
        loading,
        layoutSaveCount,
        openLayout,
        closeLayout,
        addSeat,
        moveSeat,
        removeSeat,
        cycleSeatType,
        toggleSeatActive,
        saveLayout,
      }}
    >
      {children}
    </HallLayoutContext.Provider>
  );
};
