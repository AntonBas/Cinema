import React from "react";
import { X } from "lucide-react";
import { Tooltip } from "@/components/ui/Tooltip/Tooltip";
import styles from "./CinemaHall.module.css";
import type { SeatInfo } from "@/types/seatReservation";

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
  const rows = [...new Set(seats.map((seat) => seat.row))].sort(
    (a, b) => a - b,
  );

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
    <div className={styles.cinemaHall}>
      <div className={styles.screenArea}>
        <div className={styles.screen}>SCREEN</div>
        <div className={styles.screenReflection} />
      </div>

      <div className={styles.seatsLayout}>
        <div className={styles.rowsContainer}>
          {rows.map((rowNumber) => {
            const rowSeats = seats
              .filter((seat) => seat.row === rowNumber)
              .sort((a, b) => a.seatNumber - b.seatNumber);

            return (
              <div key={`row-${rowNumber}`} className={styles.row}>
                <div className={styles.rowLabel}>
                  <span className={styles.rowNumber}>{rowNumber}</span>
                </div>
                <div className={styles.seatsRow}>
                  {rowSeats.map((seat) => {
                    const { status, typeName, statusText } = getSeatInfo(seat);
                    const isLoading = loadingSeats.includes(seat.id);
                    const disabled =
                      status === "inactive" ||
                      status === "booked" ||
                      status === "temporary" ||
                      isLoading;

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
                        position="top"
                      >
                        <button
                          className={seatClass}
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
            );
          })}
        </div>
      </div>

      <div className={styles.legend}>
        <h4 className={styles.legendTitle}>Seat types:</h4>
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
