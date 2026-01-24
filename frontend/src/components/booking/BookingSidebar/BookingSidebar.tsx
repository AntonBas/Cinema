import React from 'react';
import { useSeatReservation } from '@/hooks/features/seatAvailability/useSeatReservation';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import { TicketTypeSelect } from '../TicketTypeSelect';
import styles from './BookingSidebar.module.css';

interface BookingSidebarProps {
    selectedSeats: SelectedSeat[];
    totalPrice: number;
    sessionId: number;
    onTicketTypeChange: (seatId: number, ticketTypeId: number) => void;
}

export const BookingSidebar: React.FC<BookingSidebarProps> = ({
    selectedSeats,
    totalPrice,
    sessionId,
    onTicketTypeChange
}) => {
    const { createTemporaryReservation, reserving, error } = useSeatReservation();

    const handleReserve = async () => {
        if (selectedSeats.length === 0) return;

        const reservationData = {
            sessionId,
            seatIds: selectedSeats.map(seat => seat.seat.id),
            ticketTypeIds: selectedSeats.reduce((acc, selected) => {
                if (selected.ticketTypeId) {
                    acc[selected.seat.id] = selected.ticketTypeId;
                }
                return acc;
            }, {} as Record<number, number>)
        };

        try {
            await createTemporaryReservation(reservationData);
        } catch (error) {
            console.error('Reservation failed:', error);
        }
    };

    if (selectedSeats.length === 0) {
        return (
            <div className={styles.sidebar}>
                <div className={styles.empty}>
                    <div className={styles.emptyIcon}>🎬</div>
                    <h3>No seats selected</h3>
                    <p>Click on available seats to select them</p>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.sidebar}>
            <div className={styles.header}>
                <h3>Selected Seats</h3>
                <span className={styles.seatCount}>{selectedSeats.length} seats</span>
            </div>

            <div className={styles.seatsList}>
                {selectedSeats.map((selectedSeat) => (
                    <div key={selectedSeat.seat.id} className={styles.seatItem}>
                        <div className={styles.seatInfo}>
                            <span className={styles.seatNumber}>
                                Row {selectedSeat.seat.row}, Seat {selectedSeat.seat.seatNumber}
                            </span>
                            <div className={styles.ticketTypeContainer}>
                                <TicketTypeSelect
                                    seatId={selectedSeat.seat.id}
                                    ticketPrices={selectedSeat.seat.ticketPrices}
                                    selectedTicketTypeId={selectedSeat.ticketTypeId}
                                    onSelect={onTicketTypeChange}
                                />
                            </div>
                        </div>
                        <div className={styles.seatPrice}>
                            ${selectedSeat.price.toFixed(2)}
                        </div>
                    </div>
                ))}
            </div>

            <div className={styles.summary}>
                <div className={styles.total}>
                    <span>Total:</span>
                    <span className={styles.totalPrice}>${totalPrice.toFixed(2)}</span>
                </div>

                <button
                    className={styles.reserveButton}
                    onClick={handleReserve}
                    disabled={reserving || selectedSeats.length === 0}
                >
                    {reserving ? 'Processing...' : `Reserve ${selectedSeats.length} seat(s)`}
                </button>

                {error && <div className={styles.error}>{error}</div>}
            </div>
        </div>
    );
};