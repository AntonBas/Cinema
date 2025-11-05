import React, { useState, useEffect } from 'react';
import type { CinemaHallDto, HallLayoutDto, SeatLayoutRequest, SeatDto } from '@/types';
import { SeatType } from '@/types';
import { useCinemaHalls, useCinemaHallMutation, useSeatMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import styles from './HallLayoutModal.module.css';

interface HallLayoutModalProps {
    hall: CinemaHallDto;
    onClose: () => void;
    onSeatsGenerated?: () => void;
}

export const HallLayoutModal: React.FC<HallLayoutModalProps> = ({
    hall,
    onClose,
    onSeatsGenerated
}) => {
    const [showSeatForm, setShowSeatForm] = useState(false);
    const [updatingSeat, setUpdatingSeat] = useState<number | null>(null);
    const [seatForm, setSeatForm] = useState<SeatLayoutRequest>({
        rows: 5,
        seatsPerRow: 10,
        defaultSeatType: SeatType.STANDARD
    });
    const [localLayout, setLocalLayout] = useState<HallLayoutDto | null>(null);

    const { hallLayout, loading, error, getHallLayout } = useCinemaHalls();
    const { generateSeats, loading: generating } = useCinemaHallMutation();
    const { updateSeatType, loading: updating } = useSeatMutation();
    const { showNotification } = useNotification();

    useEffect(() => {
        setLocalLayout(hallLayout);
    }, [hallLayout]);

    useEffect(() => {
        getHallLayout(hall.id!);
    }, [hall.id, getHallLayout]);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    const handleGenerateSeats = async () => {
        try {
            await generateSeats(hall.id!, seatForm);
            await getHallLayout(hall.id!);
            setShowSeatForm(false);
            onSeatsGenerated?.();
        } catch (err) {
        }
    };

    const handleSeatClick = async (seat: SeatDto) => {
        if (updatingSeat === seat.id || updating) return;

        const nextSeatType = getNextSeatType(seat.seatType);
        const previousLayout = localLayout;

        try {
            setUpdatingSeat(seat.id);

            setLocalLayout(prev => {
                if (!prev) return prev;
                return {
                    ...prev,
                    rows: prev.rows.map(row => ({
                        ...row,
                        seats: row.seats.map(s =>
                            s.id === seat.id ? { ...s, seatType: nextSeatType } : s
                        )
                    }))
                };
            });

            await updateSeatType(hall.id!, seat.id, nextSeatType);

            await getHallLayout(hall.id!);

            showNotification(`Seat type updated to ${getSeatTypeName(nextSeatType)}`, 'success');
        } catch (err) {
            setLocalLayout(previousLayout);
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
                            <div key={row.rowNumber} className={styles.row}>
                                <div className={styles.rowIndicator}>
                                    <span className={styles.rowLetter}>
                                        {String.fromCharCode(65 + index)}
                                    </span>
                                </div>
                                <div className={styles.seatsRow}>
                                    {row.seats.map(seat => (
                                        <div
                                            key={seat.id}
                                            className={`${styles.seat} ${styles[seat.seatType.toLowerCase()]} ${updatingSeat === seat.id ? styles.updating : ''
                                                }`}
                                            onClick={() => handleSeatClick(seat)}
                                            title={`Row ${String.fromCharCode(65 + index)}, Seat ${seat.number} - ${getSeatTypeName(seat.seatType)}`}
                                        >
                                            {updatingSeat === seat.id ? (
                                                <div className={styles.loadingSpinner}></div>
                                            ) : (
                                                <span className={styles.seatNumber}>{seat.number}</span>
                                            )}
                                        </div>
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
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.modalHeader}>
                    <h2>🎭 {hall.name} - Seat Layout</h2>
                    <button className={styles.closeButton} onClick={onClose}>×</button>
                </div>

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
                            <p>This hall doesn't have any seats yet.</p>
                            <button
                                className={styles.generateButton}
                                onClick={() => setShowSeatForm(true)}
                                disabled={generating}
                            >
                                {generating ? 'Generating...' : 'Generate Seats Layout'}
                            </button>
                        </div>
                    )}

                    {showSeatForm && (
                        <div className={styles.seatForm}>
                            <h3>Generate Seats Layout</h3>
                            <div className={styles.formRow}>
                                <label>
                                    Number of Rows:
                                    <input
                                        type="number"
                                        min="1"
                                        max="20"
                                        value={seatForm.rows}
                                        onChange={(e) => setSeatForm({ ...seatForm, rows: parseInt(e.target.value) || 1 })}
                                        disabled={generating}
                                    />
                                </label>
                                <label>
                                    Seats per Row:
                                    <input
                                        type="number"
                                        min="1"
                                        max="20"
                                        value={seatForm.seatsPerRow}
                                        onChange={(e) => setSeatForm({ ...seatForm, seatsPerRow: parseInt(e.target.value) || 1 })}
                                        disabled={generating}
                                    />
                                </label>
                                <label>
                                    Default Seat Type:
                                    <select
                                        value={seatForm.defaultSeatType}
                                        onChange={(e) => setSeatForm({ ...seatForm, defaultSeatType: e.target.value as SeatType })}
                                        disabled={generating}
                                    >
                                        {Object.values(SeatType).map(type => (
                                            <option key={type} value={type}>
                                                {getSeatTypeName(type)}
                                            </option>
                                        ))}
                                    </select>
                                </label>
                            </div>
                            <div className={styles.formActions}>
                                <button
                                    className={styles.cancelButton}
                                    onClick={() => setShowSeatForm(false)}
                                    disabled={generating}
                                >
                                    Cancel
                                </button>
                                <button
                                    className={styles.generateButton}
                                    onClick={handleGenerateSeats}
                                    disabled={generating}
                                >
                                    {generating ? 'Generating...' : 'Generate Seats'}
                                </button>
                            </div>
                        </div>
                    )}

                    {renderSeats()}
                </div>
            </div>
        </div>
    );
};