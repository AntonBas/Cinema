import React from 'react';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import { TicketTypeSelect } from '../TicketTypeSelect';
import styles from './BookingSidebar.module.css';

interface BookingSidebarProps {
    selectedSeats: SelectedSeat[];
    totalPrice: number;
    sessionId: number;
    onTicketTypeChange: (seatId: number, ticketTypeId: number) => void;
    onBooking: () => Promise<void>;
    isBooking: boolean;
}

export const BookingSidebar: React.FC<BookingSidebarProps> = ({
    selectedSeats,
    totalPrice,
    onTicketTypeChange,
    onBooking,
    isBooking
}) => {
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
                                    ticketPrices={selectedSeat.seat.ticketPrices || []}
                                    selectedTicketTypeId={selectedSeat.ticketTypeId}
                                    onSelect={onTicketTypeChange}
                                />
                            </div>
                        </div>
                        <div className={styles.seatPrice}>
                            ₴{selectedSeat.price.toFixed(2)}
                        </div>
                    </div>
                ))}
            </div>

            <div className={styles.summary}>
                <div className={styles.total}>
                    <span>Total:</span>
                    <span className={styles.totalPrice}>₴{totalPrice.toFixed(2)}</span>
                </div>

                <Tooltip
                    content="After booking, you will have 20 minutes to complete the payment. Seats will be temporarily reserved during this time."
                    position="top"
                >
                    <button
                        className={styles.bookButton}
                        onClick={onBooking}
                        disabled={isBooking || selectedSeats.length === 0}
                    >
                        {isBooking ? 'Processing...' : `Book Now - ₴${totalPrice.toFixed(2)}`}
                    </button>
                </Tooltip>
            </div>
        </div>
    );
};