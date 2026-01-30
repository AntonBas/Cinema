import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSeatAvailability } from '@/hooks/features/seatAvailability/useSeatAvailability';
import { useSeatSelection } from '@/hooks/features/seatAvailability/useSeatSelection';
import { useBonus } from '@/hooks/features/bonus/useBonus';
import { CinemaHall } from '@/components/booking/CinemaHall';
import { BookingSidebar } from '@/components/booking/BookingSidebar';
import { Layout } from '@/components/layout/Layout';
import { Notification } from '@/components/ui/Notification/Notification';
import { bookingApi } from '@/api/bookingApi';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import type { BookingCreateRequest, SeatSelectionRequest } from '@/types/booking';
import type { NotificationType } from '@/components/ui/Notification/Notification';
import styles from './BookingPage.module.css';

interface NotificationState {
    id: string;
    message: string;
    type: NotificationType;
    isVisible: boolean;
}

export const BookingPage: React.FC = () => {
    const { sessionId } = useParams<{ sessionId: string }>();
    const navigate = useNavigate();
    const sessionIdNum = parseInt(sessionId || '0');
    const [selectionError, setSelectionError] = useState<string | null>(null);
    const [isBooking, setIsBooking] = useState(false);
    const [notifications, setNotifications] = useState<NotificationState[]>([]);

    const {
        seatData,
        loading,
        error,
        fetchSeatAvailability,
        checkSpecificSeat
    } = useSeatAvailability(sessionIdNum);

    const {
        selectedSeats,
        totalPrice,
        selectSeat,
        deselectSeat,
        isSeatSelected,
        updateSeatTicketType
    } = useSeatSelection({
        seatData,
        checkSpecificSeat,
        maxSeats: 10
    });

    const { getMyBalance } = useBonus();
    const [bonusBalance, setBonusBalance] = useState<number>(0);
    const [maxUsablePoints, setMaxUsablePoints] = useState<number>(0);
    const [minUsablePoints, setMinUsablePoints] = useState<number>(0);

    useEffect(() => {
        if (sessionIdNum && !seatData && !loading) {
            fetchSeatAvailability();
        }
    }, [sessionIdNum, fetchSeatAvailability, loading, seatData]);

    useEffect(() => {
        const loadBonusData = async () => {
            try {
                const token = localStorage.getItem('authToken');
                if (!token) return;

                const balanceData = await getMyBalance();
                setBonusBalance(balanceData.pointsBalance);
                setMinUsablePoints(balanceData.minUsablePoints);

                const maxPoints = Math.min(
                    balanceData.pointsBalance,
                    balanceData.maxUsablePoints,
                    Math.floor(totalPrice / 0.01)
                );
                setMaxUsablePoints(maxPoints);
            } catch (error) {
                showNotification('Failed to load bonus data', 'error');
            }
        };

        if (sessionIdNum && seatData) {
            loadBonusData();
        }
    }, [sessionIdNum, seatData, totalPrice, getMyBalance]);

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

    const handleSeatClick = async (seatId: number) => {
        try {
            setSelectionError(null);
            if (isSeatSelected(seatId)) {
                deselectSeat(seatId);
            } else {
                await selectSeat(seatId);
            }
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to select seat';
            setSelectionError(errorMessage);
            showNotification(errorMessage, 'error');
        }
    };

    const handleBooking = async (bonusPointsToUse: number) => {
        if (selectedSeats.length === 0) {
            showNotification('Please select at least one seat', 'warning');
            return;
        }

        if (!sessionIdNum) {
            showNotification('Invalid session', 'error');
            return;
        }

        try {
            setIsBooking(true);
            setSelectionError(null);

            const token = localStorage.getItem('authToken');
            if (!token) {
                showNotification('You need to be logged in to book tickets', 'warning');
                return;
            }

            const seats: SeatSelectionRequest[] = selectedSeats.map(seat => ({
                seatId: seat.seat.id,
                ticketTypeId: seat.ticketTypeId || 1
            }));

            const bookingRequest: BookingCreateRequest = {
                sessionId: sessionIdNum,
                seats: seats,
                bonusPointsToUse: bonusPointsToUse > 0 ? bonusPointsToUse : undefined
            };

            const bookingResponse = await bookingApi.create(bookingRequest);

            showNotification('Booking created successfully!', 'success');
            setTimeout(() => {
                navigate(`/booking/summary/${bookingResponse.id}`);
            }, 1000);

        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to create booking';
            showNotification(`Booking failed: ${errorMessage}`, 'error');
        } finally {
            setIsBooking(false);
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loading}>Loading seats...</div>
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
                {notifications.map((notification, index) => (
                    <Notification
                        key={notification.id}
                        id={notification.id}
                        message={notification.message}
                        type={notification.type}
                        isVisible={notification.isVisible}
                        onClose={closeNotification}
                        position={index}
                    />
                ))}

                <div className={styles.header}>
                    <h1>{seatData.movieTitle}</h1>
                    <div className={styles.sessionInfo}>
                        <span>Hall: {seatData.hallName}</span>
                        <span>Available seats: {seatData.availableSeats}</span>
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
                            isBooking={isBooking}
                            bonusBalance={bonusBalance}
                            maxUsablePoints={maxUsablePoints}
                            minUsablePoints={minUsablePoints}
                        />
                    </div>
                </div>
            </div>
        </Layout>
    );
};