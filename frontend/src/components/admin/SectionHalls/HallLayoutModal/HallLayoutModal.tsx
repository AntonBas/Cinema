import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, HallLayoutResponse, SeatResponse } from '@/types';
import { SeatType } from '@/types';
import { useCinemaHalls, useSeatMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { Modal, Button } from '@/components/ui';
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
    const [selectedSeat, setSelectedSeat] = useState<SeatResponse | null>(null);
    const [showSeatDetails, setShowSeatDetails] = useState(false);

    const { hallLayout, loading, error, getHallLayout } = useCinemaHalls();
    const { updateSeatType, activateSeat, deactivateSeat, loading: updatingSeatType } = useSeatMutation();
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
            setSelectedSeat(null);
            setShowSeatDetails(false);
        }
    }, [isOpen]);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    const handleSeatTypeClick = async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatType) return;

        e.stopPropagation();
        setSelectedSeat(seat);
        setShowSeatDetails(true);

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

            showNotification(`Seat ${seat.row}-${seat.number} type changed to ${getSeatTypeName(nextSeatType)}`, 'success');
        } catch (err) {
            await getHallLayout(hall.id);
            showNotification('Failed to update seat type', 'error');
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    };

    const handleSeatStatusClick = async (seat: SeatResponse, e: React.MouseEvent) => {
        if (updatingSeat === seat.id || updatingSeatType) return;

        e.stopPropagation();
        e.preventDefault();
        setSelectedSeat(seat);
        setShowSeatDetails(true);

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
                showNotification(`Seat ${seat.row}-${seat.number} activated`, 'success');
            } else {
                await deactivateSeat(hall.id, seat.id);
                showNotification(`Seat ${seat.row}-${seat.number} deactivated`, 'success');
            }
        } catch (err) {
            await getHallLayout(hall.id);
            showNotification('Failed to update seat status', 'error');
        } finally {
            setUpdatingSeat(null);
            setUpdatingSeatAction(null);
        }
    };

    const handleQuickToggleStatus = async () => {
        if (!selectedSeat) return;

        const newActive = !selectedSeat.active;

        try {
            if (newActive) {
                await activateSeat(hall.id, selectedSeat.id);
                showNotification(`Seat ${selectedSeat.row}-${selectedSeat.number} activated`, 'success');
            } else {
                await deactivateSeat(hall.id, selectedSeat.id);
                showNotification(`Seat ${selectedSeat.row}-${selectedSeat.number} deactivated`, 'success');
            }

            await getHallLayout(hall.id);
        } catch (err) {
            showNotification('Failed to update seat status', 'error');
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

    const getSeatTypeColor = (seatType: SeatType): string => {
        const colors = {
            [SeatType.STANDARD]: '#4CAF50',
            [SeatType.VIP]: '#FF9800',
            [SeatType.DISABLED]: '#2196F3',
            [SeatType.COUPLE]: '#9C27B0'
        };
        return colors[seatType];
    };

    const SeatComponent: React.FC<{ seat: SeatResponse; rowIndex: number }> = ({ seat }) => {
        const isUpdatingType = updatingSeat === seat.id && updatingSeatAction === 'type';
        const isUpdatingStatus = updatingSeat === seat.id && updatingSeatAction === 'status';

        return (
            <button
                className={`${styles.seatButton} ${styles[seat.seatType.toLowerCase()]} ${!seat.active ? styles.inactive : ''}`}
                onClick={(e) => handleSeatTypeClick(seat, e)}
                onContextMenu={(e) => handleSeatStatusClick(seat, e)}
                title={`Row ${seat.row}, Seat ${seat.number}
Type: ${getSeatTypeName(seat.seatType)}
Status: ${seat.active ? 'Active' : 'Inactive'}
Left click: Change type
Right click: Activate/Deactivate`}
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
                        {localLayout.rows.map((row, rowIndex) => (
                            <div key={`row-${row.rowNumber}`} className={styles.row}>
                                <div className={styles.rowLabel}>Row {row.rowNumber}</div>
                                <div className={styles.seatsRow}>
                                    {row.seats.map(seat => (
                                        <SeatComponent
                                            key={`seat-${seat.id}`}
                                            seat={seat}
                                            rowIndex={rowIndex}
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
                                <div className={`${styles.legendColor} ${styles.disabled}`}></div>
                                <span>Disabled</span>
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

                    <div className={styles.controls}>
                        <div className={styles.instructions}>
                            <div className={styles.instructionIcon}>🎯</div>
                            <div className={styles.instructionText}>
                                <p><strong>Left click:</strong> Change seat type</p>
                                <p><strong>Right click:</strong> Toggle active status</p>
                                <p className={styles.note}><em>Inactive seats cannot be booked</em></p>
                            </div>
                        </div>

                        {selectedSeat && showSeatDetails && (
                            <div className={styles.seatDetails}>
                                <h4>Selected Seat</h4>
                                <div className={styles.seatInfo}>
                                    <div className={styles.seatInfoItem}>
                                        <span>Position:</span>
                                        <strong>Row {selectedSeat.row}, Seat {selectedSeat.number}</strong>
                                    </div>
                                    <div className={styles.seatInfoItem}>
                                        <span>Type:</span>
                                        <strong style={{ color: getSeatTypeColor(selectedSeat.seatType) }}>
                                            {getSeatTypeName(selectedSeat.seatType)}
                                        </strong>
                                    </div>
                                    <div className={styles.seatInfoItem}>
                                        <span>Status:</span>
                                        <strong className={selectedSeat.active ? styles.statusActive : styles.statusInactive}>
                                            {selectedSeat.active ? 'Active' : 'Inactive'}
                                        </strong>
                                    </div>
                                </div>
                                <div className={styles.seatActions}>
                                    <Button
                                        variant={selectedSeat.active ? "error" : "success"}
                                        size="small"
                                        onClick={handleQuickToggleStatus}
                                        disabled={updatingSeatType}
                                    >
                                        {selectedSeat.active ? 'Deactivate' : 'Activate'}
                                    </Button>
                                    <Button
                                        variant="cancel"
                                        size="small"
                                        onClick={() => setShowSeatDetails(false)}
                                    >
                                        Close
                                    </Button>
                                </div>
                            </div>
                        )}
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