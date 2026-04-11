import React, { useState, useMemo } from 'react';
import type { SeatResponse, SeatRowResponse } from '@/types/seat';
import { SeatType } from '@/types/seat';
import { Modal } from '@/components/ui/Modal/Modal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { useHallLayout } from '../HallLayoutContext';
import styles from './HallLayoutModal.module.css';

const getNextSeatType = (currentType: SeatType): SeatType => {
    const types: SeatType[] = [SeatType.STANDARD, SeatType.VIP, SeatType.COUPLE];
    const currentIndex = types.indexOf(currentType);
    return types[(currentIndex + 1) % types.length];
};

const getSeatTypeName = (seatType: SeatType): string => {
    const names: Record<SeatType, string> = {
        [SeatType.STANDARD]: 'Standard',
        [SeatType.VIP]: 'VIP',
        [SeatType.COUPLE]: 'Couple'
    };
    return names[seatType];
};

interface SeatComponentProps {
    seat: SeatResponse;
    onTypeChange: (seat: SeatResponse) => void;
    onStatusToggle: (seat: SeatResponse) => void;
    updating: boolean;
}

const SeatComponent: React.FC<SeatComponentProps> = ({ seat, onTypeChange, onStatusToggle, updating }) => {
    const handleClick = (e: React.MouseEvent) => {
        e.stopPropagation();
        if (!updating) onTypeChange(seat);
    };

    const handleContextMenu = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (!updating) onStatusToggle(seat);
    };

    return (
        <button
            className={`${styles.seatButton} ${styles[seat.seatType.toLowerCase()]} ${!seat.active ? styles.inactive : ''}`}
            onClick={handleClick}
            onContextMenu={handleContextMenu}
            title={`Row ${seat.row}, Seat ${seat.number}\nType: ${getSeatTypeName(seat.seatType)}\nStatus: ${seat.active ? 'Active' : 'Inactive'}\n\nLeft click: Change type\nRight click: Toggle status`}
            disabled={updating}
        >
            {updating ? (
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
};

export const HallLayoutModal: React.FC = () => {
    const {
        currentHall,
        layout,
        loading,
        closeLayout,
        updateSeatType,
        toggleSeatStatus
    } = useHallLayout();

    const [updatingSeatId, setUpdatingSeatId] = useState<number | null>(null);

    const handleTypeChange = async (seat: SeatResponse) => {
        if (!currentHall) return;

        const nextSeatType = getNextSeatType(seat.seatType);
        setUpdatingSeatId(seat.id);
        await updateSeatType(seat.id, nextSeatType);
        setUpdatingSeatId(null);
    };

    const handleStatusToggle = async (seat: SeatResponse) => {
        if (!currentHall) return;

        setUpdatingSeatId(seat.id);
        await toggleSeatStatus(seat.id);
        setUpdatingSeatId(null);
    };

    const activeSeatsCount = useMemo(() => {
        if (!layout?.rows) return 0;
        return layout.rows.reduce(
            (acc, row) => acc + row.seats.filter((s) => s.active).length,
            0
        );
    }, [layout]);

    if (!currentHall) return null;

    const sortedRows = layout?.rows ? [...layout.rows].sort((a, b) => a.rowNumber - b.rowNumber) : [];

    return (
        <Modal
            isOpen={!!currentHall}
            onClose={closeLayout}
            title={`${currentHall.name} - Seat Management`}
            size="fullscreen"
        >
            <div className={styles.modalContent}>
                {loading && !layout && (
                    <div className={styles.loading}>
                        <LoadingSpinner text="Loading hall layout..." />
                    </div>
                )}

                {!loading && !layout?.rows?.length && (
                    <div className={styles.noLayout}>
                        <div className={styles.emptyIcon}>🎭</div>
                        <h3>No Seats Configured</h3>
                        <p>Please configure seats layout in hall settings.</p>
                    </div>
                )}

                {layout && (
                    <div className={styles.cinemaHall}>
                        <div className={styles.headerControls}>
                            <div className={styles.stats}>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>{layout.totalSeats}</span>
                                    <span className={styles.statLabel}>Total Seats</span>
                                </div>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>{activeSeatsCount}</span>
                                    <span className={styles.statLabel}>Active</span>
                                </div>
                                <div className={styles.statItem}>
                                    <span className={styles.statNumber}>{layout.totalSeats - activeSeatsCount}</span>
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
                                {sortedRows.map((row: SeatRowResponse) => (
                                    <div key={`row-${row.rowNumber}`} className={styles.row}>
                                        <div className={styles.rowLabel}>Row {row.rowNumber}</div>
                                        <div className={styles.seatsRow}>
                                            {row.seats
                                                .sort((a, b) => a.number - b.number)
                                                .map((seat: SeatResponse) => (
                                                    <SeatComponent
                                                        key={`seat-${seat.id}`}
                                                        seat={seat}
                                                        onTypeChange={handleTypeChange}
                                                        onStatusToggle={handleStatusToggle}
                                                        updating={updatingSeatId === seat.id}
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
                )}
            </div>
        </Modal>
    );
};