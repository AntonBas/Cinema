import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import type { SeatResponse } from '@/types/seat';
import type { HallLayoutResponse } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { Modal } from '@/components/ui/Modal/Modal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { useHallLayout } from '../HallLayoutContext';
import styles from './HallLayoutModal.module.css';

export const HallLayoutModal: React.FC = () => {
    const {
        currentHall,
        layout,
        loading,
        closeLayout,
        updateSeatType,
        toggleSeatStatus
    } = useHallLayout();

    const [updatingSeat, setUpdatingSeat] = useState<number | null>(null);
    const [updatingSeatAction, setUpdatingSeatAction] = useState<'type' | 'status' | null>(null);
    const [localLayout, setLocalLayout] = useState<HallLayoutResponse | null>(layout);
    const isMountedRef = useRef<boolean>(true);

    useEffect(() => {
        isMountedRef.current = true;
        return () => {
            isMountedRef.current = false;
        };
    }, []);

    useEffect(() => {
        if (layout && isMountedRef.current) {
            setLocalLayout(layout);
        }
    }, [layout]);

    useEffect(() => {
        if (!currentHall) {
            setLocalLayout(null);
        }
    }, [currentHall]);

    const getNextSeatType = useCallback((currentType: SeatType): SeatType => {
        const types: SeatType[] = [SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE];
        const currentIndex = types.indexOf(currentType);
        const nextIndex = (currentIndex + 1) % types.length;
        return types[nextIndex];
    }, []);

    const getSeatTypeName = useCallback((seatType: SeatType): string => {
        const names: Record<SeatType, string> = {
            [SeatType.STANDARD]: 'Standard',
            [SeatType.VIP]: 'VIP',
            [SeatType.COUPLE]: 'Couple'
        };
        return names[seatType] || seatType;
    }, []);

    const handleSeatTypeClick = useCallback(async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || !currentHall || !isMountedRef.current) return;

        e.stopPropagation();
        const nextSeatType = getNextSeatType(seat.seatType);

        try {
            setUpdatingSeat(seat.id);
            setUpdatingSeatAction('type');

            if (isMountedRef.current) {
                setLocalLayout((prev: HallLayoutResponse | null) => {
                    if (!prev) return prev;
                    return {
                        ...prev,
                        rows: prev.rows.map((row: any) => ({
                            ...row,
                            seats: row.seats.map((s: SeatResponse) =>
                                s.id === seat.id ? { ...seat, seatType: nextSeatType } : s
                            )
                        }))
                    };
                });
            }

            await updateSeatType(seat.id, nextSeatType);
        } finally {
            if (isMountedRef.current) {
                setUpdatingSeat(null);
                setUpdatingSeatAction(null);
            }
        }
    }, [currentHall, updatingSeat, getNextSeatType, updateSeatType]);

    const handleSeatStatusClick = useCallback(async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || !currentHall || !isMountedRef.current) return;

        e.stopPropagation();
        e.preventDefault();

        try {
            setUpdatingSeat(seat.id);
            setUpdatingSeatAction('status');

            if (isMountedRef.current) {
                setLocalLayout((prev: HallLayoutResponse | null) => {
                    if (!prev) return prev;
                    return {
                        ...prev,
                        rows: prev.rows.map((row: any) => ({
                            ...row,
                            seats: row.seats.map((s: SeatResponse) =>
                                s.id === seat.id ? { ...seat, active: !seat.active } : s
                            )
                        }))
                    };
                });
            }

            await toggleSeatStatus(seat.id);
        } finally {
            if (isMountedRef.current) {
                setUpdatingSeat(null);
                setUpdatingSeatAction(null);
            }
        }
    }, [currentHall, updatingSeat, toggleSeatStatus]);

    const activeSeatsCount = useMemo(() => {
        if (!localLayout?.rows) return 0;
        return localLayout.rows.reduce(
            (acc: number, row: any) => acc + row.seats.filter((s: SeatResponse) => s.active).length, 0
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

        const sortedRows = [...localLayout.rows].sort((a: any, b: any) => a.rowNumber - b.rowNumber);

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
                        {sortedRows.map((row: any) => (
                            <div key={`row-${row.rowNumber}`} className={styles.row}>
                                <div className={styles.rowLabel}>Row {row.rowNumber}</div>
                                <div className={styles.seatsRow}>
                                    {row.seats
                                        .sort((a: any, b: any) => a.number - b.number)
                                        .map((seat: SeatResponse) => (
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
                            {Object.values(SeatType).map((type: SeatType) => (
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

    if (!currentHall) return null;

    return (
        <Modal
            isOpen={!!currentHall}
            onClose={closeLayout}
            title={`${currentHall.name} - Seat Management`}
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