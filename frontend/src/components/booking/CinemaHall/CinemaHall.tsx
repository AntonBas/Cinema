import React, { useMemo } from "react";
import { X } from "lucide-react";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import styles from "./CinemaHall.module.css";
import type { SeatInfo } from "@/types/seatReservation";
import { CELL_WIDTH, CELL_HEIGHT } from "@/utils/hallLayoutGrid";
import { useElementWidth } from "@/hooks/common/useElementWidth";
import { useViewportHeight } from "@/hooks/common/useViewportHeight";

const SEAT_INSET = 8;
const TARGET_HALL_WIDTH = 560;
const HALL_CHROME_HEIGHT = 260;
const MIN_CANVAS_HEIGHT = 200;

interface CinemaHallProps {
  seats: SeatInfo[];
  selectedSeats: number[];
  loadingSeats?: number[];
  onSeatClick: (seatId: number) => void;
}

export const CinemaHall: React.FC<CinemaHallProps> = ({
  seats,
  selectedSeats,
  loadingSeats = [],
  onSeatClick,
}) => {
  const { ref: viewportRef, width: viewportWidth } =
    useElementWidth<HTMLDivElement>();
  const viewportHeight = useViewportHeight();

  const canvasSize = useMemo(() => {
    const maxX = seats.reduce((max, seat) => Math.max(max, seat.x), 0);
    const maxY = seats.reduce((max, seat) => Math.max(max, seat.y), 0);
    return {
      width: maxX + CELL_WIDTH,
      height: maxY + CELL_HEIGHT,
    };
  }, [seats]);

  const availableCanvasHeight = Math.max(
    MIN_CANVAS_HEIGHT,
    viewportHeight - HALL_CHROME_HEIGHT,
  );
  const compactScale = Math.min(
    1,
    TARGET_HALL_WIDTH / canvasSize.width,
    availableCanvasHeight / canvasSize.height,
  );
  const scale =
    viewportWidth > 0
      ? Math.min(compactScale, viewportWidth / canvasSize.width)
      : compactScale;
  const hallWidth = Math.max(360, canvasSize.width * compactScale + 48);

  const getSeatInfo = (seat: SeatInfo) => {
    const status = !seat.active
      ? "inactive"
      : selectedSeats.includes(seat.id)
        ? "selected"
        : seat.temporarilyReserved
          ? "temporary"
          : !seat.available
            ? "booked"
            : "available";

    const typeName =
      seat.seatType === "VIP"
        ? "VIP"
        : seat.seatType === "COUPLE"
          ? "Couple"
          : "Standard";
    const statusText =
      status === "booked"
        ? "Booked"
        : status === "inactive"
          ? "Unavailable"
          : status === "temporary"
            ? "Temporarily reserved"
            : status === "selected"
              ? "Selected"
              : "Available";

    return { status, typeName, statusText };
  };

  return (
    <div className={styles.cinemaHall} style={{ maxWidth: hallWidth }}>
      <div className={styles.screenArea}>
        <div className={styles.screen}>SCREEN</div>
        <div className={styles.screenReflection} />
      </div>

      <div className={styles.seatsLayout}>
        <div ref={viewportRef} className={styles.canvasViewport}>
          <div
            className={styles.canvasSizer}
            style={{
              width: canvasSize.width * scale,
              height: canvasSize.height * scale,
            }}
          >
            <div
              className={styles.canvas}
              style={{
                width: canvasSize.width,
                height: canvasSize.height,
                transform: `scale(${scale})`,
              }}
            >
              {seats.map((seat) => {
                const { status, typeName, statusText } = getSeatInfo(seat);
                const isLoading = loadingSeats.includes(seat.id);
                const disabled =
                  status === "inactive" ||
                  status === "booked" ||
                  status === "temporary" ||
                  isLoading;

                const width =
                  seat.seatType === "COUPLE"
                    ? CELL_WIDTH * 2 - SEAT_INSET * 2
                    : CELL_WIDTH - SEAT_INSET * 2;
                const seatClass = `${styles.seatButton} ${styles[seat.seatType.toLowerCase()]} ${
                  status === "inactive"
                    ? styles.inactive
                    : status === "selected"
                      ? styles.selected
                      : status === "temporary"
                        ? styles.temporary
                        : status === "booked"
                          ? styles.booked
                          : ""
                }`;

                const title = `Row ${seat.row}, Seat ${seat.seatNumber} (${typeName}) - ${statusText}`;

                return (
                  <Tooltip
                    key={`seat-${seat.id}`}
                    content={title}
                    position={seat.row === 1 ? "bottom" : "top"}
                    style={{
                      position: "absolute",
                      left: seat.x + SEAT_INSET,
                      top: seat.y + SEAT_INSET,
                    }}
                  >
                    <button
                      className={seatClass}
                      style={{ width, height: CELL_HEIGHT - SEAT_INSET * 2 }}
                      onClick={() => onSeatClick(seat.id)}
                      disabled={disabled}
                    >
                      {isLoading ? (
                        <span className={styles.loadingSpinner} />
                      ) : (
                        <span className={styles.seatNumber}>
                          {seat.seatNumber}
                        </span>
                      )}
                      {!seat.active && (
                        <div className={styles.inactiveOverlay}>
                          <X size={14} className={styles.inactiveIcon} />
                        </div>
                      )}
                    </button>
                  </Tooltip>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      <div className={styles.legend}>
        <div className={styles.legendGrid}>
          <div className={styles.legendItem}>
            <div className={`${styles.legendColor} ${styles.standard}`} />
            <span>Standard</span>
          </div>
          <div className={styles.legendItem}>
            <div className={`${styles.legendColor} ${styles.vip}`} />
            <span>VIP</span>
          </div>
          <div className={styles.legendItem}>
            <div className={`${styles.legendColor} ${styles.couple}`} />
            <span>Couple</span>
          </div>
        </div>
      </div>
    </div>
  );
};
