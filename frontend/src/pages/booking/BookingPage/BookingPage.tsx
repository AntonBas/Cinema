import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useSeatAvailability } from '@/hooks/features/seatAvailability/useSeatAvailability';
import { useSeatSelection } from '@/hooks/features/seatAvailability/useSeatSelection';
import { CinemaHall } from '@/components/booking/CinemaHall';
import { BookingSidebar } from '@/components/booking/BookingSidebar';
import { Layout } from '@/components/layout/Layout';
import type { SelectedSeat } from '@/hooks/features/seatAvailability/useSeatSelection';
import styles from './BookingPage.module.css';

export const BookingPage: React.FC = () => {
    const { sessionId } = useParams<{ sessionId: string }>();
    const sessionIdNum = parseInt(sessionId || '0');
    const [selectionError, setSelectionError] = useState<string | null>(null);

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
                        />
                    </div>
                </div>
            </div>
        </Layout>
    );
};