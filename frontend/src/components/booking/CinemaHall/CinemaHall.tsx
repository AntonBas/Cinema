import React from 'react';
import styles from './CinemaHall.module.css';
import type { SeatInfo } from '@/types/seatAvailability';

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
        if (!seat.available) return 'occupied';
        if (selectedSeats.includes(seat.id)) return 'selected';
        if (seat.temporarilyReserved) return 'temporary';
        return 'available';
    };

    const getSeatClass = (seat: SeatInfo) => {
        const status = getSeatStatus(seat);
        const seatType = seat.seatType.toLowerCase();

        return `${styles.seat} ${styles[status]} ${styles[seatType]}`;
    };

    return (
        <div className={styles.cinemaHall}>
            <div className={styles.screenArea}>
                <div className={styles.screen}>SCREEN</div>
            </div>

            <div className={styles.seatsContainer}>
                {rows.map(rowNumber => {
                    const rowSeats = seats.filter(seat => seat.row === rowNumber)
                        .sort((a, b) => a.seatNumber - b.seatNumber);

                    return (
                        <div key={`row-${rowNumber}`} className={styles.row}>
                            <div className={styles.rowLabel}>Row {rowNumber}</div>
                            <div className={styles.seats}>
                                {rowSeats.map(seat => (
                                    <button
                                        key={`seat-${seat.id}`}
                                        className={getSeatClass(seat)}
                                        onClick={() => onSeatClick(seat.id)}
                                        disabled={!seat.available || seat.temporarilyReserved}
                                        title={`Row ${seat.row}, Seat ${seat.seatNumber}`}
                                    >
                                        <span className={styles.seatNumber}>{seat.seatNumber}</span>
                                    </button>
                                ))}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};