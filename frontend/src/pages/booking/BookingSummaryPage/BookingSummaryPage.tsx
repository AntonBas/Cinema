import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ProgressStepper } from '@/components/booking/ProgressStepper/ProgressStepper';
import { BOOKING_STEPS } from '@/components/booking/ProgressStepper/bookingSteps';
import { Layout } from '@/components/layout/Layout/Layout';
import { ConfirmModal } from '@/components/ui/ConfirmModal/ConfirmModal';
import { useBooking } from '@/hooks/features/booking/useBooking';
import styles from './BookingSummaryPage.module.css';

export const BookingSummaryPage: React.FC = () => {
    const { bookingId } = useParams<{ bookingId: string }>();
    const navigate = useNavigate();
    const [showCancelModal, setShowCancelModal] = useState(false);

    const { booking, loading, getById, cancel } = useBooking();

    useEffect(() => {
        if (bookingId) {
            getById(parseInt(bookingId));
        }
    }, [bookingId, getById]);

    const handleCancelBooking = async () => {
        if (!bookingId) return;

        await cancel(parseInt(bookingId));
        setShowCancelModal(false);
        setTimeout(() => navigate('/'), 2000);
    };

    const handleProceedToPayment = () => {
        if (!booking) return;

        const bookingData = {
            id: booking.id,
            bookingNumber: booking.bookingNumber,
            movieTitle: booking.movieTitle,
            hallName: booking.hallName,
            sessionTime: booking.sessionTime,
            totalPrice: booking.totalPrice,
            finalPrice: booking.finalPrice,
            bonusPointsUsed: booking.bonusPointsUsed,
            bookedSeats: booking.seatReservations.map(seat => ({
                seatNumber: String(seat.seatNumber),
                seatRow: seat.row,
                ticketType: seat.ticketTypeName,
                seatPrice: seat.seatPrice
            }))
        };

        navigate(`/booking/payment/${booking.id}`, {
            state: { booking: bookingData }
        });
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loading}>Loading booking summary...</div>
            </Layout>
        );
    }

    if (!booking) {
        return (
            <Layout>
                <div className={styles.error}>Booking not found</div>
            </Layout>
        );
    }

    const sessionDate = new Date(booking.sessionTime);
    const expiresAt = new Date(booking.expiresAt);
    const timeLeft = Math.max(0, Math.floor((expiresAt.getTime() - Date.now()) / (1000 * 60)));
    const hoursLeft = Math.floor(timeLeft / 60);
    const minutesLeft = timeLeft % 60;

    return (
        <Layout>
            <ConfirmModal
                isOpen={showCancelModal}
                onConfirm={handleCancelBooking}
                onCancel={() => setShowCancelModal(false)}
                title="Cancel Booking"
                message="Are you sure you want to cancel this booking? This action cannot be undone."
                confirmText="Yes, Cancel Booking"
                cancelText="No, Keep Booking"
                variant="error"
            />

            <div className={styles.summaryPage}>
                <ProgressStepper steps={BOOKING_STEPS} currentStep={2} className={styles.stepper} />

                <div className={styles.header}>
                    <h1>Booking Summary</h1>
                    <p className={styles.bookingNumber}>Booking #: {booking.bookingNumber}</p>
                </div>

                <div className={styles.content}>
                    <div className={styles.movieInfo}>
                        <h2>{booking.movieTitle}</h2>
                        <div className={styles.sessionDetails}>
                            <div className={styles.detailItem}>
                                <span>Hall:</span>
                                <span>{booking.hallName}</span>
                            </div>
                            <div className={styles.detailItem}>
                                <span>Date:</span>
                                <span>{sessionDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
                            </div>
                            <div className={styles.detailItem}>
                                <span>Time:</span>
                                <span>{sessionDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.seatsInfo}>
                        <h3>Selected Seats</h3>
                        <div className={styles.seatsList}>
                            {booking.seatReservations.map(seat => (
                                <div key={seat.id} className={styles.seatItem}>
                                    <div className={styles.seatInfo}>
                                        <span>Row {seat.row}, Seat {seat.seatNumber}</span>
                                        <span className={styles.ticketType}>{seat.ticketTypeName}</span>
                                    </div>
                                    <span>₴{parseFloat(seat.seatPrice).toFixed(2)}</span>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className={styles.pricingInfo}>
                        <h3>Payment Details</h3>
                        <div className={styles.priceBreakdown}>
                            {booking.bonusPointsUsed > 0 && (
                                <>
                                    <div className={styles.priceRow}>
                                        <span>Total Price:</span>
                                        <span>{parseFloat(booking.totalPrice).toFixed(2)}₴</span>
                                    </div>
                                    <div className={styles.priceRow}>
                                        <span>Bonus discount:</span>
                                        <span className={styles.discount}>-{parseFloat(booking.bonusDiscountAmount).toFixed(2)}₴</span>
                                    </div>
                                </>
                            )}
                            <div className={styles.finalPriceRow}>
                                <span>Amount to pay:</span>
                                <span className={styles.finalPrice}>{parseFloat(booking.finalPrice).toFixed(2)}₴</span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.bookingInfo}>
                        <div className={styles.infoItem}>
                            <span>Booking status:</span>
                            <span className={`${styles.status} ${styles[booking.status.toLowerCase()]}`}>
                                {booking.status}
                            </span>
                        </div>
                        <div className={styles.infoItem}>
                            <span>Time left:</span>
                            <span className={`${styles.timeLeft} ${timeLeft < 10 ? styles.warning : ''}`}>
                                {hoursLeft > 0 ? `${hoursLeft}h ` : ''}{minutesLeft}m
                            </span>
                        </div>
                    </div>

                    <div className={styles.actions}>
                        <button
                            className={styles.cancelButton}
                            onClick={() => setShowCancelModal(true)}
                            disabled={booking.status !== 'PENDING'}
                        >
                            Cancel Booking
                        </button>
                        <button
                            className={styles.payButton}
                            onClick={handleProceedToPayment}
                            disabled={booking.status !== 'PENDING'}
                        >
                            💳 Proceed to Payment
                        </button>
                    </div>
                </div>
            </div>
        </Layout>
    );
};