import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { CinemaHallResponse, HallLayoutResponse } from '@/types/cinemaHall';
import { type SeatResponse, SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useSeats } from '@/hooks/features/seats/useSeats';
import { Modal } from '@/components/ui/Modal/Modal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
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
    const hasLoadedRef = useRef(false);

    const { hallLayout, loading, getHallLayout } = useCinemaHalls();
    const { updateSeatType, setSeatStatus, loading: updatingSeatLoading } = useSeats();

    useEffect(() => {
        if (hallLayout) {
            setLocalLayout(hallLayout);
        }
    }, [hallLayout]);

    useEffect(() => {
        if (isOpen && hall.id && !hasLoadedRef.current) {
            getHallLayout(hall.id);
            hasLoadedRef.current = true;
        }
    }, [hall.id, getHallLayout, isOpen]);

    useEffect(() => {
        if (!isOpen) {
            hasLoadedRef.current = false;
            setLocalLayout(null);
        }
    }, [isOpen]);

    const getNextSeatType = useCallback((currentType: SeatType): SeatType => {
        const types = [SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE];
        const currentIndex = types.indexOf(currentType);
        const nextIndex = (currentIndex + 1) % types.length;
        return types[nextIndex];
    }, []);

    const getSeatTypeName = useCallback((seatType: SeatType): string => {
        const names = {
            [SeatType.STANDARD]: 'Standard',
            [SeatType.VIP]: 'VIP',
            [SeatType.COUPLE]: 'Couple'
        };
        return names[seatType] || seatType;
    }, []);

    const handleSeatTypeClick = useCallback(async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatLoading) return;

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
        } catch {
            await getHallLayout(hall.id);
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    }, [hall.id, updatingSeat, updatingSeatLoading, getNextSeatType, updateSeatType, getHallLayout]);

    const handleSeatStatusClick = useCallback(async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatLoading) return;

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

            await setSeatStatus(hall.id, seat.id, newActive);
        } catch {
            await getHallLayout(hall.id);
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    }, [hall.id, updatingSeat, updatingSeatLoading, setSeatStatus, getHallLayout]);

    const activeSeatsCount = useMemo(() => {
        if (!localLayout?.rows) return 0;
        return localLayout.rows.reduce(
            (acc, row) => acc + row.seats.filter(s => s.active).length, 0
        );
    }, [localLayout]);

    const SeatComponent = useCallback(({ seat }: { seat: SeatResponse; rowIndex: number }) => {
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
                    <div className={styles.loadingSpinner} />
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
    }, [updatingSeat, updatingSeatAction, handleSeatTypeClick, handleSeatStatusClick, getSeatTypeName]);

    const renderSeats = useCallback(() => {
        if (!localLayout?.rows?.length) return null;

        const sortedRows = [...localLayout.rows].sort((a, b) => a.rowNumber - b.rowNumber);

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
                    <div className={styles.screenReflection} />
                </div>

                <div className={styles.seatsLayout}>
                    <div className={styles.rowsContainer}>
                        {sortedRows.map((row) => (
                            <div key={`row-${row.rowNumber}`} className={styles.row}>
                                <div className={styles.rowLabel}>Row {row.rowNumber}</div>
                                <div className={styles.seatsRow}>
                                    {row.seats
                                        .sort((a, b) => a.number - b.number)
                                        .map(seat => (
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
                            {Object.values(SeatType).map((type) => (
                                <div key={type} className={styles.legendItem}>
                                    <div className={`${styles.legendColor} ${styles[type.toLowerCase()]}`} />
                                    <span>{getSeatTypeName(type)}</span>
                                </div>
                            ))}
                            <div className={styles.legendItem}>
                                <div className={`${styles.legendColor} ${styles.inactive}`} />
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
    }, [localLayout, activeSeatsCount, SeatComponent, getSeatTypeName]);

    if (!isOpen) return null;

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={`${hall.name} - Seat Management`}
            size="fullscreen"
        >
            <div className={styles.modalContent}>
                {loading && !localLayout && (
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading hall layout..." />
                    </div>
                )}

                {!loading && !localLayout?.rows?.length && (
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