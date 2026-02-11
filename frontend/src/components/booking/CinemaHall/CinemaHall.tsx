import React from 'react';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import styles from './CinemaHall.module.css';
import type { SeatInfo } from '@/types/seatReservation';

interface CinemaHallProps {
    seats: SeatInfo[];
    selectedSeats: number[];
    onSeatClick: (seatId: number) => void;
}

export const CinemaHall: React.FC<CinemaHallProps> = ({
    seats,
    selectedSeats,
    onSeatClick
}) => {
    const rows = [...new Set(seats.map(seat => seat.row))].sort((a, b) => a - b);

    const getSeatStatus = (seat: SeatInfo) => {
        if (!seat.active) return 'unavailable';
        if (!seat.available) return 'booked';
        if (selectedSeats.includes(seat.id)) return 'selected';
        if (seat.temporarilyReserved) return 'temporary';
        return 'available';
    };

    const getSeatClass = (seat: SeatInfo) => {
        const status = getSeatStatus(seat);
        const seatType = seat.seatType.toLowerCase();

        return `${styles.seat} ${styles[status]} ${styles[seatType]}`;
    };

    const getSeatTitle = (seat: SeatInfo) => {
        const status = getSeatStatus(seat);
        const seatType = seat.seatType === 'VIP' ? 'VIP' : seat.seatType === 'COUPLE' ? 'Couple' : 'Standard';

        let title = `Row ${seat.row}, Seat ${seat.seatNumber} (${seatType})`;

        if (status === 'booked') {
            title += ' - Booked';
        } else if (status === 'unavailable') {
            title += ' - Unavailable';
        } else if (status === 'temporary') {
            title += ' - Temporarily reserved';
        } else if (status === 'selected') {
            title += ' - Selected';
        } else {
            title += ' - Available';
        }

        return title;
    };

    const isSeatDisabled = (seat: SeatInfo) => {
        return !seat.active || !seat.available || seat.temporarilyReserved;
    };

    return (
        <div className={styles.cinemaHall}>
            <div className={styles.screenArea}>
                <div className={styles.screen}>SCREEN</div>
            </div>

            <div className={styles.seatsContainer}>
                <div className={styles.scrollableArea}>
                    {rows.map(rowNumber => {
                        const rowSeats = seats.filter(seat => seat.row === rowNumber)
                            .sort((a, b) => a.seatNumber - b.seatNumber);

                        return (
                            <div key={`row-${rowNumber}`} className={styles.row}>
                                <div className={styles.seats}>
                                    {rowSeats.map(seat => (
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
                                                <span className={styles.seatNumber}>{seat.seatNumber}</span>
                                            </button>
                                        </Tooltip>
                                    ))}
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>

            <div className={styles.legend}>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.available}`}></div>
                    <span>Available</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.selected}`}></div>
                    <span>Selected</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.booked}`}></div>
                    <span>Booked</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.vip}`}></div>
                    <span>VIP</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.couple}`}></div>
                    <span>Couple</span>
                </div>
                <div className={styles.legendItem}>
                    <div className={`${styles.legendColor} ${styles.unavailable}`}></div>
                    <span>Unavailable</span>
                </div>
            </div>
        </div>
    );
};