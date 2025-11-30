import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, HallLayoutResponse, SeatResponse } from '@/types';
import { SeatType } from '@/types';
import { useCinemaHalls, useSeatMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { Modal } from '@/components/ui';
import styles from './HallLayoutModal.module.css';

interface HallLayoutModalProps {
    hall: CinemaHallResponse;
    isOpen: boolean;
    onClose: () => void;
}

export const HallLayoutModal: React.FC<HallLayoutModalProps> = ({
    hall,
    isOpen,
    onClose
}) => {
    const [updatingSeat, setUpdatingSeat] = useState<number | null>(null);
    const [localLayout, setLocalLayout] = useState<HallLayoutResponse | null>(null);
    const [hasLoaded, setHasLoaded] = useState(false);

    const { hallLayout, loading, error, getHallLayout } = useCinemaHalls();
    const { updateSeatType, loading: updatingSeatType } = useSeatMutation();
    const { showNotification } = useNotification();

    useEffect(() => {
        setLocalLayout(hallLayout);
    }, [hallLayout]);

    useEffect(() => {
        if (isOpen && hall.id && !hasLoaded) {
            getHallLayout(hall.id);
            setHasLoaded(true);
        }
    }, [hall.id, getHallLayout, isOpen, hasLoaded]);

    useEffect(() => {
        if (!isOpen) {
            setHasLoaded(false);
            setLocalLayout(null);
        }
    }, [isOpen]);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    const handleSeatClick = async (seat: SeatResponse) => {
        if (updatingSeat === seat.id || updatingSeatType) return;

        const nextSeatType = getNextSeatType(seat.seatType);

        try {
            setUpdatingSeat(seat.id);

            const updatedSeat = { ...seat, seatType: nextSeatType };

            setLocalLayout(prev => {
                if (!prev) return prev;
                return {
                    ...prev,
                    rows: prev.rows.map(row => ({
                        ...row,
                        seats: row.seats.map(s =>
                            s.id === seat.id ? updatedSeat : s
                        )
                    }))
                };
            });

            await updateSeatType(hall.id, seat.id, nextSeatType);

            showNotification(`Seat type updated to ${getSeatTypeName(nextSeatType)}`, 'success');
        } catch (err) {
            await getHallLayout(hall.id);
            showNotification('Failed to update seat type', 'error');
        } finally {
            setUpdatingSeat(null);
        }
    };

    const getNextSeatType = (currentType: SeatType): SeatType => {
        const types = Object.values(SeatType);
        const currentIndex = types.indexOf(currentType);
        const nextIndex = (currentIndex + 1) % types.length;
        return types[nextIndex];
    };

    const getSeatTypeName = (seatType: SeatType): string => {
        const names = {
            [SeatType.STANDARD]: 'Standard',
            [SeatType.VIP]: 'VIP',
            [SeatType.DISABLED]: 'Disabled',
            [SeatType.COUPLE]: 'Couple'
        };
        return names[seatType];
    };

    const SeatComponent: React.FC<{ seat: SeatResponse; rowIndex: number }> = ({ seat, rowIndex }) => (
        <div
            className={`${styles.seat} ${styles[seat.seatType.toLowerCase()]} ${updatingSeat === seat.id ? styles.updating : ''}`}
            onClick={() => handleSeatClick(seat)}
            title={`Row ${rowIndex + 1}, Seat ${seat.number} - ${getSeatTypeName(seat.seatType)}`}
        >
            {updatingSeat === seat.id ? (
                <div className={styles.loadingSpinner}></div>
            ) : (
                <span className={styles.seatNumber}>{seat.number}</span>
            )}
        </div>
    );

    const renderSeats = () => {
        if (!localLayout || !localLayout.rows || localLayout.rows.length === 0) return null;

        return (
            <div className={styles.cinemaHall}>
                <div className={styles.screenArea}>
                    <div className={styles.screen}>SCREEN</div>
                    <div className={styles.screenReflection}></div>
                </div>

                <div className={styles.seatsLayout}>
                    <div className={styles.rowsContainer}>
                        {localLayout.rows.map((row, index) => (
                            <div key={`row-${row.rowNumber}`} className={styles.row}>
                                <div className={styles.seatsRow}>
                                    {row.seats.map(seat => (
                                        <SeatComponent
                                            key={`seat-${seat.id}`}
                                            seat={seat}
                                            rowIndex={index}
                                        />
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className={styles.footer}>
                    <div className={styles.legend}>
                        <div className={styles.legendTitle}>Seat Types:</div>
                        <div className={styles.legendItems}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.standard}`}></div>
                                <span>Standard</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.vip}`}></div>
                                <span>VIP</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.disabled}`}></div>
                                <span>Disabled</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.couple}`}></div>
                                <span>Couple</span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.instructions}>
                        <div className={styles.instructionIcon}>🎯</div>
                        <p className={styles.instructionText}>Click on any seat to change its type</p>
                    </div>

                    <div className={styles.hallStats}>
                        <div className={styles.stat}>
                            <span className={styles.statNumber}>{localLayout.totalSeats}</span>
                            <span className={styles.statLabel}>Total Seats</span>
                        </div>
                        <div className={styles.stat}>
                            <span className={styles.statNumber}>{localLayout.totalRows}</span>
                            <span className={styles.statLabel}>Rows</span>
                        </div>
                        <div className={styles.stat}>
                            <span className={styles.statNumber}>{localLayout.maxSeatsPerRow}</span>
                            <span className={styles.statLabel}>Seats/Row</span>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={`${hall.name} - Seat Layout`}
            size="fullscreen"
        >
            <div className={styles.modalContent}>
                {loading && (
                    <div className={styles.loading}>
                        <div className={styles.loadingSpinner}></div>
                        Loading hall layout...
                    </div>
                )}

                {!loading && (!localLayout || localLayout.rows.length === 0) && (
                    <div className={styles.noLayout}>
                        <div className={styles.emptyIcon}>🎭</div>
                        <h3>No Seats Configured</h3>
                        <p>Please configure seats layout in hall settings.</p>
                    </div>
                )}

                {renderSeats()}
            </div>
        </Modal>
    );
};