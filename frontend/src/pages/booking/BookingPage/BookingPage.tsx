import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useSeatAvailability } from '@/hooks/features/seatAvailability/useSeatAvailability';
import { useSeatSelection } from '@/hooks/features/seatAvailability/useSeatSelection';
import { CinemaHall } from '@/components/booking/CinemaHall';
import { BookingSidebar } from '@/components/booking/BookingSidebar';
import { Layout } from '@/components/layout/Layout';
import { bookingApi } from '@/api/bookingApi';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import type { BookingCreateRequest, SeatSelectionRequest } from '@/types/booking';
import styles from './BookingPage.module.css';

export const BookingPage: React.FC = () => {
    const { sessionId } = useParams<{ sessionId: string }>();
    const navigate = useNavigate();
    const sessionIdNum = parseInt(sessionId || '0');
    const [selectionError, setSelectionError] = useState<string | null>(null);
    const [isBooking, setIsBooking] = useState(false);

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

    useEffect(() => {
        if (sessionIdNum && !seatData && !loading) {
            fetchSeatAvailability();
        }
    }, [sessionIdNum, fetchSeatAvailability, loading, seatData]);

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
            console.error('Failed to select seat:', error);
        }
    };

    const handleBooking = async () => {
        if (selectedSeats.length === 0) {
            setSelectionError('Please select at least one seat');
            return;
        }

        if (!sessionIdNum) {
            setSelectionError('Invalid session');
            return;
        }

        try {
            setIsBooking(true);
            setSelectionError(null);

            // Перевірка чи є токен
            const token = localStorage.getItem('authToken');
            if (!token) {
                setSelectionError('You need to be logged in to book tickets');
                throw new Error('No auth token found');
            }

            console.log('Auth token exists:', token.substring(0, 20) + '...');

            const seats: SeatSelectionRequest[] = selectedSeats.map(seat => ({
                seatId: seat.seat.id,
                ticketTypeId: seat.ticketTypeId || 1
            }));

            const bookingRequest: BookingCreateRequest = {
                sessionId: sessionIdNum,
                seats: seats
            };

            console.log('Creating booking request:', bookingRequest);

            const bookingResponse = await bookingApi.create(bookingRequest);

            console.log('Booking created successfully:', bookingResponse);
            navigate(`/booking/summary/${bookingResponse.id}`);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : 'Failed to create booking';
            setSelectionError(`Booking failed: ${errorMessage}. Please check if you're logged in.`);
            console.error('Booking failed details:', error);
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
                <div className={styles.header}>
                    <h1>{seatData.movieTitle}</h1>
                    <div className={styles.sessionInfo}>
                        <span>Hall: {seatData.hallName}</span>
                        <span>Available seats: {seatData.availableSeats}</span>
                        <span>Price from: ₴{parseFloat(seatData.basePrice).toFixed(2)}</span>
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
                        />
                    </div>
                </div>
            </div>
        </Layout>
    );
};