import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, HallLayoutResponse } from '@/types/cinemaHall';
import { type SeatResponse, SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSeats } from '@/hooks/features/seats/useSeats';
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
    const [updatingSeatAction, setUpdatingSeatAction] = useState<'type' | 'status' | null>(null);
    const [localLayout, setLocalLayout] = useState<HallLayoutResponse | null>(null);
    const [hasLoaded, setHasLoaded] = useState(false);

    const { hallLayout, loading, getHallLayout } = useCinemaHalls();
    const { updateSeatType, activateSeat, deactivateSeat, loading: updatingSeatType } = useSeats();

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

    const handleSeatTypeClick = async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatType) return;

        e.stopPropagation();
        const nextSeatType = getNextSeatType(seat.seatType);

        try {
            setUpdatingSeat(seat.id);
            setUpdatingSeatAction('type');

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
        } catch (err) {
            await getHallLayout(hall.id);
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    };

    const handleSeatStatusClick = async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatType) return;

        e.stopPropagation();
        e.preventDefault();

        const newActive = !seat.active;

        try {
            setUpdatingSeat(seat.id);
            setUpdatingSeatAction('status');

            const updatedSeat = { ...seat, active: newActive };

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

            if (newActive) {
                await activateSeat(hall.id, seat.id);
            } else {
                await deactivateSeat(hall.id, seat.id);
            }
        } catch (err) {
            await getHallLayout(hall.id);
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    };

    const getNextSeatType = (currentType: SeatType): SeatType => {
        const types = [SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE];
        const currentIndex = types.indexOf(currentType);
        const nextIndex = (currentIndex + 1) % types.length;
        return types[nextIndex];
    };

    const getSeatTypeName = (seatType: SeatType): string => {
        const names = {
            [SeatType.STANDARD]: 'Standard',
            [SeatType.VIP]: 'VIP',
            [SeatType.COUPLE]: 'Couple'
        };
        return names[seatType] || seatType;
    };

    const SeatComponent: React.FC<{ seat: SeatResponse; rowIndex: number }> = ({ seat }) => {
        const isUpdatingType = updatingSeat === seat.id && updatingSeatAction === 'type';
        const isUpdatingStatus = updatingSeat === seat.id && updatingSeatAction === 'status';

        return (
            <button
                className={`${styles.seatButton} ${styles[seat.seatType.toLowerCase()]} ${!seat.active ? styles.inactive : ''}`}
                onClick={(e) => handleSeatTypeClick(seat, e)}
                onContextMenu={(e) => handleSeatStatusClick(seat, e)}
                title={`Row ${seat.row}, Seat ${seat.number}\nType: ${getSeatTypeName(seat.seatType)}\nStatus: ${seat.active ? 'Active' : 'Inactive'}\n\nLeft click: Change type\nRight click: Toggle status`}
                disabled={isUpdatingType || isUpdatingStatus}
            >
                {isUpdatingType || isUpdatingStatus ? (
                    <div className={styles.loadingSpinner}></div>
                ) : (
                    <span className={styles.seatNumber}>{seat.number}</span>
                )}
                {!seat.active && (
                    <div className={styles.inactiveOverlay}>
                        <span className={styles.inactiveIcon}>✕</span>
                    </div>
                )}
            </button>
        );
    };

    const renderSeats = () => {
        if (!localLayout || !localLayout.rows || localLayout.rows.length === 0) return null;

        const activeSeatsCount = localLayout.rows.reduce(
            (acc, row) => acc + row.seats.filter(s => s.active).length, 0
        );

        return (
            <div className={styles.cinemaHall}>
                <div className={styles.headerControls}>
                    <div className={styles.stats}>
                        <div className={styles.statItem}>
                            <span className={styles.statNumber}>{localLayout.totalSeats}</span>
                            <span className={styles.statLabel}>Total Seats</span>
                        </div>
                        <div className={styles.statItem}>
                            <span className={styles.statNumber}>{activeSeatsCount}</span>
                            <span className={styles.statLabel}>Active</span>
                        </div>
                        <div className={styles.statItem}>
                            <span className={styles.statNumber}>{localLayout.totalSeats - activeSeatsCount}</span>
                            <span className={styles.statLabel}>Inactive</span>
                        </div>
                    </div>
                </div>

                <div className={styles.screenArea}>
                    <div className={styles.screen}>SCREEN</div>
                    <div className={styles.screenReflection}></div>
                </div>

                <div className={styles.seatsLayout}>
                    <div className={styles.rowsContainer}>
                        {localLayout.rows.map((row) => (
                            <div key={`row-${row.rowNumber}`} className={styles.row}>
                                <div className={styles.rowLabel}>Row {row.rowNumber}</div>
                                <div className={styles.seatsRow}>
                                    {row.seats.map(seat => (
                                        <SeatComponent
                                            key={`seat-${seat.id}`}
                                            seat={seat}
                                            rowIndex={row.rowNumber}
                                        />
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className={styles.footer}>
                    <div className={styles.legend}>
                        <h4 className={styles.legendTitle}>Seat Types:</h4>
                        <div className={styles.legendGrid}>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.standard}`}></div>
                                <span>Standard</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.vip}`}></div>
                                <span>VIP</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.couple}`}></div>
                                <span>Couple</span>
                            </div>
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.inactive}`}></div>
                                <span>Inactive</span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.instructions}>
                        <div className={styles.instructionIcon}>🎯</div>
                        <div className={styles.instructionText}>
                            <p><strong>Left click:</strong> Change seat type</p>
                            <p><strong>Right click:</strong> Toggle active status</p>
                            <p className={styles.note}>Inactive seats cannot be booked</p>
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
            title={`${hall.name} - Seat Management`}
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