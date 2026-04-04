import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ProgressStepper } from '@/components/booking/ProgressStepper/ProgressStepper';
import { Layout } from '@/components/layout/Layout/Layout';
import { Notification } from '@/components/ui/Notification/Notification';
import { Modal } from '@/components/ui/Modal/Modal';
import { bookingApi } from '@/api/bookingApi';
import type { BookingResponse } from '@/types/booking';
import type { NotificationType } from '@/components/ui/Notification/Notification';
import styles from './BookingSummaryPage.module.css';

interface NotificationState {
    id: string;
    message: string;
    type: NotificationType;
    isVisible: boolean;
}

interface BookingStateData {
    id: number;
    bookingNumber: string;
    movieTitle: string;
    hallName: string;
    sessionTime: string;
    totalPrice: string;
    finalPrice: string;
    bonusPointsUsed: number;
    bookedSeats: Array<{
        seatNumber: string;
        seatRow: number;
        ticketType: string;
        seatPrice: string;
    }>;
}

const BOOKING_STEPS = [
    {
        id: 1,
        title: 'Select Seats',
        description: 'Choose your seats',
        isClickable: true
    },
    {
        id: 2,
        title: 'Booking Summary',
        description: 'Review your booking',
        isClickable: true
    },
    {
        id: 3,
        title: 'Payment',
        description: 'Secure payment',
        isClickable: false
    },
    {
        id: 4,
        title: 'Confirmation',
        description: 'Booking confirmed',
        isClickable: false
    }
];

export const BookingSummaryPage: React.FC = () => {
    const { bookingId } = useParams<{ bookingId: string }>();
    const navigate = useNavigate();
    const [booking, setBooking] = useState<BookingResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [notifications, setNotifications] = useState<NotificationState[]>([]);
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [isCancelling, setIsCancelling] = useState(false);

    useEffect(() => {
        const fetchBooking = async () => {
            if (!bookingId) return;

            try {
                setLoading(true);
                const response = await bookingApi.getById(parseInt(bookingId));
                if (response?.data) {
                    setBooking(response.data);
                }
            } catch (err) {
                const errorMessage = err instanceof Error ? err.message : 'Failed to load booking';
                setError(errorMessage);
                showNotification(errorMessage, 'error');
            } finally {
                setLoading(false);
            }
        };

        fetchBooking();
    }, [bookingId]);

    const handleCancelBooking = async () => {
        if (!bookingId) return;

        setIsCancelling(true);
        try {
            await bookingApi.cancel(parseInt(bookingId));
            showNotification('Booking cancelled successfully', 'success');
            setShowCancelModal(false);
            setTimeout(() => navigate('/'), 2000);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to cancel booking';
            showNotification(errorMessage, 'error');
        } finally {
            setIsCancelling(false);
        }
    };

    const openCancelModal = () => {
        setShowCancelModal(true);
    };

    const closeCancelModal = () => {
        setShowCancelModal(false);
    };

    const showNotification = (message: string, type: NotificationType = 'info') => {
        const id = Date.now().toString();
        setNotifications(prev => [...prev, { id, message, type, isVisible: true }]);
    };

    const closeNotification = (id: string) => {
        setNotifications(prev => prev.map(notification =>
            notification.id === id ? { ...notification, isVisible: false } : notification
        ));
        setTimeout(() => {
            setNotifications(prev => prev.filter(notification => notification.id !== id));
        }, 300);
    };

    const handleStepClick = (step: any) => {
        if (step.id === 1 && booking?.sessionId) {
            navigate(`/booking/${booking.sessionId}`);
        }
        if (step.id === 2) {
            return;
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loading}>Loading booking summary...</div>
            </Layout>
        );
    }

    if (error) {
        return (
            <Layout>
                <div className={styles.error}>Error: {error}</div>
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

    const formatDateEnglish = (date: Date): string => {
        const options: Intl.DateTimeFormatOptions = {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        };
        return date.toLocaleDateString('en-US', options);
    };

    return (
        <Layout>
            <Modal
                isOpen={showCancelModal}
                onClose={closeCancelModal}
                title="Cancel Booking"
                size="small"
            >
                <div className={styles.modalContent}>
                    <p>Are you sure you want to cancel this booking?</p>
                    <p className={styles.modalWarning}>This action cannot be undone.</p>

                    <div className={styles.modalActions}>
                        <button
                            className={styles.modalCancelButton}
                            onClick={closeCancelModal}
                            disabled={isCancelling}
                        >
                            No, Keep Booking
                        </button>
                        <button
                            className={styles.modalConfirmButton}
                            onClick={handleCancelBooking}
                            disabled={isCancelling}
                        >
                            {isCancelling ? 'Cancelling...' : 'Yes, Cancel Booking'}
                        </button>
                    </div>
                </div>
            </Modal>

            <div className={styles.summaryPage}>
                {notifications.map((notification) => (
                    <Notification
                        key={notification.id}
                        id={notification.id}
                        message={notification.message}
                        type={notification.type}
                        isVisible={notification.isVisible}
                        onClose={closeNotification}
                    />
                ))}

                <ProgressStepper
                    steps={BOOKING_STEPS}
                    currentStep={2}
                    className={styles.stepper}
                    onStepClick={handleStepClick}
                />

                <div className={styles.header}>
                    <h1>Booking Summary</h1>
                    <p className={styles.bookingNumber}>Booking #: {booking.bookingNumber}</p>
                </div>

                <div className={styles.content}>
                    <div className={styles.movieInfo}>
                        <h2>{booking.movieTitle}</h2>
                        <div className={styles.sessionDetails}>
                            <div className={styles.detailItem}>
                                <span className={styles.detailLabel}>Hall:</span>
                                <span className={styles.detailValue}>{booking.hallName}</span>
                            </div>
                            <div className={styles.detailItem}>
                                <span className={styles.detailLabel}>Date:</span>
                                <span className={styles.detailValue}>
                                    {formatDateEnglish(sessionDate)}
                                </span>
                            </div>
                            <div className={styles.detailItem}>
                                <span className={styles.detailLabel}>Time:</span>
                                <span className={styles.detailValue}>
                                    {sessionDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.seatsInfo}>
                        <h3>Selected Seats</h3>
                        <div className={styles.seatsList}>
                            {booking.seatReservations.map((seat) => (
                                <div key={seat.id} className={styles.seatItem}>
                                    <div className={styles.seatInfo}>
                                        <span className={styles.seatLocation}>
                                            Row {seat.row}, Seat {seat.seatNumber}
                                        </span>
                                        <span className={styles.ticketType}>{seat.ticketTypeName}</span>
                                    </div>
                                    <span className={styles.seatPrice}>₴{parseFloat(seat.seatPrice).toFixed(2)}</span>
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
                                        <span>Bonus points used:</span>
                                        <span>{booking.bonusPointsUsed} points</span>
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
                            <span className={styles.infoLabel}>Booking status:</span>
                            <span className={`${styles.status} ${styles[booking.status.toLowerCase()]}`}>
                                {booking.status}
                            </span>
                        </div>
                        <div className={styles.infoItem}>
                            <span className={styles.infoLabel}>Expires at:</span>
                            <span className={styles.expiryTime}>
                                {expiresAt.toLocaleString('en-US')}
                            </span>
                        </div>
                        <div className={styles.infoItem}>
                            <span className={styles.infoLabel}>Time left:</span>
                            <span className={`${styles.timeLeft} ${timeLeft < 10 ? styles.warning : ''}`}>
                                {hoursLeft > 0 ? `${hoursLeft}h ` : ''}{minutesLeft}m
                            </span>
                        </div>
                    </div>

                    <div className={styles.actions}>
                        <div className={styles.actionButtons}>
                            <button
                                className={styles.cancelButton}
                                onClick={openCancelModal}
                                disabled={booking.status !== 'PENDING'}
                            >
                                Cancel Booking
                            </button>
                            <button
                                className={styles.payButton}
                                onClick={() => {
                                    const bookingState: BookingStateData = {
                                        id: booking.id,
                                        bookingNumber: booking.bookingNumber,
                                        movieTitle: booking.movieTitle,
                                        hallName: booking.hallName,
                                        sessionTime: booking.sessionTime,
                                        totalPrice: booking.totalPrice,
                                        finalPrice: booking.finalPrice,
                                        bonusPointsUsed: booking.bonusPointsUsed,
                                        bookedSeats: booking.seatReservations.map(seat => ({
                                            seatNumber: seat.seatNumber.toString(),
                                            seatRow: seat.row,
                                            ticketType: seat.ticketTypeName,
                                            seatPrice: seat.seatPrice
                                        }))
                                    };
                                    navigate(`/booking/payment/${booking.id}`, {
                                        state: { booking: bookingState }
                                    });
                                }}
                                disabled={booking.status !== 'PENDING'}
                            >
                                💳 Proceed to Payment
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};