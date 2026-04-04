import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSeatReservation } from '@/hooks/features/seatReservation/useSeatReservation';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import { useBooking } from '@/hooks/features/booking/useBooking';
import { CinemaHall } from '@/components/booking/CinemaHall/CinemaHall';
import { BookingSidebar } from '@/components/booking/BookingSidebar/BookingSidebar';
import { ProgressStepper } from '@/components/booking/ProgressStepper/ProgressStepper';
import { Layout } from '@/components/layout/Layout/Layout';
import { Notification } from '@/components/ui/Notification/Notification';
import type { SelectedSeat } from '@/hooks/features/seatReservation/useSeatReservation';
import type { SeatInfo } from '@/types/seatReservation';
import type { NotificationType } from '@/components/ui/Notification/Notification';
import styles from './BookingPage.module.css';

interface NotificationState {
    id: string;
    message: string;
    type: NotificationType;
    isVisible: boolean;
}

const BOOKING_STEPS = [
    { id: 1, title: 'Select Seats', description: 'Choose your seats', isClickable: true },
    { id: 2, title: 'Booking Summary', description: 'Review your booking', isClickable: false },
    { id: 3, title: 'Payment', description: 'Secure payment', isClickable: false },
    { id: 4, title: 'Confirmation', description: 'Booking confirmed', isClickable: false }
];

export const BookingPage: React.FC = () => {
    const { sessionId } = useParams<{ sessionId: string }>();
    const navigate = useNavigate();
    const sessionIdNum = parseInt(sessionId || '0');
    const [selectionError, setSelectionError] = useState<string | null>(null);
    const [notifications, setNotifications] = useState<NotificationState[]>([]);

    const {
        data: seatData,
        availableSeatsCount,
        selectedSeats,
        totalPrice,
        loading,
        getSeatAvailability,
        selectSeat: hookSelectSeat,
        deselectSeat,
        isSeatSelected,
        updateSeatTicketType,
        hasData,
        totalSelected,
        loadingSeats
    } = useSeatReservation(sessionIdNum);

    const { myBalance, getMyBalance } = useBonus();
    const { create, loading: bookingLoading } = useBooking();

    const loadBonusDataRef = React.useRef(getMyBalance);

    useEffect(() => {
        loadBonusDataRef.current = getMyBalance;
    }, [getMyBalance]);

    useEffect(() => {
        if (sessionIdNum && !hasData && !loading) {
            getSeatAvailability();
        }
    }, [sessionIdNum, hasData, loading, getSeatAvailability]);

    useEffect(() => {
        if (sessionIdNum && seatData) {
            const loadBonusData = async () => {
                try {
                    const token = localStorage.getItem('authToken');
                    if (!token) return;
                    await loadBonusDataRef.current();
                } catch (error) {
                    showNotification('Failed to load bonus data', 'error');
                }
            };
            loadBonusData();
        }
    }, [sessionIdNum, seatData]);

    const showNotification = useCallback((message: string, type: NotificationType = 'info') => {
        const id = Date.now().toString();
        setNotifications(prev => [...prev, { id, message, type, isVisible: true }]);
    }, []);

    const closeNotification = useCallback((id: string) => {
        setNotifications(prev => prev.map(notification =>
            notification.id === id ? { ...notification, isVisible: false } : notification
        ));
        setTimeout(() => {
            setNotifications(prev => prev.filter(notification => notification.id !== id));
        }, 300);
    }, []);

    const handleSeatClick = async (seatId: number) => {
        try {
            setSelectionError(null);
            const seat = seatData?.seats.find((s: SeatInfo) => s.id === seatId);
            if (!seat) throw new Error('Seat not found');

            if (isSeatSelected(seatId)) {
                await deselectSeat(seatId);
            } else {
                await hookSelectSeat(seat);
            }
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to select seat';
            setSelectionError(errorMessage);
            showNotification(errorMessage, 'error');
        }
    };

    const handleBooking = async (bonusPointsToUse: number) => {
        if (totalSelected === 0) {
            showNotification('Please select at least one seat', 'warning');
            return;
        }

        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                showNotification('You need to be logged in to book tickets', 'warning');
                return;
            }

            const seats = selectedSeats.map(seat => ({
                seatId: seat.seat.id,
                ticketTypeId: seat.ticketTypeId
            }));

            const response = await create({
                sessionId: sessionIdNum,
                seats,
                bonusPointsToUse: bonusPointsToUse > 0 ? bonusPointsToUse : undefined
            });

            if (response) {
                showNotification('Booking created successfully!', 'success');
                await getSeatAvailability();
                setTimeout(() => navigate(`/booking/summary/${response.id}`), 1000);
            }
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to create booking';
            showNotification(`Booking failed: ${errorMessage}`, 'error');
        }
    };

    const handleStepClick = (step: any) => {
        if (step.id === 1) {
            return;
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loading}>Loading seats...</div>
            </Layout>
        );
    }

    if (!seatData) {
        return (
            <Layout>
                <div className={styles.error}>No seat data available</div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.bookingPage}>
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
                    currentStep={1}
                    className={styles.stepper}
                    onStepClick={handleStepClick}
                />

                <div className={styles.header}>
                    <h1>{seatData.movieTitle}</h1>
                    <div className={styles.sessionInfo}>
                        <span>Hall: {seatData.hallName}</span>
                        <span>Available seats: {availableSeatsCount || seatData.availableSeats}</span>
                        <span>Price from: {parseFloat(seatData.basePrice).toFixed(2)}₴</span>
                    </div>
                </div>

                {selectionError && (
                    <div className={styles.selectionError}>
                        Error: {selectionError}
                    </div>
                )}

                <div className={styles.content}>
                    <div className={styles.hallSection}>
                        <CinemaHall
                            seats={seatData.seats}
                            selectedSeats={selectedSeats.map((s: SelectedSeat) => s.seat.id)}
                            loadingSeats={loadingSeats}
                            onSeatClick={handleSeatClick}
                        />
                    </div>

                    <div className={styles.sidebarSection}>
                        <BookingSidebar
                            selectedSeats={selectedSeats}
                            totalPrice={totalPrice}
                            sessionId={sessionIdNum}
                            onTicketTypeChange={updateSeatTicketType}
                            onBooking={handleBooking}
                            isBooking={bookingLoading}
                            maxUsablePoints={myBalance?.maxUsablePoints || 0}
                            minUsablePoints={myBalance?.minUsablePoints || 0}
                        />
                    </div>
                </div>
            </div>
        </Layout>
    );
};