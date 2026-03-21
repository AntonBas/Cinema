import React from 'react';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import styles from './CinemaHall.module.css';
import type { SeatInfo } from '@/types/seatReservation';

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
    onSeatClick
}) => {
    const rows = [...new Set(seats.map(seat => seat.row))].sort((a, b) => a - b);

    const getSeatStatus = (seat: SeatInfo): string => {
        if (!seat.active) return 'inactive';
        if (selectedSeats.includes(seat.id)) return 'selected';
        if (seat.temporarilyReserved) return 'temporary';
        if (!seat.available) return 'booked';
        return 'available';
    };

    const getSeatClass = (seat: SeatInfo): string => {
        const status = getSeatStatus(seat);
        const seatType = seat.seatType.toLowerCase();

        let className = `${styles.seatButton} ${styles[seatType]}`;

        if (status === 'inactive') {
            className += ` ${styles.inactive}`;
        } else if (status === 'selected') {
            className += ` ${styles.selected}`;
        } else if (status === 'temporary') {
            className += ` ${styles.temporary}`;
        } else if (status === 'booked') {
            className += ` ${styles.booked}`;
        }

        return className;
    };

    const getSeatTitle = (seat: SeatInfo): string => {
        const status = getSeatStatus(seat);
        const seatType = seat.seatType === 'VIP' ? 'VIP' : seat.seatType === 'COUPLE' ? 'Couple' : 'Standard';

        let title = `Row ${seat.row}, Seat ${seat.seatNumber} (${seatType})`;

        if (status === 'booked') {
            title += ' - Booked';
        } else if (status === 'inactive') {
            title += ' - Unavailable';
        } else if (status === 'temporary') {
            title += ' - Temporarily reserved (5 min)';
        } else if (status === 'selected') {
            title += ' - Selected';
        } else {
            title += ' - Available';
        }

        return title;
    };

    const isSeatDisabled = (seat: SeatInfo): boolean => {
        const status = getSeatStatus(seat);
        return status === 'inactive' || status === 'booked' || status === 'temporary' || loadingSeats.includes(seat.id);
    };

    return (
        <div className={styles.cinemaHall}>
            <div className={styles.screenArea}>
                <div className={styles.screen}>SCREEN</div>
                <div className={styles.screenReflection} />
            </div>

            <div className={styles.seatsLayout}>
                <div className={styles.rowsContainer}>
                    {rows.map(rowNumber => {
                        const rowSeats = seats.filter(seat => seat.row === rowNumber)
                            .sort((a, b) => a.seatNumber - b.seatNumber);

                        return (
                            <div key={`row-${rowNumber}`} className={styles.row}>
                                <div className={styles.rowLabel}>Row {rowNumber}</div>
                                <div className={styles.seatsRow}>
                                    {rowSeats.map(seat => {
                                        const isLoading = loadingSeats.includes(seat.id);

                                        return (
                                            <Tooltip
                                                key={`seat-${seat.id}`}
                                                content={getSeatTitle(seat)}
                                                position="top"
                                            >
                                                <button
                                                    className={getSeatClass(seat)}
                                                    onClick={() => onSeatClick(seat.id)}
                                                    disabled={isSeatDisabled(seat)}
                                                >
                                                    {isLoading ? (
                                                        <span className={styles.loadingSpinner} />
                                                    ) : (
                                                        <span className={styles.seatNumber}>{seat.seatNumber}</span>
                                                    )}
                                                    {!seat.active && (
                                                        <div className={styles.inactiveOverlay}>
                                                            <span className={styles.inactiveIcon}>✕</span>
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
                <h4 className={styles.legendTitle}>Seat Types:</h4>
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
                    <div className={styles.legendItem}>
                        <div className={`${styles.legendColor} ${styles.selected}`} />
                        <span>Selected</span>
                    </div>
                    <div className={styles.legendItem}>
                        <div className={`${styles.legendColor} ${styles.booked}`} />
                        <span>Booked</span>
                    </div>
                    <div className={styles.legendItem}>
                        <div className={`${styles.legendColor} ${styles.temporary}`} />
                        <span>Temporarily Reserved</span>
                    </div>
                    <div className={styles.legendItem}>
                        <div className={`${styles.legendColor} ${styles.inactive}`} />
                        <span>Inactive</span>
                    </div>
                </div>
            </div>
        </div>
    );
};