import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, HallLayoutResponse, SeatLayoutRequest, SeatResponse } from '@/types';
import { SeatType } from '@/types';
import { useCinemaHalls, useCinemaHallMutation, useSeatMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { Button, Input, Select } from '@/components/ui';
import styles from './HallLayoutModal.module.css';

interface HallLayoutModalProps {
    hall: CinemaHallResponse;
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
    const [localLayout, setLocalLayout] = useState<HallLayoutResponse | null>(null);

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

    const handleSeatClick = async (seat: SeatResponse) => {
        if (updatingSeat === seat.id || updating) return;

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

            await updateSeatType(hall.id!, seat.id, nextSeatType);

            showNotification(`Seat type updated to ${getSeatTypeName(nextSeatType)}`, 'success');
        } catch (err) {
            await getHallLayout(hall.id!);
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
            title={`Row ${String.fromCharCode(65 + rowIndex)}, Seat ${seat.number} - ${getSeatTypeName(seat.seatType)}`}
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
                                <div className={styles.rowIndicator}>
                                    <span className={styles.rowLetter}>
                                        {String.fromCharCode(65 + index)}
                                    </span>
                                </div>
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

    const seatTypeOptions = Object.values(SeatType).map(type => ({
        value: type,
        label: getSeatTypeName(type)
    }));

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <div className={styles.modalHeader}>
                    <h2>{hall.name} - Seat Layout</h2>
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
                            <Button
                                variant="primary"
                                onClick={() => setShowSeatForm(true)}
                                disabled={generating}
                                className={styles.generateButton}
                            >
                                {generating ? 'Generating...' : 'Generate Seats Layout'}
                            </Button>
                        </div>
                    )}

                    {showSeatForm && (
                        <div className={styles.seatForm}>
                            <h3>Generate Seats Layout</h3>
                            <div className={styles.formRow}>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                    <label style={{ color: '#ffffff', fontWeight: 600, fontSize: '0.9rem' }}>
                                        Number of Rows:
                                    </label>
                                    <Input
                                        type="number"
                                        value={seatForm.rows.toString()}
                                        onChange={(value) => {
                                            const numValue = parseInt(value) || 1;
                                            if (numValue >= 1 && numValue <= 20) {
                                                setSeatForm({ ...seatForm, rows: numValue });
                                            }
                                        }}
                                        disabled={generating}
                                    />
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                    <label style={{ color: '#ffffff', fontWeight: 600, fontSize: '0.9rem' }}>
                                        Seats per Row:
                                    </label>
                                    <Input
                                        type="number"
                                        value={seatForm.seatsPerRow.toString()}
                                        onChange={(value) => {
                                            const numValue = parseInt(value) || 1;
                                            if (numValue >= 1 && numValue <= 20) {
                                                setSeatForm({ ...seatForm, seatsPerRow: numValue });
                                            }
                                        }}
                                        disabled={generating}
                                    />
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                    <label style={{ color: '#ffffff', fontWeight: 600, fontSize: '0.9rem' }}>
                                        Default Seat Type:
                                    </label>
                                    <Select
                                        value={seatForm.defaultSeatType}
                                        onChange={(value) => setSeatForm({ ...seatForm, defaultSeatType: value as SeatType })}
                                        options={seatTypeOptions}
                                        disabled={generating}
                                    />
                                </div>
                            </div>
                            <div style={{
                                display: 'flex',
                                gap: '1rem',
                                justifyContent: 'center',
                                marginTop: '1.5rem'
                            }}>
                                <Button
                                    variant="cancel"
                                    onClick={() => setShowSeatForm(false)}
                                    disabled={generating}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    variant="primary"
                                    onClick={handleGenerateSeats}
                                    disabled={generating}
                                >
                                    {generating ? 'Generating...' : 'Generate Seats'}
                                </Button>
                            </div>
                        </div>
                    )}

                    {renderSeats()}
                </div>
            </div>
        </div>
    );
};